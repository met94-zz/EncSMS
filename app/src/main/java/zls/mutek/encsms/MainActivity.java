package zls.mutek.encsms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, InputPassDialogFragment.InputPassDialogListener,
        InputPhoneDialogFragment.InputPhoneDialogListener
{
    enum WorkingStates {
        ListSMS,
        ListMessages
    }
    enum Themes {
    Dark,
    Light
}

    String MsgHeader;
    boolean useDefaultApp;

    //public static MainActivity activity;

    SimpleCursorAdapter mAdapter;
    static WorkingStates state = WorkingStates.ListSMS;
    String selectedPhoneNumber;
    final StringFuncs stringFuncs = new StringFuncs();
    MyListView msgsList;
    Drawable originalSendButtonDrawable;
    ViewGroup.LayoutParams originalSendButtonLayoutParams;
    Button send_addSMSButton;
    SharedPreferences.OnSharedPreferenceChangeListener mSettingsListener;
    static Themes activeTheme = Themes.Dark;


    String password;

    static final String ACTIVE_THEME_BUNDLE = "activeTheme_BUNDLE";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //initializing settings
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        SettingsActivity.initializeResources(this);
        mSettingsListener = getOnSharedPreferenceChange();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(mSettingsListener);
        mSettingsListener.onSharedPreferenceChanged(PreferenceManager.getDefaultSharedPreferences(this), SettingsActivity.KEY_THEME); //set activeTheme

        if(savedInstanceState != null) {
            activeTheme = (Themes)savedInstanceState.get(ACTIVE_THEME_BUNDLE);
            state = WorkingStates.ListSMS;
        }

        if(activeTheme == Themes.Light) {
            setTheme(R.style.AppThemeLight_NoActionBar);
        }
        super.onCreate(savedInstanceState);

        if(!Utils.askPermissions(this, new String[] { SMSConsts.READ_PHONE_STATE })) {
            return;
        }

        if(!Utils.checkPreferencesSecurityKey(this))
        {
            InputSecurityKeyDialogFragment inputSecurityKeyDialogFragment = new InputSecurityKeyDialogFragment();
            inputSecurityKeyDialogFragment.show(getFragmentManager(), getString(R.string.dialog_key_tag));
        }

        //activity = this;

        if(activeTheme == Themes.Dark) {// || (activeTheme == Themes.Light && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)) {
            ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
            ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
        } else if(activeTheme == Themes.Light) {
            ContextCompat.getDrawable(this, R.drawable.ic_autorenew_black_48dp).clearColorFilter();
            ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).clearColorFilter();
            //ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).setColorFilter(new ColorMatrixColorFilter(Utils.NEGATIVE_COLORFILTER));
        }

        setContentView(R.layout.activity_main);

        if(getSupportActionBar() == null) {
            Toolbar myToolbar = (Toolbar) findViewById(R.id.actionbar_main);
            if(activeTheme == Themes.Light) {
                myToolbar.getContext().setTheme(R.style.AppThemeLight_NoActionBar);
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    myToolbar.setBackgroundColor(0x20444444);
                }
            }
            setSupportActionBar(myToolbar);
        }

        //Utils.showAllCNames();

        String[] fromColumns = {SMSConsts.PhoneNumberCol, SMSConsts.MessageCol, SMSConsts.DateCol};
        int[] toViews = {R.id.text1, R.id.txtMessage, R.id.txtInfo}; // The TextView's in layout

        // Create an empty adapter we will use to display the loaded data.
        // We pass null for the cursor, then update it in onLoadFinished()
        mAdapter = new MyCursorAdapter(this,
                R.layout.simple_list_item_3, null,
                fromColumns, toViews, 0);

        mAdapter.setViewBinder(getViewBinder());

        msgsList = ((MyListView) findViewById(R.id.msgs_list));
        msgsList.setAdapter(mAdapter);
        msgsList.setOnItemClickListener(getListItemClickListener());
        Utils.setDefaultDivider(msgsList);

        send_addSMSButton = ((Button) findViewById(R.id.SMSSendButton));
        originalSendButtonDrawable = send_addSMSButton.getBackground();
        originalSendButtonLayoutParams = new ViewGroup.LayoutParams(send_addSMSButton.getLayoutParams());
        changeSendButtonToAddButton(send_addSMSButton);
        send_addSMSButton.setOnClickListener(getOnSendButtonClick());

        String[] perms = new String[] { SMSConsts.READ_SMS_PERM, SMSConsts.SEND_SMS_PERM, SMSConsts.READ_CONTACTS_PERM };
        if(!Utils.askPermissions(this, perms)) {
            return;
        }

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState (Bundle savedInstanceState) {

        savedInstanceState.putSerializable(ACTIVE_THEME_BUNDLE, activeTheme);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if(state == WorkingStates.ListMessages) {
            //msgsList.bShouldChangeSendToAddButton = true;
            changeSendButtonToAddButton(send_addSMSButton);
            state = WorkingStates.ListSMS;
            password = null;
            refreshMsgsList();
            if(getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.app_name);
            }
            EditText inputSMS = ((EditText) findViewById(R.id.SMSeditText));
            inputSMS.setVisibility(View.INVISIBLE);

            //sendSMS.setVisibility(View.INVISIBLE);
            //Utils.setDefaultDivider(msgsList);
            //shouldScrollTop = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_refresh:
                //fillSMSList();
                refreshMsgsList();
                return true;
            case R.id.action_add:
                InputPhoneDialogFragment dialog = new InputPhoneDialogFragment();
                dialog.show(getFragmentManager(), getString(R.string.dialog_phone_tag));
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }
    /**********************************
     *
     * On Request Permission Result
     *
     **********************************/
    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull  int[] grantResults)
    {
        if(requestCode == SMSConsts.REQUEST_CODE_ASK_PERMISSIONS)
        {
            boolean refresh = false;
            for(int i=0; i<permissions.length && i<grantResults.length; i++)
            {
                if(permissions[i].compareTo(SMSConsts.READ_SMS_PERM) == 0 && grantResults[i] == PERMISSION_GRANTED)
                {
                    refresh = true;
                } else if(permissions[i].compareTo(SMSConsts.READ_PHONE_STATE) == 0) {
                    if(grantResults[i] == PERMISSION_DENIED) {
                        Dialog dialog = new Dialog(this);
                        dialog.setContentView(R.layout.dialog_noperms);
                        dialog.setTitle(R.string.dialog_missing_permsTitle);
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                finish();
                            }
                        });
                        dialog.show();
                    } else {
                        recreate();
                    }
                }
            }
            if(refresh) {
                refreshMsgsList();
            }
        }
    }

    /**********************************
     *
     * Loader implementation start
     *
     **********************************/
    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        String orderByClause = null;
        String whereClause = SMSConsts.MessageCol + " LIKE '" + MsgHeader + "%'" + " AND " + SMSConsts.WHERE_CLAUSE_TYPE + " AND "; //message body filter
        if(state == WorkingStates.ListSMS) {
            orderByClause = SMSConsts.ORDER_BY_SMS;
            whereClause += SMSConsts.DateCol + " in (select max(" + SMSConsts.DateCol + ") from sms " + " where " + SMSConsts.MessageCol +
                    " like '" + MsgHeader + "%'" + " group by " + SMSConsts.PhoneNumberCol + ")";
        } else if(state == WorkingStates.ListMessages) {
            orderByClause = SMSConsts.ORDER_BY_MSGS;
            whereClause += SMSConsts.WHERE_CLAUSE_PHONE_NUMBER_SMS + "'" + selectedPhoneNumber + "'"; //filter messages by phone number
        }
        CursorLoader tet = new CursorLoader(this, Uri.parse(SMSConsts.SMS_URI),
                new String[]{SMSConsts.IdCol, SMSConsts.PhoneNumberCol, SMSConsts.MessageCol, SMSConsts.DateCol, SMSConsts.TypeCol},
                whereClause, null, orderByClause);
        tet.getSortOrder();
        return tet;
    }

    // Called when a previously created loader has finished loading
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.swapCursor(data);
        if(state == WorkingStates.ListMessages) {
            msgsList.setSelection(data.getCount() - 1);
        } else if (state == WorkingStates.ListSMS) {
            //msgsList.setSelectionAfterHeaderView();
            msgsList.bShouldScrollTop = true;
        }
        //msgsList.setSelectionAfterHeaderView();
        msgsList.bShouldMarkTopItem = true;
        //tet.setFadingEdgeLength();
        //Cursor a = (Cursor)msgsList.getItemAtPosition(0);
        //MyCursorAdapter.ViewHolder tet = (MyCursorAdapter.ViewHolder)a.getTag();
        //a = a;
    }

    // Called when a previously created loader is reset, making the data unavailable
    public void onLoaderReset(Loader<Cursor> loader) {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mAdapter.swapCursor(null);
        msgsList.bShouldResetDivider = true; //to make sure divider is not resetted too soon, that is before ListView gets updated
        //msgsList.setSelectionAfterHeaderView();
    }
     ///**********************************
     //*
     //* Loader implementation end
     //*
     //**********************************/

    /**********************************
     *
     * Refresh List
     *
     *
     **********************************/
    public void refreshMsgsList()
    {
        getLoaderManager().restartLoader(0, null, this);
    }

    /**********************************
     *
     * View Binder implementation
     * Formats data queried from database to be shown in listview
     *
     **********************************/
    private SimpleCursorAdapter.ViewBinder getViewBinder() {
        return new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                if (aCursor.getColumnIndexOrThrow(SMSConsts.PhoneNumberCol) == aColumnIndex) {
                    String phoneNumber = aCursor.getString(aColumnIndex);

                    String contactName = Utils.getContactName(MainActivity.this, phoneNumber);
                    if (contactName == null || contactName.isEmpty()) {
                        return false;
                    }
                    TextView textView = (TextView) aView;
                    textView.setText(contactName);
                    return true;
                } else if (aCursor.getColumnIndexOrThrow(SMSConsts.DateCol) == aColumnIndex) {
                    Date date = new Date(aCursor.getLong(aColumnIndex));

                    String formattedDate = new SimpleDateFormat("yyyy-MM-dd H:mm", Locale.getDefault()).format(date);
                    TextView textView = (TextView) aView;
                    textView.setText(formattedDate);
                    return true;
                } else if (aCursor.getColumnIndexOrThrow(SMSConsts.MessageCol) == aColumnIndex) {
                    String msgDecrypted = null;
                    if(MainActivity.state == WorkingStates.ListMessages) {
                        String msg = aCursor.getString(aColumnIndex);
                        msgDecrypted = stringFuncs.decryptString(MainActivity.this, msg);
                        if(msgDecrypted.isEmpty()) {
                            if(selectedPhoneNumber.isEmpty() || password.isEmpty()) {
                                onBackPressed();
                            }
                        }
                    }
                    TextView textView = (TextView) aView;
                    textView.setText(msgDecrypted);
                    return true;
                }
                return false;
            }
        };

    }

    /**********************************
     *
     * On Item Click Listener for ListSMS listview
     * makes transition from ListSMS to ListMessages
     *
     **********************************/
    private AdapterView.OnItemClickListener getListItemClickListener() {
        return new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id){
                if(state == WorkingStates.ListSMS) {
                    InputPassDialogFragment dialog = new InputPassDialogFragment();
                    dialog.show(getFragmentManager(), getString(R.string.dialog_pass_tag));

                    Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                    selectedPhoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(SMSConsts.PhoneNumberCol));
                }
            }
        };
    }

    /**********************************
     *
     * On Cancel input pass dialog button click callback
     *
     *
     **********************************/
    @Override
    public void onInputPassDialogCancelClick(InputPassDialogFragment dialog) {

    }

    /**********************************
     *
     * On OK input pass dialog button click callback
     *
     *
     **********************************/
    @Override
    public void onInputPassDialogOkClick(InputPassDialogFragment dialog) {
        password = dialog.passEditBox.getText().toString();
        if(password.length() == 0) { return; }
        state = WorkingStates.ListMessages;
        refreshMsgsList();

        String contactName = Utils.getContactName(this, selectedPhoneNumber);
        if(contactName == null || contactName.isEmpty()) {
            contactName = selectedPhoneNumber;
        }
        if(getSupportActionBar() != null) {
            getSupportActionBar().setTitle(contactName);
        }
        EditText inputSMS = ((EditText) findViewById(R.id.SMSeditText));
        inputSMS.setVisibility(View.VISIBLE);

        //sendSMS.setVisibility(View.VISIBLE);
        changeSendButtonToDefault(send_addSMSButton);
        msgsList.setDivider(null);

        //as it works only on android < 4.4 kitkat, I don't care enough to implement
        //mark message as read
        /*
        ContentValues updateValues = new ContentValues(1);
        updateValues.put(SMSConsts.ReadCol, true);
        int i = MainActivity.this.getContentResolver().update(Uri.parse(SMSConsts.SMS_INBOX_URI), updateValues, SMSConsts.PhoneNumberCol + "=" + selectedPhoneNumber, null);

        if(i == 0) //above method doesnt work on android >= 4.4 kitkat
        {

        }
        */
    }

    /**********************************
     *
     * On Cancel input phone dialog button click callback
     *
     *
     **********************************/
    @Override
    public void onInputPhoneDialogCancelClick(InputPhoneDialogFragment dialog) {

    }

    /**********************************
     *
     * On OK input phone dialog button click callback
     *
     *
     **********************************/
    @Override
    public void onInputPhoneDialogOkClick(InputPhoneDialogFragment dialog) {
        String text = dialog.phoneEditText.getText().toString();
        if(text.length() > 0) {
            selectedPhoneNumber = text;
            InputPassDialogFragment inputPassDialog = new InputPassDialogFragment();
            inputPassDialog.show(getFragmentManager(), getString(R.string.dialog_pass_tag));
        }
    }

    /**********************************
     *
     * On pick contact dialog button click callback
     *
     *
     **********************************/
    @Override
    public void onInputPhoneDialogPickContactClick(InputPhoneDialogFragment dialog) {
        Intent it= new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        it.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        dialog.startActivityForResult(it, SMSConsts.REQUEST_CODE_PICK_CONTACT);
    }

    /**********************************
     *
     * On Click Send Button
     *
     **********************************/

    private View.OnClickListener getOnSendButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state == WorkingStates.ListMessages)
                {
                    String text = ((EditText) findViewById(R.id.SMSeditText)).getText().toString();
                    String textXORed = stringFuncs.encryptString(text, MainActivity.this.password);

                    sendSMS(selectedPhoneNumber, textXORed);
                    EditText inputSMS = ((EditText) findViewById(R.id.SMSeditText));
                    inputSMS.setText(null);
                } else if(state ==WorkingStates.ListSMS) {
                    InputPhoneDialogFragment dialog = new InputPhoneDialogFragment();
                    dialog.show(getFragmentManager(), getString(R.string.dialog_phone_tag));
                }
            }
        };
    }

    /**********************************
     *
     * On Preference Changed
     *
     **********************************/

    private SharedPreferences.OnSharedPreferenceChangeListener getOnSharedPreferenceChange() {
        return new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(SettingsActivity.KEY_MSG_HEADER)) {
                    MsgHeader = sharedPreferences.getString(key, SettingsActivity.MSG_HEADER_DEF);
                    refreshMsgsList();
                } else if(key.equals(SettingsActivity.KEY_DEFAULTAPP)) {
                    useDefaultApp = sharedPreferences.getBoolean(key, false);
                } else if(key.equals(SettingsActivity.KEY_THEME)) {
                    if(sharedPreferences.getBoolean(key, true)) {
                        MainActivity.activeTheme = Themes.Dark;
                    } else {
                        MainActivity.activeTheme = Themes.Light;
                    }
                    //MainActivity.this.getTheme().applyStyle(R.style.AppTheme, true);
                }
            }
        };
    }

    /**********************************
     *
     * Change view of send button so its add button
     *
     **********************************/
    public void changeSendButtonToAddButton(Button sendSMS)
    {
        int _width, _height;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // Build.VERSION_CODES.LOLLIPOP=21
            sendSMS.setText(null);
            _width=ContextCompat.getDrawable(this, R.drawable.btn_rounded_material).getIntrinsicWidth();
            _height=ContextCompat.getDrawable(this, R.drawable.btn_rounded_material).getIntrinsicHeight();
        } else {
            sendSMS.setText(R.string.plus);
            _width=ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).getIntrinsicWidth();
            _height=ContextCompat.getDrawable(this, R.drawable.ic_add_circle_outline_black_48dp).getIntrinsicHeight();
        }
        sendSMS.getLayoutParams().width = _width;
        sendSMS.getLayoutParams().height = _height;
        sendSMS.setBackgroundResource(R.drawable.btn_rounded_material);
        //sendSMS.invalidate();
    }

    /**********************************
     *
     * Change view of send button back to default
     *
     **********************************/
    private void changeSendButtonToDefault(Button sendSMS)
    {
        //sendSMS.setBackgroundDrawable(originalSendButtonDrawable);
        //ViewCompat.setBackground(sendSMS, originalSendButtonDrawable);
        sendSMS.setBackground(originalSendButtonDrawable);
        sendSMS.setText(R.string.send);
        sendSMS.getLayoutParams().width = originalSendButtonLayoutParams.width;
        sendSMS.getLayoutParams().height = originalSendButtonLayoutParams.height;
    }

    /**********************************
     *
     * sendSMS
     * sends an SMS message to another device
     *
     * credits weimenglee
     **********************************/
    private void sendSMS(String phoneNumber, String message)
    {
        if(message == null || message.length() == 0) {
            return;
        }
        String SENT = "SMS_SENT";

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0,
                new Intent(SENT), 0);

        //---when the SMS has been sent---
        registerReceiver(new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode())
                {
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        Toast.makeText(context, "Generic failure",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        Toast.makeText(context, "No service",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        Toast.makeText(context, "Null PDU",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        Toast.makeText(context, "Radio off",
                                Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));

        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

        /* //works on android < 4.4 only or if it is default sms app
        ContentValues values = new ContentValues();
        values.put("address", phoneNumber);
        values.put("body", MsgHeader + message);
        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        */
        if(!useDefaultApp) {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(phoneNumber, null, MsgHeader + message, sentPI, null);
        } else {
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse(SMSConsts.SMS_DEFAULTAPP_URI + phoneNumber));
            sendIntent.putExtra(SMSConsts.SMS_DEFAULTAPP_PHONE, phoneNumber);
            sendIntent.putExtra(SMSConsts.SMS_DEFAULTAPP_MSG, MsgHeader + message);
            startActivity(sendIntent);
        }
    }
}
