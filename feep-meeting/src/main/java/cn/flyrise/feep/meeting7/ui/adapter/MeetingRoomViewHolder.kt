package cn.flyrise.feep.meeting7.ui.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.component.TimeQuantumLayout

/**
 * @author ZYP
 * @since 2018-06-19 14:26
 */
class MeetingRoomViewHolder : RecyclerView.ViewHolder {

    val tvName: TextView
    val tvLocation: TextView
    val tvMeetingRoomDetail: TextView
    val tvMeetingRoomBook: LinearLayout

    val ivState: ImageView
    val tvRoomUseUp: ViewGroup
    val layoutMeetingRoomBook: ViewGroup
    val layoutQuantumContainer: ViewGroup
    val timeQuantumLayout: TimeQuantumLayout

    constructor(itemView: View) : super(itemView) {
        tvName = itemView.findViewById(R.id.nmsTvMeetingRoomName)
        tvLocation = itemView.findViewById(R.id.nmsTvMeetingRoomLocation)
        tvMeetingRoomDetail = itemView.findViewById(R.id.nmsTvMeetingRoomDetail)
        tvMeetingRoomBook = itemView.findViewById(R.id.nmsTvMeetingRoomBook)

        ivState = itemView.findViewById(R.id.nmsIvMeetingRoomState)
        tvRoomUseUp = itemView.findViewById(R.id.nmsTvMeetingRoomUseUp)
        timeQuantumLayout = itemView.findViewById(R.id.nmsTimeQuantumLayout)

        layoutMeetingRoomBook = itemView.findViewById(R.id.nmsLayoutMeetingRoomBook)
        layoutQuantumContainer = itemView.findViewById(R.id.nmsLayoutQuantumContainer)

    }

}