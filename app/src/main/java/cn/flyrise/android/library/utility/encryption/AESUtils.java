package cn.flyrise.android.library.utility.encryption;

import android.os.Build;
import android.os.Build.VERSION;
import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author cww123
 */
public class AESUtils {

	private static final String KEY = "FE<>*^dk%73h43@7643cww123";
	private static final String KEY_P = "FEdksh437643qiyeyungongzuotai123";//android P(28)以上key
	private static final String ALGORITHM = "AES";
	private static final int KEY_SIZE = 128;
	private static final String CRYPTO = "Crypto";

	/**
	 * 生成密钥
	 */
	private static String getSecretKey(String seed) throws Exception {
		final KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
		SecureRandom secureRandom;
		if (seed != null && !"".equals(seed)) {
			if (Build.VERSION.SDK_INT > 23) {  // Android  6.0 以上
				secureRandom = SecureRandom.getInstance("SHA1PRNG", new CryptoProvider());
			}
			else if (android.os.Build.VERSION.SDK_INT >= 17) {
				secureRandom = SecureRandom.getInstance("SHA1PRNG", CRYPTO);
			}
			else {
				secureRandom = SecureRandom.getInstance("SHA1PRNG");
			}
			secureRandom.setSeed(seed.getBytes());
		}
		else {
			secureRandom = new SecureRandom();
		}
		keyGenerator.init(KEY_SIZE, secureRandom);
		final SecretKey secretKey = keyGenerator.generateKey();
		return Base64Utils.encode(secretKey.getEncoded());
	}

	/**
	 * 加密
	 */
	private static byte[] encrypt(byte[] data, String KEY) {
		SecretKeySpec secretKeySpec = null;
		try {
			final String key = getSecretKey(KEY);
			final Key k = toKey(Base64Utils.decode(key));
			final byte[] raw = k.getEncoded();
			secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
		} catch (Exception e) {
			if (android.os.Build.VERSION.SDK_INT >= 28) {
				secretKeySpec = new SecretKeySpec(KEY_P.getBytes(), "AES/CBC/PKCS5PADDING");
			}
		}
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 加密
	 */
	public static byte[] encrypt(byte[] data) {
		return encrypt(data, KEY);
	}

	/**
	 * 解密
	 */
	private static byte[] decrypt(byte[] data, String KEY) {
		SecretKeySpec secretKeySpec = null;
		try {
			final String key = getSecretKey(KEY);
			final Key k = toKey(Base64Utils.decode(key));
			final byte[] raw = k.getEncoded();
			secretKeySpec = new SecretKeySpec(raw, ALGORITHM);
		} catch (Exception e) {
			if (VERSION.SDK_INT >= 28) {
				secretKeySpec = new SecretKeySpec(KEY_P.getBytes(), "AES/CBC/PKCS5PADDING");
			}
		}
		try {
			final Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] decrypt(byte[] data) {
		return decrypt(data, KEY);
	}

	/**
	 * 转换密钥
	 */
	private static Key toKey(byte[] key) {
		final SecretKey secretKey = new SecretKeySpec(key, ALGORITHM);
		return secretKey;
	}
}
