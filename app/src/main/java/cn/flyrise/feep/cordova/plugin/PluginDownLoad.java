package cn.flyrise.feep.cordova.plugin;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.AudioPlayer;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * 类描述：
 * @author 罗展健
 * @version 1.0
 */
public class PluginDownLoad extends CordovaPlugin {

	private static final String ACTION = "FileDownload";
	private AttachmentViewer mViewer;

	public PluginDownLoad() {
		DownloadConfiguration configuration = new DownloadConfiguration.Builder()
				.owner(CoreZygote.getLoginUserServices().getUserId())
				.downloadDir(CoreZygote.getPathServices().getDownloadDirPath())
				.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
				.decryptDir(CoreZygote.getPathServices().getTempFilePath())
				.create();
		mViewer = new AttachmentViewer(CoreZygote.getContext(), configuration);
		mViewer.setAttachmentViewerListener(new XSimpleAttachmentViewerListener());
	}

	@Override public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		if (action.equals(ACTION)) {
			String downloadUrl = args.getString(0);
			String fileName = args.getString(1);
			String id = args.getString(2);
			String fileType = args.getString(3);
			fileName = fileName.length() > 40 ? fileName.substring(0, 40) + fileType : fileName;
			mViewer.openAttachment(downloadUrl, id, fileName);
		}
		return true;
	}

	private class XSimpleAttachmentViewerListener extends SimpleAttachmentViewerListener {

		@Override public void preparePlayAudioAttachment(Attachment attachment, String attachmentPath) {
			FragmentActivity activity = (FragmentActivity) cordova.getActivity();
			AudioPlayer player = AudioPlayer.newInstance(attachment, attachmentPath);
			player.show(activity.getSupportFragmentManager(), "Audio");
		}

		@Override public void onDownloadBegin(TaskInfo taskInfo) {
			FEToast.showMessage("正在下载，请稍等");
		}

		@Override public void prepareOpenAttachment(Intent intent) {
			if (cordova == null) return;
			final Context context = cordova.getActivity();
			if (((Activity) context).isFinishing()) return;
			final Builder builder = new Builder(cordova.getActivity());
			builder.setTitle(context.getResources().getString(R.string.dialog_default_title));
			builder.setMessage(context.getResources().getString(R.string.downlowned_or_see));
			builder.setPositiveButton(context.getResources().getString(R.string.dialog_button_ok), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (intent == null) {
						FEToast.showMessage("暂不支持查看此文件类型");
						return;
					}

					try {
						cordova.getActivity().startActivity(intent);
					} catch (Exception exp) {
						FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
					}
				}
			});
			builder.setNegativeButton(context.getResources().getString(R.string.dialog_default_cancel_button_text), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			builder.create().show();
		}

		@Override public void onDownloadFailed() {
			FEToast.showMessage(cordova.getActivity().getResources().getString(R.string.util_download_failed));
		}
	}
}
