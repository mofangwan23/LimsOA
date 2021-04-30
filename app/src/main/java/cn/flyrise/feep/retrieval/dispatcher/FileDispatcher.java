package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import android.content.Intent;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.attachments.AttachmentViewer;
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration;
import cn.flyrise.feep.media.attachments.listener.SimpleAttachmentViewerListener;
import cn.flyrise.feep.retrieval.bean.FileRetrieval;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.squirtlez.frouter.FRouter;

/**
 * @author ZYP
 * @since 2018-05-03 11:39
 */
public class FileDispatcher implements RetrievalDispatcher {

	private AttachmentViewer mAttachmentViewer;

	@Override public void jumpToSearchPage(Context context, String keyword) {
		FRouter.build(context, "/knowledge/search")
				.withInt("EXTRA_FOLDERTYPES", 588)         // TODO 暂时默认搜个人文件夹
				.withString("keyword", keyword)
				.go();
	}

	@Override public void jumpToDetailPage(Context context, Retrieval retrieval) {
		LoadingHint.show(context);
		if (mAttachmentViewer == null) {
			DownloadConfiguration configuration = new DownloadConfiguration.Builder()
					.owner(CoreZygote.getLoginUserServices().getUserId())
					.downloadDir(CoreZygote.getPathServices().getKnowledgeCachePath())
					.encryptDir(CoreZygote.getPathServices().getSafeFilePath())
					.decryptDir(CoreZygote.getPathServices().getTempFilePath())
					.create();

			mAttachmentViewer = new AttachmentViewer(context, configuration);
			mAttachmentViewer.setAttachmentViewerListener(new XSimpleAttachmenntViewerListener(context));
		}

		FileRetrieval fileRetrieval = (FileRetrieval) retrieval;
		mAttachmentViewer.openAttachment(fileRetrieval.url, fileRetrieval.businessId, fileRetrieval.filename);
	}

	private class XSimpleAttachmenntViewerListener extends SimpleAttachmentViewerListener {

		private Context context;

		public XSimpleAttachmenntViewerListener(Context context) {
			this.context = context;
		}

		@Override public void prepareOpenAttachment(Intent intent) {
			LoadingHint.hide();
			if (intent == null) {
				FEToast.showMessage("暂不支持查看此文件类型");
				return;
			}

			try {
				this.context.startActivity(intent);
			} catch (Exception exp) {
				FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
			}
		}

		@Override public void onDownloadProgressChange(int progress) {
			LoadingHint.showProgress(progress, CommonUtil.getString(R.string.know_downloading));
		}

		@Override public void onDownloadFailed() {
			FEToast.showMessage(CommonUtil.getString(R.string.know_open_fail));
			LoadingHint.hide();
		}

		@Override public void onDecryptFailed() {
			FEToast.showMessage(CommonUtil.getString(R.string.know_open_fail));
			LoadingHint.hide();
		}

		@Override public void onDecryptProgressChange(int progress) {
			LoadingHint.showProgress(progress, CommonUtil.getString(R.string.know_decode_open));
		}
	}
}
