package com.bagri.common.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import static com.bagri.common.util.FileUtils.def_encoding;

public class Encryptor {

    private static final String DA_MD5 = "MD5";
    private static final String DA_SHA_256 = "SHA-256";
    
    /**
     * Encrypts provided string value
     * 
     * @param value
     * @return encrypted value or null in case of any error
     */
    public static String encrypt(String value) { 

		try {
	        MessageDigest digest = MessageDigest.getInstance(DA_MD5);
            byte[] hash = digest.digest(value.getBytes(def_encoding));
            StringBuffer hexString = new StringBuffer(2*hash.length);
            for (byte b: hash) {
                hexString.append(String.format("%02x", b&0xff));
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
    

    public static String encrypt(String value, String salt) { 

		try {
	        MessageDigest digest = MessageDigest.getInstance(DA_MD5);
	        digest.update(salt.getBytes());
	        digest.update(value.getBytes(def_encoding));
	        byte[] hash = digest.digest();	        
            StringBuffer hexString = new StringBuffer(2*hash.length);
            for (byte b: hash) {
                hexString.append(String.format("%02x", b&0xff));
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
