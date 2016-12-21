package com.bagri.common.security;

import static org.junit.Assert.*;

import org.junit.Test;

import com.bagri.support.security.CipherEncryptor;
import com.bagri.support.security.Encryptor;

public class EncryptorTest {

	@Test
	public void testEncrypt() {
		String password = "password";
		String pwd1 = Encryptor.encrypt(password);
		String pwd2 = Encryptor.encrypt(password);
		assertEquals(pwd1, pwd2);
		String salt = "12345";
		pwd1 = Encryptor.encrypt(password, salt);
		pwd2 = Encryptor.encrypt(password, salt);
		assertEquals(pwd1, pwd2);
	}
	
	@Test
	public void testCipher() {
		
		CipherEncryptor ce = new CipherEncryptor("AES", "testtesttesttest");

		String password = "password";
		String pwd1 = ce.encrypt(password);
		String pwd2 = ce.decrypt(pwd1);
		assertEquals(password, pwd2);
		
	}

	@Test
	public void testCipher2() throws Exception {
		String password = "password";
		String nonce = "testtesttesttest";
		String pwd1 = CipherEncryptor.encrypt(password, nonce);
		//Thread.sleep(20);
		String pwd2 = CipherEncryptor.encrypt(password, nonce);
		//assertNotEquals(pwd1, pwd2);
		assertEquals(password, CipherEncryptor.decrypt(pwd1, nonce));
		assertEquals(password, CipherEncryptor.decrypt(pwd2, nonce));
	}
	
}
