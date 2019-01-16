package zls.mutek.encsms;

import java.security.SecureRandom;

/**
 * Created by abara on 5/2/2017.
 * class containing Encryption/Decryption functions
 */

class StringFuncs {

    /**********************************
     *
     * encryptString
     * encrypts message that will be send as SMS
     *
     **********************************/
    String encryptString(String msg, String passEnc)
    {
        int originalLength = msg.length();
        int passEncLength = passEnc.length();

        if(originalLength <= 20) { //padding only when text is <= 20 characters long
            SecureRandom rand = new SecureRandom();
            int padding = rand.nextInt();
            if(padding < 0) { padding *= -1; } // so result is always positive value
            padding = (padding%25)+10; //padding between 10-35
            byte[] bytes = new byte[padding];
            rand.nextBytes(bytes);
            for(int i=0; i<bytes.length; i++) {
                if(bytes[i] < 0) {
                    bytes[i] *= -1; // so result is always positive value
                }
                //make sure all bytes are ascii values so when creating String all of them are used
                bytes[i] = (byte) ((bytes[i] % ('~' - '!')) + '!');
            }
            //format (xx)PADDING_BYTESmessage where xx is number of padding bytes
            msg = '(' + String.valueOf(padding) + ')' + new String(bytes) + msg;
        }
        char[] chars = new char[msg.length()];
        for(int i=0; i<msg.length(); i++) {
            chars[i] = (char)(msg.charAt(i) ^ passEnc.charAt(i%passEncLength));
            chars[i] += '!'; //so its always >= '!' and can be send in SMS
        }
        return new String(chars);
    }

    /**********************************
     *
     * decryptString
     * decrypts message received as SMS
     *
     **********************************/
    String decryptString(MainActivity activity, String msg)
    {
        String passDec = activity.password;
        if(passDec == null || passDec.length() == 0) {
            return "";
        }
        int passDecLength = passDec.length();
        int j=0, padding=0;
        if(msg.startsWith(activity.MsgHeader)) {
            j = activity.MsgHeader.length(); //header padding
        }
        if((j + 3) < msg.length() && ((msg.charAt(j) - '!') ^ passDec.charAt(0)) == '(') { //detects if
            if(((msg.charAt(j+3) - '!') ^ passDec.charAt(3%passDecLength)) == ')' ) { //random padding was applied
                char[] charNums = new char[2];
                for(int i=1; i<3; i++) { //decode number of random padding bytes
                    char tmp = msg.charAt(j+i);
                    tmp -= '!';
                    charNums[i-1] = (char)(tmp ^ passDec.charAt(i%passDecLength));
                }
                try {
                    padding = Integer.valueOf(new String(charNums));
                    padding += 4; //(xx)
                } catch(NumberFormatException e) {
                    return "";
                }
            }
        }
        char[] chars = new char[msg.length()-padding-j];

        for(int i=padding; i<msg.length()-j; i++) {
            char tmp = msg.charAt(i+j);
            tmp -= '!';
            chars[i-padding] = (char)(tmp ^ passDec.charAt(i%passDecLength));
        }
        return new String(chars);
    }
}
