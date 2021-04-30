package cn.flyrise.feep.media.common;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.ISecurity.BaseSecurity;
import cn.flyrise.feep.media.BuildConfig;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-10-27 15:23
 */
public class AttachmentUtils {

	private static final String REGEX_ENCRYPT_CHECK = "(/\\w+)+/feep/\\w+/SAFEFILE(/\\S+)+";
	private static final String REGEX_FIX_ERROR_CHAR = "/|\\\\|\\*|\\?|<|>|\\\\d|\\|";
	private static final String AUTHORITY = "com.flyrise.study.media.attachment.provider";

	/**
	 * 检查是否是加密文件、目前 SAFEFILE 目录下全是加密文件
	 */
	public static boolean isEncryptFile(String path) {
		if (path.matches(REGEX_ENCRYPT_CHECK)) {
			return true;
		}

		return BaseSecurity.isEncrypt(path);
	}

	/**
	 * 从加密文件的文件名中解析出来 taskId 跟 MD5 加密过的文件名
	 * @param fileName 加密文件的文件名【格式:(taskId)MD5[fileName]】
	 */
	public static String[] parseTaskIdAndStorageName(String fileName) {
		if (fileName.startsWith("(") && fileName.contains(")")) {
			String[] results = new String[2];
			int index = fileName.lastIndexOf(')');
			results[0] = fileName.substring(1, index);
			results[1] = fileName.substring(index + 1, fileName.length());
			return results;
		}
		return null;
	}

	/**
	 * 检查是否图片附件
	 */
	public static boolean isImageAttachment(Attachment attachment) {
		if (attachment == null) return false;
		return TextUtils.equals(attachment.type, FileCategoryTable.TYPE_IMAGE + "");
	}

	/**
	 * 检查是否音频附件,用户刚刚录制的文件
	 */
	public static boolean isTempAudioAttachment(Attachment attachment) {
		return attachment != null && !TextUtils.isEmpty(attachment.path)
				&& TextUtils.equals(attachment.type, FileCategoryTable.TYPE_AUDIO + "")
				&& attachment.path.contains(CoreZygote.getPathServices().getTempFilePath());
	}

	/**
	 * 检查临时录音文件
	 */
	public static boolean isAudioAttachment(Attachment attachment) {
		if (attachment == null) return false;
		return TextUtils.equals(attachment.type, FileCategoryTable.TYPE_AUDIO + "");
	}

	/**
	 * 检查附件是否是关联事项
	 */
	public static boolean isAssociateMatters(Attachment attachment) {
		if (attachment == null) return false;
		return TextUtils.equals(attachment.type, FileCategoryTable.TYPE_COLLABORATION + "")
				|| TextUtils.equals(attachment.type, FileCategoryTable.TYPE_MEETING + "");
	}

	/**
	 * 修复附件 ID
	 */
	public static String fixAttachmentId(String attachmentId) {
		if (TextUtils.isEmpty(attachmentId)) {
			return attachmentId;
		}
		return attachmentId.replaceAll(REGEX_FIX_ERROR_CHAR, "");
	}

	/**
	 * 对附件名字进行加密 (taskId)MD5(name)
	 */
	public static String encryptAttachmentName(String attachmentId, String attachmentName) {
		return "(" + fixAttachmentId(attachmentId) + ")" + CommonUtil.getMD5(attachmentName);
	}

	/**
	 * 根据指定 Attachment 从 SAFEFILE 目录下获取已经下载成功的附件
	 */
	public static File getDownloadedAttachment(Attachment attachment) {
		if (!(attachment instanceof NetworkAttachment)) {
			return null;
		}

		String encryptAttachmentPath = CoreZygote.getPathServices().getSafeFilePath()
				+ File.separator + encryptAttachmentName(attachment.getId(), attachment.name);

		File attachmentFile = new File(encryptAttachmentPath);
		if (attachmentFile.exists()) {
			return attachmentFile;
		}

		return null;
	}

	public static String getAttachmentFileType(int type) {
		String fileType = null;
		switch (type) {
			case FileCategoryTable.TYPE_IMAGE:
				fileType = "image/*";
				break;
			case FileCategoryTable.TYPE_TXT:
			case FileCategoryTable.TYPE_WORD:
			case FileCategoryTable.TYPE_EXCEL:
			case FileCategoryTable.TYPE_PPT:
				fileType = "application/msword";
				break;
			case FileCategoryTable.TYPE_PDF:
				fileType = "application/pdf";
				break;
			case FileCategoryTable.TYPE_ZIP:
				fileType = "application/zip";
				break;
		}

		return fileType;
	}

	/**
	 * 获取跳转intent,兼容Android N
	 * @param context
	 * @param filepath
	 * @param fileType
	 * @return
	 */
	public static Intent getIntent(Context context,String filepath,String fileType){
		File file = new File(filepath);
		if (!file.exists()){
			return null;
		}
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Uri uri;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			uri = FileProvider.getUriForFile(context, "cn.flyrise.study.media.feprovider",
					new File(filepath));
			intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		}else{
			uri = Uri.fromFile(new File(filepath));
		}
		intent.setDataAndType(uri, fileType);
		return intent;
	}
}
