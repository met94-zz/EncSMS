package zls.mutek.encsms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import static android.app.Activity.RESULT_OK;

/**
 * Created by abara on 5/15/2017.
 * custom dialog class used for inputing phone number
 */

public class InputPhoneDialogFragment extends DialogFragment {

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the InputPhoneDialogFragment in case the host needs to query it. */
    interface InputPhoneDialogListener {
        void onInputPhoneDialogOkClick(InputPhoneDialogFragment dialog);
        void onInputPhoneDialogCancelClick(InputPhoneDialogFragment dialog);
        void onInputPhoneDialogPickContactClick(InputPhoneDialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    InputPhoneDialogFragment.InputPhoneDialogListener mListener;

    EditText phoneEditText;
    Button pickContactButton;

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

    public void onAttachFunc(Context context) {
        try {
            // Instantiate the InputPhoneDialogListener so we can send events to the host
            mListener = (InputPhoneDialogListener)context;
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
        View view = inflater.inflate(R.layout.dialog_inputphone, null); //ignore lint as null is required for dialogs
        builder.setView(view);

        builder.setMessage(R.string.dialog_phone_label)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the ok button event back to the host activity
                        mListener.onInputPhoneDialogOkClick(InputPhoneDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onInputPhoneDialogCancelClick(InputPhoneDialogFragment.this);
                    }
                });

        phoneEditText = (EditText)view.findViewById(R.id.phoneEditText_phoneDialog);
        pickContactButton = (Button) view.findViewById(R.id.pickContactButton_phoneDialog);
        pickContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onInputPhoneDialogPickContactClick(InputPhoneDialogFragment.this);
            }
        });

        // Create the AlertDialog object and return it
        //Dialog dialog = builder.create();
        return builder.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request it is that we're responding to
        if (requestCode == SMSConsts.REQUEST_CODE_PICK_CONTACT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the URI that points to the selected contact
                Uri contactUri = data.getData();
                // We only need the NUMBER column, because there will be only one row in the result
                String[] projection = {ContactsContract.CommonDataKinds.Phone.NUMBER};

                // Perform the query on the contact to get the NUMBER column
                // We don't need a selection or sort order (there's only one result for the given URI)
                // CAUTION: The query() method should be called from a separate thread to avoid blocking
                // your app's UI thread. (For simplicity of the sample, this code doesn't do that.)
                // Consider using CursorLoader to perform the query.

                // My Note: I dont care enough about above //possible performance issue
                Cursor cursor = getActivity().getContentResolver().query(contactUri, projection, null, null, null);
                if(cursor != null) {
                    cursor.moveToFirst();

                    // Retrieve the phone number from the NUMBER column
                    int column = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    phoneEditText.setText(cursor.getString(column));
                    if(!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        }
    }

}

