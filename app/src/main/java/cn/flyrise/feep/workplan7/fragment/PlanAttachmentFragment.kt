package cn.flyrise.feep.workplan7.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.media.attachments.AudioPlayer
import cn.flyrise.feep.media.attachments.NetworkAttachmentListPresenter
import cn.flyrise.feep.media.attachments.NetworkAttachmentListView
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.DownloadProgress
import cn.flyrise.feep.media.attachments.downloader.DownloadConfiguration
import cn.flyrise.feep.media.attachments.listener.IAttachmentItemClickInterceptor
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback
import cn.flyrise.feep.media.attachments.listener.INetworkAttachmentItemHandleListener
import cn.flyrise.feep.workplan7.adapter.PlanAttachmentAdapter
import kotlinx.android.synthetic.main.plan_fragment_attachment.*

/**
 * author : klc
 * Msg :
 */
class PlanAttachmentFragment : Fragment(), NetworkAttachmentListView, INetworkAttachmentItemHandleListener, IDownloadProgressCallback {

    var attachments: ArrayList<Attachment>? = null
    private var canDelete: Boolean = false
    private var nestedScrollingEnabled: Boolean = false
    private var mAdapter: PlanAttachmentAdapter? = null

    private var mLoadingDialog: FELoadingDialog? = null
    private var mPresenter: NetworkAttachmentListPresenter? = null
    private var mItemHandleInterceptor: IAttachmentItemClickInterceptor? = null
    var mDelCallback: PlanAttachmentAdapter.OnItemDelCallBack? = null

    companion object {
        fun getInstance(attachments: ArrayList<Attachment>?, itemHandlerInterceptor: IAttachmentItemClickInterceptor?, nestedScrollingEnabled: Boolean, canDelete: Boolean): PlanAttachmentFragment {
            val fragment = PlanAttachmentFragment()
            fragment.attachments = attachments
            fragment.canDelete = canDelete
            fragment.mItemHandleInterceptor = itemHandlerInterceptor
            fragment.nestedScrollingEnabled = nestedScrollingEnabled
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.plan_fragment_attachment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        attachmentListView.isNestedScrollingEnabled = nestedScrollingEnabled
        attachmentListView.layoutManager = LinearLayoutManager(activity)
        val drawable = resources.getDrawable(R.drawable.ms_divider_album_item)
        val dividerDecoration = DividerItemDecoration(activity, DividerItemDecoration.VERTICAL_LIST)
        dividerDecoration.setDrawable(drawable)
        attachmentListView.addItemDecoration(dividerDecoration)
        val pathService = CoreZygote.getPathServices()
        val configuration = DownloadConfiguration.Builder().owner(CoreZygote.getLoginUserServices().userId).downloadDir(pathService.downloadDirPath).encryptDir(pathService.safeFilePath).decryptDir(pathService.tempFilePath).create()
        mPresenter = NetworkAttachmentListPresenter(activity,this, attachments, configuration)
        mAdapter = PlanAttachmentAdapter(attachments, this, this, canDelete)
        attachmentListView.adapter = mAdapter
        mAdapter!!.onItemDelCallBack = mDelCallback
    }

    override fun onAttachmentItemClick(position: Int, attachment: Attachment) {
        if (mItemHandleInterceptor == null) {
            mPresenter!!.openAttachment(attachment)
            return
        }

        if (!mItemHandleInterceptor!!.isInterceptAttachmentClick(attachment)) {
            mPresenter!!.openAttachment(attachment)
        }
    }

    override fun onAttachmentDownloadStopped(attachment: Attachment) {
        mPresenter?.stopAttachmentDownload(attachment)
    }

    override fun onAttachmentDownloadResume(attachment: Attachment) {
        mPresenter?.downloadAttachment(attachment)
    }

    override fun downloadProgress(attachment: Attachment): DownloadProgress?{
        return mPresenter?.getAttachmentDownloadProgress(attachment)
    }

    override fun attachmentDownloadProgressChange(position: Int) {
        mAdapter?.notifyItemChanged(position)
    }

    override fun decryptProgressChange(progress: Int) {
        if (mLoadingDialog == null) {
            mLoadingDialog = FELoadingDialog.Builder(activity).setCancelable(false).create()
        }
        mLoadingDialog!!.updateProgress(progress)
        mLoadingDialog!!.show()
    }

    override fun decryptFileFailed() {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }
        FEToast.showMessage("文件解密失败，请重试！")
    }

    override fun errorMessageReceive(errorMessage: String) {
        FEToast.showMessage(errorMessage)
    }

    override fun playAudioAttachment(attachment: Attachment, audioPath: String) {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }
        val player = AudioPlayer.newInstance(attachment, audioPath)
        player.show(childFragmentManager, "Audio")
    }

    override fun openAttachment(intent: Intent?) {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }

        if (intent == null) {
            FEToast.showMessage("暂不支持查看此文件类型")
            return
        }

        try {
            startActivity(intent)
        }
        catch (exp: Exception) {
            FEToast.showMessage("无法打开，建议安装查看此类型文件的软件")
        }
    }

}