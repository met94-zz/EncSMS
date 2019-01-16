package zls.mutek.encsms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * Created by abara on 5/12/2017.
 * custom dialog class used for inputing messages password
 */

public class InputPassDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the InputPassDialogFragment in case the host needs to query it. */
    interface InputPassDialogListener {
        void onInputPassDialogOkClick(InputPassDialogFragment dialog);
        void onInputPassDialogCancelClick(InputPassDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    InputPassDialogListener mListener;

    ProgressBar passStrengthProgressBar;
    TextView strengthHintLabel;
    TextView strengthLabel;
    EditText passEditBox;

    //this is called when API < 23
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return; //Build.VERSION_CODES.LOLLIPOP_MR1 = 22
        onAttachFunc(activity);
    }

    //this is called when API >= 23
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachFunc(context);
    }

    public void onAttachFunc(Context context)
    {
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the InputPassDialogListener so we can send events to the host
            mListener = (InputPassDialogListener)context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
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
        View view = inflater.inflate(R.layout.dialog_inputpass, null); //ignore lint as null is required for dialogs
        builder.setView(view);

        builder.setMessage(R.string.dialog_pass_enter)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the ok button event back to the host activity
                        mListener.onInputPassDialogOkClick(InputPassDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onInputPassDialogCancelClick(InputPassDialogFragment.this);
                    }
                });

        passStrengthProgressBar = (ProgressBar)view.findViewById(R.id.progressBar_passDialog);
        strengthHintLabel = (TextView)view.findViewById(R.id.strengthHintLabel_passDialog);
        strengthLabel = (TextView)view.findViewById(R.id.strengthLabel_passDialog);
        passEditBox = (EditText) view.findViewById(R.id.passEditBox_passDialog);

        passEditBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) { //used for updating password strength
                int strengthPercentage=0;
                String hint = null;
                String strenghtText = null;
                if(s.length() > 0) {
                    String password = s.toString();
                    String[] partialRegexChecks = {".*[a-z]+.*", // lower
                            ".*[A-Z]+.*", // upper
                            ".*[\\d]+.*", // digits
                            ".*[^A-Za-z0-9]+.*" // symbols
                    };

                    if (password.matches(partialRegexChecks[0])) {
                        strengthPercentage += 25;
                    } else { //no need to check if hint==null cause it always is the first time
                        hint = getString(R.string.dialog_pass_strongHint1);
                    }
                    if (password.matches(partialRegexChecks[1])) {
                        strengthPercentage += 25;
                    } else if (hint == null) {
                        hint = getString(R.string.dialog_pass_strongHint2);
                    }
                    if (password.matches(partialRegexChecks[2])) {
                        strengthPercentage += 25;
                    } else if (hint == null) {
                        hint = getString(R.string.dialog_pass_strongHint3);
                    }
                    if (password.matches(partialRegexChecks[3])) {
                        strengthPercentage += 25;
                    } else if (hint == null) {
                        hint = getString(R.string.dialog_pass_strongHint4);
                    }
                    if (password.length() <= 8) {
                        strengthPercentage /= 10;
                        hint = getString(R.string.dialog_pass_strongHint5);
                    }
                    strenghtText = getString((strengthPercentage == 100) ? R.string.dialog_pass_strong1 : (strengthPercentage >= 75) ?
                            R.string.dialog_pass_strong2 : (strengthPercentage >= 50) ? R.string.dialog_pass_strong3 :
                            (strengthPercentage >= 25) ? R.string.dialog_pass_strong4 : R.string.dialog_pass_strong5);
                }
                if(passStrengthProgressBar != null) {
                    passStrengthProgressBar.setProgress(strengthPercentage);
                }
                if(strengthHintLabel != null) {
                    strengthHintLabel.setText(hint);
                }
                if(strengthLabel != null) {
                    strengthLabel.setText(strenghtText);
                }
            }
        });

        // Create the AlertDialog object and return it
        //Dialog dialog = builder.create();
        return builder.create();
    }

}
