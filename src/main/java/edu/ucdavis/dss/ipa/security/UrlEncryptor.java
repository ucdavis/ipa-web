package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by okadri on 9/16/16.
 */

public class UrlEncryptor {
    public static String encrypt(String salt, String IpAddress) {
        try {
            Date now = new Date();
            String timestamp = now.toString();
            String unencrypted = StringUtils.join(new String[] { timestamp, IpAddress }, ", ");

            IvParameterSpec iv = new IvParameterSpec(salt.getBytes("UTF-8"));

            String secret = SettingsConfiguration.getDownloadSecretKey();
            SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes("UTF-8"),
                    "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            byte[] encrypted = cipher.doFinal(unencrypted.getBytes());

            return Base64.encodeBase64URLSafeString(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static List<String> decrypt(String salt, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(salt.getBytes("UTF-8"));

            String secret = SettingsConfiguration.getDownloadSecretKey();
            SecretKeySpec skeySpec = new SecretKeySpec(secret.getBytes("UTF-8"),
                    "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            String unencrypted = new String(original);
            return Arrays.asList(unencrypted.split(", "));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean validate(String salt, String encrypted, String ipAddress, long timeout) throws ParseException {
        long reqTime = 0;
        Date now = new Date();
        String reqIpAddress = "";
        List<String> decrypted = UrlEncryptor.decrypt(salt, encrypted);

        if (decrypted != null) {
            String reqStrTimeStamp = decrypted.get(0);
            reqIpAddress = decrypted.get(1);
            Date reqTimeStamp = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy").parse(reqStrTimeStamp);
            reqTime = reqTimeStamp.getTime();
        }

        long timeDiff = Math.abs((now.getTime() - reqTime)/1000);

        return timeDiff < timeout && ipAddress.equals(reqIpAddress);
    }

}
