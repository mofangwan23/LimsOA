package cn.flyrise.feep.core.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.services.ISecurity;

/**
 * @author ZYP
 * @since 2017-02-07 11:21
 */
public class FeepDecrypt extends ISecurity.BaseSecurity implements ISecurity.IDecrypt {

	private int progress = 0;

	@Override public void decrypt(final String filePath, final ISecurity.IDecryptListener listener) {
		final String fileName = filePath.substring(filePath.lastIndexOf("/"), filePath.length());
		final String targetPath = CoreZygote.getPathServices().getTempFilePath() + fileName;
		decrypt(filePath, targetPath, listener);
	}

	public void decrypt(final String filePath, final String targetPath, final ISecurity.IDecryptListener listener) {
		final File srcFile = new File(filePath);
		if (!srcFile.exists() || !isEncrypt(filePath)) {
			if (listener != null) {
				listener.onDecryptFailed();
			}
			return;
		}

		new Thread(() -> {
			boolean hasError = false;
			final File decryptFile = new File(targetPath);
			if (!decryptFile.getParentFile().exists()) {
				decryptFile.getParentFile().mkdirs();
			}
			try {
				InputStream is = new FileInputStream(srcFile);
				OutputStream out = new FileOutputStream(targetPath);
				byte[] buffer = new byte[1024];
				byte[] buffer2 = new byte[1024];
				byte bMax = (byte) 255;
				long size = srcFile.length() - ISecurity.SECURITY_KEY.length();
				long encryptsize = 0;
				int mod = (int) (size % 1024);
				int div = (int) (size >> 10);
				int count = mod == 0 ? div : (div + 1);
				int k = 1, r;
				while ((k <= count && (r = is.read(buffer)) > 0)) {
					if (mod != 0 && k == count) {
						r = mod;
					}
					for (int i = 0; i < r; i++) {
						byte b = buffer[i];
						buffer2[i] = b == 0 ? bMax : --b;
					}
					out.write(buffer2, 0, r);
					k++;

					encryptsize += r;
					int nowProgress = (int) ((100 * encryptsize) / size);
					if (nowProgress > progress) {
						progress = nowProgress;
						if (listener != null) {
							handler.post(() -> listener.onDecryptProgress(progress));
						}
					}
				}
				out.close();
				is.close();
				hasError = false;
			} catch (Exception e) {
				hasError = true;
			} finally {
				if (listener != null) {
					if (hasError) {
						handler.post(listener::onDecryptFailed);
					}
					else {
						decryptFile.setLastModified(srcFile.lastModified());    // 将源文件的修改时间设置到解密文件上，确保两个文件是同一个东西
						handler.post(() -> listener.onDecryptSuccess(decryptFile));
					}
				}
			}
		}).start();
	}
}
