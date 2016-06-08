package com.bagri.common.security;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
//import java.util.Base64;

public class CipherEncryptor {

	private String nonce;
	private String algo;

    private Cipher ecipher = null;
    private Cipher dcipher = null;
    
    public CipherEncryptor(String algo, String nonce) {
    	this.algo = algo;
    	this.nonce = nonce;
    	init();
    }
	
	public void init() {
        if (ecipher == null || dcipher == null) {
            SecretKey key;
            try {
                key = new SecretKeySpec(nonce.getBytes(), algo);

                ecipher = Cipher.getInstance(algo);
                ecipher.init(Cipher.ENCRYPT_MODE, key);

                dcipher = Cipher.getInstance(algo);
                dcipher.init(Cipher.DECRYPT_MODE, key);
            } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException e) {
                throw new RuntimeException("Encryption disabled. Check if you  have installed unlimited JCE policy", e);
            }
        }
    }

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
