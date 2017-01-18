package com.bagri.support.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Two-way encryptor, uses Cipher API.
 * 
 * @author Denis Sukhoroslov
 *
 */
public class CipherEncryptor {

	private String nonce;
	private String algo;

    private Cipher ecipher = null;
    private Cipher dcipher = null;
    
    /**
     * 
     * @param algo the algorithm to use
     * @param nonce the secret word
     */
    public CipherEncryptor(String algo, String nonce) {
    	this.algo = algo;
    	this.nonce = nonce;
    	init();
    }
	
	private void init() {
        if (ecipher == null || dcipher == null) {
            SecretKey key;
            try {
                key = new SecretKeySpec(nonce.getBytes(), algo);

                ecipher = Cipher.getInstance(algo);
                ecipher.init(Cipher.ENCRYPT_MODE, key);

                dcipher = Cipher.getInstance(algo);
                dcipher.init(Cipher.DECRYPT_MODE, key);
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
                throw new RuntimeException("Encryption disabled. Check if you have installed unlimited JCE policy", e);
            }
        }
    }

	/**
	 * Encrypts the value provided
	 * 
	 * @param toEncrypt the String to encrypt
	 * @return the encrypted String
	 */
    public String encrypt(String toEncrypt) {
        byte[] encrypted;
        try {
        	String tail = String.valueOf(System.currentTimeMillis());
        	tail = tail.substring(tail.length() - 8);
            encrypted = ecipher.doFinal((toEncrypt + tail).getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Exception while encryption", e);
        }

        return new String(encrypted); 
    }

    /**
     * Decrypts the value provided 
     * 
     * @param toDecrypt the String to decrypt
     * @return the decrypted String
     */
    public String decrypt(String toDecrypt) {
        byte[] decryptedBytes;
        try {
            decryptedBytes = dcipher.doFinal(toDecrypt.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException("Exception while decryption", e);
        }

        String decrypted = new String(decryptedBytes);
        return decrypted.substring(0, decrypted.length() - 8); 
    }
    
    private static String algo_name = "AES"; 

    /**
     * Encrypts the value provided using AES algorithm
     * 
     * @param toEncrypt the String to encrypt
     * @param nonce the secret word
     * @return the encrypted String
     */
	public static String encrypt(String toEncrypt, String nonce) {
        SecretKey key;
        try {
            key = new SecretKeySpec(nonce.getBytes(), algo_name);
            Cipher ecipher = Cipher.getInstance(algo_name);
            ecipher.init(Cipher.ENCRYPT_MODE, key);
        	String tail = String.valueOf(System.currentTimeMillis());
        	tail = tail.substring(tail.length() - 8);
            byte[] encrypted = ecipher.doFinal((toEncrypt + tail).getBytes());
            return new String(encrypted);
        } catch (Exception ex) {
        	throw new RuntimeException(ex);
        }
	}

	/**
	 * Decrypts the value provided using AES algorithm
	 * 
     * @param toDecrypt the String to decrypt
     * @param nonce the secret word
     * @return the decrypted String
	 */
	public static String decrypt(String toDecrypt, String nonce) {
        SecretKey key;
        try {
            key = new SecretKeySpec(nonce.getBytes(), algo_name);
            Cipher dcipher = Cipher.getInstance(algo_name);
            dcipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = dcipher.doFinal(toDecrypt.getBytes());
            return new String(decrypted, 0, decrypted.length - 8);
        } catch (Exception ex) {
        	throw new RuntimeException(ex);
        }
	}
	
}
