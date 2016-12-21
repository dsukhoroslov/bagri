package com.bagri.support.security;

import static com.bagri.support.util.FileUtils.def_encoding;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple one-way encryptor. Encrypts String value using MD5 algorithm.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class Encryptor {

    private static final String DA_MD5 = "MD5";
    private static final String DA_SHA_256 = "SHA-256";
    
    /**
     * Encrypts the provided value using MD5 algorithm.
     * 
     * @param value the value to encrypt
     * @return the encrypted value
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
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
    }
    
	/**
     * Encrypts the provided value using MD5 algorithm.
	 * 
	 * @param value the value to encrypt
	 * @param salt the secret word to use as a salt
	 * @return the encrypted value
	 */
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
			return null;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
    }
}
