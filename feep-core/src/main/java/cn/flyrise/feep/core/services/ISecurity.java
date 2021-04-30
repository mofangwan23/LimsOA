package cn.flyrise.feep.core.services;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author ZYP
 * @since 2017-02-07 11:17
 */
public interface ISecurity {

	/**
	 * 用于加密的字符串，不能改，一改全 GG ...
	 * 服务端居然也有这个东西
	 */
	String SECURITY_KEY = "FE<>*^dk%73h43@7643cww123";

	interface IEncrypt {

		void encrypt(String filePath, IEncryptListener listener);
	}

	interface IEncryptListener {

		void onEncryptSuccess(String filePath);

		void onEncryptFailed(String filePath);
	}

	interface IDecrypt {

		void decrypt(String filePath, IDecryptListener listener);
	}

	interface IDecryptListener {

		void onDecryptSuccess(File decryptedFile);

		void onDecryptProgress(int progress);

		void onDecryptFailed();
	}

	class DecryptListenerAdapter implements IDecryptListener {

		@Override public void onDecryptSuccess(File decryptedFile) { }

		@Override public void onDecryptProgress(int progress) { }

		@Override public void onDecryptFailed() { }
	}

	class BaseSecurity {

		protected final Handler handler = new Handler(Looper.getMainLooper());

		public static boolean isEncrypt(String filePath) {
			return isEncryptFile(new File(filePath));
		}

		public static boolean isEncryptFile(File file) {
			return TextUtils.equals(ISecurity.SECURITY_KEY, readFileLastByte(file));
		}

		private static String readFileLastByte(File file) {
			if (!file.exists()) {
				return "";
			}

			StringBuilder str = new StringBuilder();
			try {
				RandomAccessFile randomFile = new RandomAccessFile(file, "r");
				long fileLength = randomFile.length();
				int keyLength = ISecurity.SECURITY_KEY.length();
				for (int i = keyLength; i >= 1; i--) {
					randomFile.seek(fileLength - i);
					str.append((char) randomFile.read());
				}
				randomFile.close();
				return str.toString();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return "";
		}
	}
}
