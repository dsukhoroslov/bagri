package com.bagri.common.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//import com.sun.xml.internal.messaging.saaj.util.Base64;
//import com.sun.org.apache.xml.internal.security.utils.Base64;

public class Encryptor {

    private static final String DA_MD5 = "MD5";
    private static final String DA_SHA_256 = "SHA-256";
    
    private static final String EN_UTF8 = "UTF-8";

    /**
     * Encrypts provided string value
     * 
     * @param value
     * @return encrypted value or null in case of any error
     */
    public static String encrypt(String value) { 

        byte[] buffer;
		try {
			buffer = value.getBytes(EN_UTF8);

	        MessageDigest digest = MessageDigest.getInstance(DA_MD5);
            digest.reset();
            digest.update(value.getBytes());
            byte[] dbytes = digest.digest();
            //String result = Base64.encode(dbytes);
            //buffer = Base64.encode(dbytes);
            //return new String(buffer); // result;

            StringBuffer hexString = new StringBuffer(dbytes.length);
            for (int i=0; i < dbytes.length; i++) {
                String hex = Integer.toHexString(0xFF & dbytes[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			//e.printStackTrace();
			return null;
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
			return null;
		}
    }
    
}
