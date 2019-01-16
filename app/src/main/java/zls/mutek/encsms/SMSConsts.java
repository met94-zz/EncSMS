package zls.mutek.encsms;

/**
 * Created by abara on 5/2/2017.
 * class containing const values
 * this may need refactoring as some of these consts may depend on android api
 * they should be imported from sdk
 */

final class SMSConsts {
    final class SMSTypes {
        static final int TYPE_INBOX = 1;
        static final int TYPE_OUTBOX = 2;
    }

    static final String PhoneNumberCol = "address";
    static final String IdCol = "_id";
    static final String MessageCol = "body";
    static final String DateCol = "date";
    static final String TypeCol = "type";
    static final String READ_SMS_PERM = "android.permission.READ_SMS";
    static final String SEND_SMS_PERM = "android.permission.SEND_SMS";
    static final String READ_CONTACTS_PERM = "android.permission.READ_CONTACTS";
    static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    static final String SMS_URI = "content://sms";
    static final String SMS_DEFAULTAPP_URI = "sms:";
    static final String SMS_DEFAULTAPP_PHONE = "address";
    static final String SMS_DEFAULTAPP_MSG = "sms_body";
    static final String ORDER_BY_SMS = "date DESC";
    static final String ORDER_BY_MSGS = "date ASC";
    static final String WHERE_CLAUSE_PHONE_NUMBER_SMS = PhoneNumberCol + " LIKE ";
    static final String WHERE_CLAUSE_TYPE = "( " + TypeCol + "=" + SMSTypes.TYPE_INBOX + " OR " + TypeCol + "=" + SMSTypes.TYPE_OUTBOX + " )";
    static final int REQUEST_CODE_ASK_PERMISSIONS = 300;
    static final int REQUEST_CODE_PICK_CONTACT = 320;
}
