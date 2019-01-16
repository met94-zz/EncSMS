package zls.mutek.encsms;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ListView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by abara on 5/2/2017.
 * utilities class
 */

final class Utils {

    static final float[] NEGATIVE_COLORFILTER = {
            -1.0f,     0,     0,    0, 255, // red
            0, -1.0f,     0,    0, 255, // green
            0,     0, -1.0f,    0, 255, // blue
            0,     0,     0, 1.0f,   0  // alpha
    };

/*
    public static void showAllCNames (Context context){
        Uri uri = Uri.parse("content://sms/");
        final Cursor cur = context.getContentResolver().query(uri, null, null, null, null);
        if(cur != null) {
            for (String s : cur.getColumnNames()) {
                Log.d("COLUMN_NAME", s);
            }
            if(!cur.isClosed()) {
                cur.close();
            }
        }
    }
*/

    static String getContactName(Context context, String phoneNumber) {
        if(ContextCompat.checkSelfPermission(context, SMSConsts.READ_CONTACTS_PERM) == PackageManager.PERMISSION_DENIED) {
            return null;
        }
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));
        Cursor cursor = context.getContentResolver().query(uri,
                new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME }, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
        return contactName;
    }

    /**********************************
     *
     * askPermissions
     * asks for missing permissions
     *
     **********************************/
    static boolean askPermissions(Activity activity, String[] perms)
    {
        int i=0;
        for(int j=0; j<perms.length; j++) {
            if(ContextCompat.checkSelfPermission( activity, perms[j]) == PackageManager.PERMISSION_DENIED) {
                perms[i++] = perms[j];
            }
        }

        String[] permsToAsk = Arrays.copyOf(perms, i);

        if(permsToAsk.length == 0) {
            return true;
        }

        ActivityCompat.requestPermissions(activity, permsToAsk, SMSConsts.REQUEST_CODE_ASK_PERMISSIONS);
        return false;
    }

    static void setDefaultDivider(ListView list)
    {
        int[] colors = {0, 0xFFFF0000, 0}; // 0xFFFF0000 stands for red color
        list.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
        list.setDividerHeight(2);
    }

    static boolean checkPreferencesSecurityKey(MainActivity activity)
    {
        String KEY_SECURITYKEY = activity.getString(R.string.generalPreferences_securityKeyKey);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String securityKey = sharedPreferences.getString(KEY_SECURITYKEY, null);
        if(securityKey == null || securityKey.length() < 12) {
            return false;
        } else {
            String newSecurityKey = getKeyHash(getEncryptedIMEI(activity));
            if(!newSecurityKey.contains(securityKey)) {
                sharedPreferences.edit().putString(KEY_SECURITYKEY, null).apply();
                return false;
            }
        }
        return true;
    }

    static String getKeyHash(String imei)
    {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] byteHash = digest.digest(imei.getBytes());
            String keyHash = "";
            //for(byte b : byteHash) {
            for(int i=0; i<20; i++) {
                byte b = byteHash[i];
                keyHash += String.format("%1$02X", b);
            }
            return keyHash;
        } catch(NoSuchAlgorithmException e) {
            return "";
        }
    }

    static String getEncryptedIMEI(Context context)
    {
        String result = "";
        final int xorVal = 0x22;
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String key = telephonyManager.getDeviceId();
        if(key == null || key.length() < 5) {
            key = "PRO8BL3M";
        }
        for(int i=0; i<key.length() && key.charAt(i) != '\0'; i++)
        {
            result += String.format("%1$02X", key.charAt(i) ^ xorVal);;
        }
        return result;
    }
}
