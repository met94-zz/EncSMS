package zls.mutek.encsms;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Created by abara on 6/22/2017.
 */

public class InputSecurityKeyDialogFragment extends DialogFragment {

    EditText keyEditBox;
    Button showEULAButton;

    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        //getActivity().finish();
        exitApp();
    }

    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
        exitApp();
    }

    private void exitApp()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().finishAndRemoveTask();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getActivity().finishAffinity();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        TextView message = (TextView)getDialog().findViewById(android.R.id.message);
        if(message != null) {
            message.setGravity(Gravity.CENTER);
            message.setTextSize(22);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_inputsecuritykey, null); //ignore lint as null is required for dialogs
        builder.setView(view);

        builder.setMessage(R.string.dialog_key_license_ver)
                .setPositiveButton(R.string.dialog_eula_agree, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        String hash = Utils.getKeyHash(Utils.getEncryptedIMEI(getActivity())); //getActivity should return MainActivity
                        String enteredKey = keyEditBox.getText().toString().replace("-", "");
                        if(hash.contains(enteredKey)) {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                            sharedPreferences.edit().putString(getActivity().getString(R.string.generalPreferences_securityKeyKey), enteredKey).apply();
                        }
                    }
                });/*
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onInputPassDialogCancelClick(InputPassDialogFragment.this);
                    }
                }); */

        showEULAButton = (Button)view.findViewById(R.id.showEula_Button);
        showEULAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                InputStream is = getResources().openRawResource(R.raw.eula);
                String eula;
                //is.read()
                try {
                    eula = IOUtils.toString(is);
                } catch(java.io.IOException e) {
                    eula = e.getMessage();
                }
                builder.setMessage(eula)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                builder.create().show();
            }
        });
        keyEditBox = (EditText) view.findViewById(R.id.keyEditBox_keyDialog);
        keyEditBox.addTextChangedListener(new TextWatcher() {
            boolean removing=false;
            boolean adding=false;
            boolean ignore=false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                removing = (start >0 && after <= 0);
                adding = (s.length() > 1 && s.toString().replace("-", "").length()%5 == 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(ignore) {
                    return;
                }

                int len = s.toString().replace("-", "").length(); //count without '-' character

                if(len > 0 && s.charAt(s.length()-1) != '-' && !removing) {
                    if (len % 5 == 0) {
                        s.append('-');
                    } else if (adding) { //adds '-' when user removed it and then typed another character
                        ignore = true;

                        char c = s.charAt(s.length() - 1);
                        s.replace(s.length() - 1, s.length(), "");
                        if (s.charAt(s.length() - 1) != '-') {
                            s.append('-');
                        }
                        s.append(c);

                        ignore = false;
                    }
                }
            }
        });

        // Create the AlertDialog object and return it
        //Dialog dialog = builder.create();
        return builder.create();
    }
}
