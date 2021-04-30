package cn.flyrise.feep.meeting7.ui.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.meeting7.selection.time.normalTextColor
import cn.flyrise.feep.meeting7.selection.time.unableTextColor
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.MeetingRoomDescription
import cn.flyrise.feep.meeting7.ui.bean.Quantum

/**
 * @author ZYP
 * @since 2018-06-26 16:20
 */
class MeetingRoomAdapter : RecyclerView.Adapter<MeetingRoomViewHolder>() {

    private var dataSource: MutableList<MeetingRoomDescription>? = null

    private var detailFunc: ((MeetingRoomDescription) -> Unit)? = null
    private var bookFunc: ((MeetingRoomDescription, Quantum?) -> Unit)? = null
    private var fullBookFunc: ((MeetingRoomDescription) -> Unit)? = null

    fun setDataSources(dataSource: MutableList<MeetingRoomDescription>) {
        this.dataSource = dataSource
    }

    fun appendDataSource(dataSource: MutableList<MeetingRoomDescription>) {
        if (CommonUtil.isEmptyList(this.dataSource)) {
            this.dataSource = mutableListOf()
        }
        this.dataSource!!.addAll(dataSource)
    }

    fun setMeetingRoomDetailFunc(func: (MeetingRoomDescription) -> Unit) {
        this.detailFunc = func
    }

    fun setMeetingRoomBookFunc(func: (MeetingRoomDescription, Quantum?) -> Unit) {
        this.bookFunc = func
    }

    fun setMeetingRoomBookPreviewFunc(func: (MeetingRoomDescription) -> Unit) {
        this.fullBookFunc = func
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)   //
            = MeetingRoomViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting_room, parent, false))

    override fun getItemCount(): Int = if (CommonUtil.isEmptyList(dataSource)) 0 else dataSource!!.size

    override fun onBindViewHolder(holder: MeetingRoomViewHolder, position: Int) {
        val description = dataSource!!.get(position)
        val params = holder.itemView.layoutParams as RecyclerView.LayoutParams
        params.topMargin = if (position == 0) PixelUtil.dipToPx(16.0F) else 0
        holder.itemView.layoutParams = params

        holder.tvName.text = description.name
        holder.tvLocation.text = description.address

        if (TextUtils.equals(description.status, "关闭")) {
            holder.layoutMeetingRoomBook.visibility = View.GONE
            holder.ivState.visibility = View.VISIBLE
            holder.tvName.setTextColor(unableTextColor)
        }
        else {
            holder.tvName.setTextColor(normalTextColor)
            holder.layoutMeetingRoomBook.visibility = View.VISIBLE
            holder.ivState.visibility = View.GONE
        }

        if (CommonUtil.isEmptyList(description.usableQuantums)) {
            holder.layoutQuantumContainer.visibility = View.GONE
        }
        else {
            holder.layoutQuantumContainer.visibility = View.VISIBLE
            holder.timeQuantumLayout.setOnTimeQuantumClickListener {
                if (bookFunc != null) {
                    bookFunc!!.invoke(description, it)
                }
            }
            holder.timeQuantumLayout.bindDataSources(description.usableQuantums)
        }

        holder.tvMeetingRoomBook.setOnClickListener {
            if (bookFunc != null) {
                bookFunc!!.invoke(description, null)
            }
        }

        holder.tvMeetingRoomDetail.setOnClickListener {
            if (detailFunc != null) {
                detailFunc!!.invoke(description)
            }
        }

        holder.tvRoomUseUp.setOnClickListener {
            if (fullBookFunc != null) {
                fullBookFunc!!.invoke(description)
            }
        }
    }
}