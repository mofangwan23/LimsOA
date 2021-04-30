package cn.flyrise.feep.media.attachments.downloader;

/**
 * @author ZYP
 * @since 2017-10-31 14:54
 */
public class DownloadConfiguration {

	private final String owner;               // 仓库主人（userId）
	private final String downloadDir;         // 下载路径 /feep/userId/TEMPDIR
	private final String decryptDir;          // 解密路径（文件解密后所在的路径） /feep/userId/TEMPFILE
	private final String encryptDir;          // 加密路径（文件加密后所在的路径） /feep/userId/SAFEFILE

	private DownloadConfiguration(Builder builder) {
		this.owner = builder.owner;
		this.decryptDir = builder.decryptDir;
		this.downloadDir = builder.downloadDir;
		this.encryptDir = builder.encryptDir;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getDownloadDir() {
		return this.downloadDir;
	}

	public String getDecryptDir() {
		return this.decryptDir;
	}

	public String getEncryptDir() {
		return this.encryptDir;
	}

	public static class Builder {

		private String owner;
		private String downloadDir;
		private String decryptDir;
		private String encryptDir;

		public Builder owner(String owner) {
			this.owner = owner;
			return this;
		}

		public Builder downloadDir(String downloadDir) {
			this.downloadDir = downloadDir;
			return this;
		}

		public Builder decryptDir(String decryptDir) {
			this.decryptDir = decryptDir;
			return this;
		}

		public Builder encryptDir(String encryptDir) {
			this.encryptDir = encryptDir;
			return this;
		}

		public DownloadConfiguration create() {
			return new DownloadConfiguration(this);
		}
	}

}
