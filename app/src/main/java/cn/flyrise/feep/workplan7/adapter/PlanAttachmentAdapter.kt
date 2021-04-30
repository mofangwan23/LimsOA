package cn.flyrise.feep.workplan7.adapter

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.attachments.listener.IDownloadProgressCallback
import cn.flyrise.feep.media.attachments.listener.INetworkAttachmentItemHandleListener
import cn.flyrise.feep.media.common.AttachmentUtils
import cn.flyrise.feep.media.common.FileCategoryTable
import kotlinx.android.synthetic.main.plan_view_attachment_item.view.*

/**
 * author : klc
 * Msg :
 */
class PlanAttachmentAdapter(
	attachment: ArrayList<Attachment>?, progressCallback: IDownloadProgressCallback?,
	handleListener: INetworkAttachmentItemHandleListener?,
	showDelete: Boolean?
) : RecyclerView.Adapter<PlanAttachmentAdapter.AttachmentViewHolder>() {

	private val mAttachments = attachment
	private val mProgressCallback = progressCallback
	private val mHandleListener = handleListener
	private val mShowDelete = showDelete
	var onItemDelCallBack: OnItemDelCallBack? = null

	override fun getItemCount(): Int = if (CommonUtil.isEmptyList(mAttachments)) 0 else mAttachments!!.size

	override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
		FELog.e("PlanAttachmentAdapter--> onBindViewHolder -->$position")
		val itemView = holder.itemView
		itemView.ivDelete.visibility = if (mShowDelete == true) View.VISIBLE else View.GONE

		val attachment = mAttachments!![position]
		itemView.ivFile.setImageResource(FileCategoryTable.getIcon(attachment.type))
		itemView.tvFileName.text = attachment.name

		if (attachment is NetworkAttachment) {
			val file = AttachmentUtils.getDownloadedAttachment(attachment)
			if (file == null) {
				itemView.tvFileSize.visibility = if (attachment.size == 0L) View.INVISIBLE else View.VISIBLE
				itemView.tvFileSize.text = Formatter.formatFileSize(CoreZygote.getContext(), attachment.size)
				itemView.ivDownSuccess.visibility = View.GONE
			}
			else {
				itemView.tvFileSize.visibility = View.VISIBLE
				itemView.tvFileSize.text = Formatter.formatFileSize(CoreZygote.getContext(), file.length())
				itemView.ivDownSuccess.visibility = View.VISIBLE
			}

			val progress = mProgressCallback?.downloadProgress(attachment)
			if (progress == null || progress.isCompleted) {
				itemView.ivFile.setColorFilter(Color.TRANSPARENT)
				itemView.ivDownState.visibility = View.GONE
				itemView.progressBar.visibility = View.GONE
			}
			else {
				itemView.ivFile.setColorFilter(Color.parseColor("#88FFFFFF"))
				itemView.ivDownState.visibility = View.VISIBLE
				itemView.ivDownState.tag = if (progress.isRunning) 1 else 0
				itemView.ivDownState.setImageResource(
					if (progress.isRunning) cn.flyrise.feep.media.R.mipmap.ms_icon_download_state_pause
					else cn.flyrise.feep.media.R.mipmap.ms_icon_download_state_restart
				)
				itemView.ivDownSuccess.visibility = View.GONE
				itemView.progressBar.visibility = View.VISIBLE
				itemView.progressBar.setProgress(progress.progress)  // 设置进度
			}
		}
		else {
			itemView.tvFileSize.text = Formatter.formatFileSize(CoreZygote.getContext(), attachment.size)
			itemView.ivFile.setColorFilter(Color.TRANSPARENT)
			itemView.ivDownSuccess.visibility = View.GONE
			itemView.ivDownState.visibility = View.GONE
			itemView.progressBar.visibility = View.GONE
		}
		itemView.setOnClickListener { mHandleListener?.onAttachmentItemClick(position, attachment) }
		itemView.ivDelete.setOnClickListener {
			mAttachments.remove(attachment)
			onItemDelCallBack?.onDel(attachment)
			notifyDataSetChanged()
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
		val itemView = LayoutInflater.from(parent.context).inflate(R.layout.plan_view_attachment_item, parent, false)
		return AttachmentViewHolder(itemView)
	}

	class AttachmentViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView)

	interface OnItemDelCallBack {
		fun onDel(attachment: Attachment)
	}

}