package cn.flyrise.android.library.utility.encryption;

import java.security.Provider;

/**
 * Create by cm132 on 2019/5/30 16:31.
 * Describe:兼容7.0系统以后crypto被移除，导致AES异常的问题
 */
public class CryptoProvider extends Provider {

	/**
	 * Creates a Provider and puts parameters
	 */
	public CryptoProvider() {
		super("Crypto", 1.0, "HARMONY (SHA1 digest; SecureRandom; SHA1withDSA signature)");
		put("SecureRandom.SHA1PRNG",
				"org.apache.harmony.security.provider.crypto.SHA1PRNG_SecureRandomImpl");
		put("SecureRandom.SHA1PRNG ImplementedIn", "Software");
	}
}
