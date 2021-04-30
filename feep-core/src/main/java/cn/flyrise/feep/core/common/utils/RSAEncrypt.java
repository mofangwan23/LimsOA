package cn.flyrise.feep.core.common.utils;

import android.text.TextUtils;
import android.util.Base64;
import cn.flyrise.feep.core.CoreZygote;
import java.io.ByteArrayOutputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;

/**
 * Create by cm132 on 2018/12/19.
 * Describe:
 */
public class RSAEncrypt {

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;


	/**
	 * 将base64编码后的公钥字符串转成PublicKey实例
	 * @param publicKey 公钥字符
	 * @return publicKEY
	 * @throws Exception exception
	 */
	private static PublicKey getPublicKey(String publicKey) throws Exception {
		byte[] keyBytes = decode(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(keySpec);
	}

	/**
	 * 将base64编码后的私钥字符串转成PrivateKey实例
	 * @param privateKey 私钥字符串
	 * @return 私钥对象
	 * @throws Exception exception
	 */
	private static PrivateKey getPrivateKey(String privateKey) throws Exception {
		byte[] keyBytes = decode(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePrivate(keySpec);
	}

	/**
	 * RSA加密
	 * @param content 待加密文本
	 * @return 密文
	 * @throws Exception exception
	 */
	public static String encrypt(String content) throws Exception {
		String key = CoreZygote.getRsaService().getPublicKey();
		if (TextUtils.isEmpty(key) || TextUtils.isEmpty(content)) return content;
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, getPublicKey(key));
		byte[] data = content.getBytes();
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encode(encryptedData);
	}

	/**
	 * RSA解密
	 * @param content 密文
	 * @param privateKey 私钥
	 * @return 明文
	 * @throws Exception exception
	 */
	public static String decrypt(String content, PrivateKey privateKey) throws Exception {
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		byte[] encryptedData = decode(content);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return new String(decryptedData);
	}

	public static byte[] decode(String base64) {
		return Base64.decode(base64, Base64.NO_WRAP);
	}

	public static String encode(byte[] bytes) {
		return new String(Base64.encode(bytes, Base64.NO_WRAP));
	}
}
