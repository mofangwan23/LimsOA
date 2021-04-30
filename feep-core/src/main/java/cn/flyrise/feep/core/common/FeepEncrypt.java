package cn.flyrise.feep.core.common;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.services.ISecurity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ZYP
 * @since 2017-02-07 11:21
 */
public class FeepEncrypt extends ISecurity.BaseSecurity implements ISecurity.IEncrypt {

	@Override public void encrypt(final String filePath, final ISecurity.IEncryptListener listener) {
		final String fileName = filePath.substring(filePath.lastIndexOf("/"), filePath.length());
		final String targetPath = CoreZygote.getPathServices().getSafeFilePath() + fileName;
		final File srcFile = new File(filePath);
		if (!srcFile.exists() || isEncrypt(filePath)) {
			listener.onEncryptSuccess(filePath);
			return;
		}

		// TODO 如果不支持加密，直接把文件移动过去就行了

		new Thread(new Runnable() {
			@Override public void run() {
				boolean hasError = false;
				final File encryptFile = new File(targetPath);
				final File parentFile = encryptFile.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}

				if (encryptFile.exists()) {
					if (listener != null) {
						handler.post(() -> listener.onEncryptSuccess(filePath));
					}
					return;
				}

				InputStream in;
				try {
					encryptFile.createNewFile();
					in = new FileInputStream(filePath);
					OutputStream out = new FileOutputStream(encryptFile);
					byte[] buffer = new byte[1024];
					int r;
					final byte[] buffer2 = new byte[1024];
					while ((r = in.read(buffer)) > 0) {
						for (int i = 0; i < r; i++) {
							byte b = buffer[i];
							buffer2[i] = b == 255 ? 0 : ++b;
						}
						out.write(buffer2, 0, r);
					}

					out.write(ISecurity.SECURITY_KEY.getBytes());
					out.flush();

					in.close();
					out.close();
				} catch (final Exception e) {
					hasError = true;
				} finally {
					if (listener != null) {
						if (hasError) {
							handler.post(() -> listener.onEncryptFailed(filePath));
						}
						else {
							handler.post(() -> listener.onEncryptSuccess(filePath));
						}
					}
				}
			}
		}).start();
	}
}
