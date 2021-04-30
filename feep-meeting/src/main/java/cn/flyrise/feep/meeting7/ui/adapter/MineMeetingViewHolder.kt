package cn.flyrise.feep.meeting7.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-06-19 14:05
 */
class MineMeetingViewHolder : RecyclerView.ViewHolder {

    val tvMeetingDate: TextView             // 最左侧：日期
    val tvMeetingTime: TextView             // 最左侧：时间
    val tvMeetingStartDate: TextView        // 会议时间：开始
    val tvMeetingEndDate: TextView          // 会议时间：结束

    val tvMeetingTitle: TextView            // 会议标题
    val tvMeetingInitiator: TextView        // 会议发起人
    val tvMeetingLocation: TextView         // 会议地点
    val tvMeetingAttendState: TextView      // 最右侧：会议参加状态（红色）

    val ivMeetingTimeLineHeader: ImageView  // 会议时间线：开始节点图标
    val viewTimeLine: View                  // 会议时间线

    val ivMeetingAttendState: ImageView     // 右上角：会议参加状态（圆形）

    val ivMeetingInitiator: ImageView       // 图标：会议发起人
    val ivMeetingDate: ImageView            // 图标：会议日期
    val ivMeetingLocation: ImageView        // 图标：会议地点
    val layoutMeetingDetail: ViewGroup


    constructor(itemView: View) : super(itemView) {

        tvMeetingDate = itemView.findViewById(R.id.nmsTvMeetingDate)
        tvMeetingTime = itemView.findViewById(R.id.nmsTvMeetingTime)
        tvMeetingStartDate = itemView.findViewById(R.id.nmsTvMeetingStartTime)
        tvMeetingEndDate = itemView.findViewById(R.id.nmsTvMeetingEndTime)

        tvMeetingTitle = itemView.findViewById(R.id.nmsTvMeetingTitle)
        tvMeetingInitiator = itemView.findViewById(R.id.nmsTvMeetingInitiator)
        tvMeetingLocation = itemView.findViewById(R.id.nmsTvMeetingLocation)
        tvMeetingAttendState = itemView.findViewById(R.id.nmsTvMeetingProcessState)

        ivMeetingTimeLineHeader = itemView.findViewById(R.id.nmsIvTimeLineHeader)
        viewTimeLine = itemView.findViewById(R.id.nmsTimeLine)
        ivMeetingAttendState = itemView.findViewById(R.id.nmsIvMeetingAttendState)
        ivMeetingInitiator = itemView.findViewById(R.id.nmsIvInitiator)
        ivMeetingDate = itemView.findViewById(R.id.nmsIvMeetingDate)
        ivMeetingLocation = itemView.findViewById(R.id.nmsIvMeetingLocation)
        layoutMeetingDetail = itemView.findViewById(R.id.nmsLayoutMeetingDetail)
    }
}