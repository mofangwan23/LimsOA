package cn.flyrise.feep.meeting7.ui.adapter

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.meeting7.selection.time.normalTextColor
import cn.flyrise.feep.meeting7.selection.time.unableTextColor
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.MeetingDescription

/**
 * @author ZYP
 * @since 2018-06-20 15:51
 */
class MineMeetingListAdapter : RecyclerView.Adapter<MineMeetingViewHolder>() {

    private var meetings: MutableList<MeetingDescription>? = null
    private var itemClickListener: ((MeetingDescription, Int) -> Unit)? = null
    private var recentlyItem: MeetingDescription? = null
    private var recentlyItemPosition: Int = -1

    fun appendToFirst(meetings: MutableList<MeetingDescription>) {
        if (this.meetings == null) {
            this.meetings = mutableListOf()
        }
        this.meetings!!.addAll(0, meetings)

        if (recentlyItem != null) {
            run findRecently@{
                this.meetings?.forEachIndexed { index, description ->
                    if (TextUtils.equals(recentlyItem?.meetingId, description.meetingId)) {
                        recentlyItemPosition = index
                        return@findRecently
                    }
                }
            }
        }
    }

    fun appendDataSource(meetings: MutableList<MeetingDescription>, isFirstLoad: Boolean) {
        if (CommonUtil.isEmptyList(meetings)) return

        if (isFirstLoad) {
            clearDataSource()
            recentlyItem = meetings[0]
            recentlyItemPosition = 0
        }

        if (this.meetings == null) {
            this.meetings = mutableListOf()
        }
        this.meetings!!.addAll(meetings)
    }

    fun clearDataSource() {
        meetings?.clear()
    }

    fun setOnItemClickListener(itemClickListener: (MeetingDescription, Int) -> Unit) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MineMeetingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.nms_item_meeting, parent, false))

    override fun getItemCount() = if (CommonUtil.isEmptyList(meetings)) 0 else meetings!!.size

    override fun onBindViewHolder(holder: MineMeetingViewHolder, position: Int) {
        val description = meetings!!.get(position)
        holder.itemView.setPadding(
                holder.itemView.paddingLeft, if (position == 0) PixelUtil.dipToPx(16.0F) else 0,
                holder.itemView.paddingRight, holder.itemView.paddingBottom)

        if (itemCount == 1) {                                       // 只有一个的情况
            holder.viewTimeLine.visibility = View.GONE
        } else {
            holder.viewTimeLine.visibility = View.VISIBLE
            val layoutParams = holder.viewTimeLine.layoutParams as FrameLayout.LayoutParams
            if (position == 0) {                                    // 第一个
                layoutParams.topMargin = PixelUtil.dipToPx(14.0f)
                layoutParams.bottomMargin = 0
            } else if (position == itemCount - 1) {                 // 最后一个
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = PixelUtil.dipToPx(12.0f)
            } else {                                                // 其他任何情况
                layoutParams.topMargin = 0
                layoutParams.bottomMargin = 0
            }
            holder.viewTimeLine.layoutParams = layoutParams
        }

        holder.tvMeetingTitle.text = description.topics             // 标题
        holder.tvMeetingInitiator.text = description.initiator      // 发起人
        holder.tvMeetingLocation.text = description.roomName        // 会议地点

        holder.tvMeetingDate.text = description.getDisplayDate()    // 左侧日期
        holder.tvMeetingTime.text = description.getDisplayTime()    // 左侧时间

        if (description.isSameDay()) {
            holder.tvMeetingEndDate.visibility = View.GONE
            holder.tvMeetingStartDate.text = description.getDisplayStartEndTime()
        } else {
            holder.tvMeetingEndDate.visibility = View.VISIBLE
            holder.tvMeetingStartDate.text = description.getDisplayStartDate()
            holder.tvMeetingEndDate.text = description.getDisplayEndDate()
        }

        holder.ivMeetingTimeLineHeader.setImageResource(when {
//            description.isToday() -> if (description.isUntreated()) R.mipmap.nms_meeting_state_no_processing else R.mipmap.nms_meeting_state_today
            description.isAttend() -> R.mipmap.nms_meeting_state_tomorrow
            description.isUntreated() -> R.mipmap.nms_meeting_state_no_processing
            description.isNotAttend() -> R.mipmap.nms_meeting_state_unable
            description.isCancel() -> R.mipmap.nms_meeting_state_unable
            else -> R.mipmap.nms_meeting_state_unable
        })

        if (description.isUnknownStatus()) {
            holder.ivMeetingAttendState.visibility = View.GONE
        } else {
            holder.ivMeetingAttendState.visibility = View.VISIBLE
            holder.ivMeetingAttendState.setImageResource(when {
                description.isOutOfDate() -> R.mipmap.nms_ic_state_finished
                description.isAttend() -> R.mipmap.nms_ic_state_attend
                description.isNotAttend() -> R.mipmap.nms_ic_state_no_attend
                description.isCancel() -> R.mipmap.nms_ic_state_cancel
                else -> R.mipmap.nms_ic_state_finished
            })
        }

        if (description.isOutOfDate() || description.isCancel()) {
            holder.tvMeetingTitle.setTextColor(unableTextColor)
            holder.tvMeetingInitiator.setTextColor(unableTextColor)
            holder.tvMeetingLocation.setTextColor(unableTextColor)
            holder.tvMeetingStartDate.setTextColor(unableTextColor)
            holder.tvMeetingEndDate.setTextColor(unableTextColor)
        } else {
            holder.tvMeetingTitle.setTextColor(normalTextColor)
            holder.tvMeetingInitiator.setTextColor(normalTextColor)
            holder.tvMeetingLocation.setTextColor(normalTextColor)
            holder.tvMeetingStartDate.setTextColor(normalTextColor)
            holder.tvMeetingEndDate.setTextColor(normalTextColor)
        }

        if (description.isAttend()) {
            holder.ivMeetingInitiator.setImageResource(R.mipmap.nms_ic_enable_conventioner)
            holder.ivMeetingDate.setImageResource(R.mipmap.nms_ic_enable_time)
            holder.ivMeetingLocation.setImageResource(R.mipmap.nms_ic_enable_location)
        } else {
            holder.ivMeetingInitiator.setImageResource(R.mipmap.nms_ic_unable_conventioner)
            holder.ivMeetingDate.setImageResource(R.mipmap.nms_ic_unable_time)
            holder.ivMeetingLocation.setImageResource(R.mipmap.nms_ic_unable_location)
        }

        if (description.isUnknownStatus()) {
            holder.tvMeetingAttendState.visibility = View.GONE
        } else {
            if (description.isUntreated()) {
                holder.ivMeetingAttendState.visibility = View.GONE
                holder.tvMeetingAttendState.visibility = View.VISIBLE
            } else {
                holder.ivMeetingAttendState.visibility = View.VISIBLE
                holder.tvMeetingAttendState.visibility = View.GONE
            }
        }

        holder.layoutMeetingDetail.setOnClickListener {
            if (itemClickListener != null) {
                itemClickListener!!.invoke(description, position)
            }
        }
    }

    fun findItemByPosition(p: Int) = this.meetings?.get(p) ?: null

    fun findRecentlyItemPosition() = this.recentlyItemPosition
}