package cn.flyrise.feep.knowledge.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.media.common.AttachmentUtils;
import cn.squirtlez.frouter.FRouter;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.SupportsAttachments;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;

import static cn.flyrise.feep.core.common.X.Func.Knowledge;

/**
 * Created by klc
 * 打开知识中心辅助工具
 */

public class KnowledgeUtil {

	public static boolean isNewVersion() {
		return FunctionManager.isNative(Func.Knowledge);
	}


	public static void openReceiverFileActivity(String messageID, String fileID, Context context) {
		FRouter.build(context, "/x5/browser")
				.withString("businessId", fileID)
				.withString("messageId", messageID)
				.withInt("moduleId", Knowledge)
				.go();
	}

	public static void openFile(Context context, File file, String fileName) {
		try {
			String fileType = "*/*";
			if (fileName.contains(".")) {
				final int lastIndex = fileName.lastIndexOf(".");
				final String type = fileName.substring(lastIndex);
				if (CommonUtil.checkArray(type, SupportsAttachments.imglastArray)) {// 查看图片
					fileType = "image/*";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.doclastArray)) {// 查看文档
					fileType = "application/msword";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.pdfTypeArray)) {// 查看pdf
					fileType = "application/pdf";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.packageTypeArray)) {// 打开安装包文件
					fileType = "application/vnd.android.package-archive";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.rarTypeArray)) {// 打开rar文件
					fileType = "application/rar";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.zipTypeArray)) {// 打开zip文件
					fileType = "application/zip";
				}
				else if (CommonUtil.checkArray(type, SupportsAttachments.reclastArray)) {// 查看音频附件
//                    new FEMaterialMusicDialog.Builder(context).setMusicFile(safeFilePath).setMusicTitle(fileName).build().show();
					return;
				}
				else {
					FEToast.showMessage(context.getResources().getString(R.string.check_attachment_no_format));
				}
				Intent intent = AttachmentUtils.getIntent(context,file.getPath(),fileType);
				context.startActivity(intent);
			}
			else {
				FEToast.showMessage(context.getResources().getString(R.string.check_attachment_no_format));
			}
		} catch (final Exception e) {
			FEToast.showMessage(context.getResources().getString(R.string.attachmentcontrolview_prompt_install_soft));
			e.printStackTrace();
		}
	}

	public static boolean isPicType(String fileType) {
		List<String> PicType = Arrays.asList(".jpg", ".bmp", ".png", ".gif", ".jpeg");
		return PicType.contains(fileType);
	}


	public static boolean isDocType(String fileType) {
		List<String> PicType = Arrays.asList(".doc", ".docx", ".txt", ".log", ".pdf", ".ppt", ".xls", ".html", ".xlsx", ".htm", ".pptx", ".vsd", ".swf", ".dot");
		return PicType.contains(fileType);
	}
}
