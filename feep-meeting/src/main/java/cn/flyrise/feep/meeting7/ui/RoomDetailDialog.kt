package cn.flyrise.feep.meeting7.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.MeetingRoomDetailData
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers



/**
 * @author ZYP
 * @since 2018-06-27 15:59
 */
class RoomDetailDialog : DialogFragment() {

    private var roomDetail: MeetingRoomDetailData? = null

    companion object {
        fun newInstance(roomDetail: MeetingRoomDetailData): RoomDetailDialog {
            val instance = RoomDetailDialog()
            instance.roomDetail = roomDetail
            instance.isCancelable = false
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        val contenteView = inflater.inflate(R.layout.nms_fragment_meeting_room_detail, container, false)
        bindView(contenteView)
        return contenteView
    }

    private fun bindView(view: View) {
        view.findViewById<ImageView>(R.id.nmsIvX).setOnClickListener { dismiss() }          // 关闭

        val ivState = view.findViewById<ImageView>(R.id.nmsIvState)
        val tvName = view.findViewById<TextView>(R.id.nmsTvName)                            // 会议室名称
        val tvSettings = view.findViewById<TextView>(R.id.nmsTvSettings)                    // 会议室配置
        val tvLocation = view.findViewById<TextView>(R.id.nmsTvLocation)                    // 会议室地址
        val tvSeats = view.findViewById<TextView>(R.id.nmsTvSeats)                          // 座椅数量
        val tvAdmin = view.findViewById<TextView>(R.id.nmsTvAdmin)                          // 管理员
        val tvRemark = view.findViewById<TextView>(R.id.nmsTvRemark)                        // 备注

        tvName.text = if (TextUtils.isEmpty(roomDetail?.name)) "无" else roomDetail!!.name
        tvSettings.text = if (TextUtils.isEmpty(roomDetail?.settings)) "无" else roomDetail!!.settings
        tvLocation.text = if (TextUtils.isEmpty(roomDetail?.address)) "无" else roomDetail!!.address
        tvSeats.text = if (TextUtils.isEmpty(roomDetail?.seats)) "无" else roomDetail!!.seats
        tvRemark.text = if (TextUtils.isEmpty(roomDetail?.remark)) "无" else roomDetail!!.remark

        CoreZygote.getAddressBookServices().queryUserDetail(roomDetail?.adminId)
                .subscribe({ userInfo ->
                    tvAdmin.text = if (userInfo == null || TextUtils.isEmpty(userInfo.name)) "无" else userInfo.name
                }, { error ->
                    tvAdmin.text =  "无"
                })

        if (TextUtils.equals(roomDetail?.status, "启用")) return

        // 处理会议室关闭的状态
        val unableTextColor = Color.parseColor("#9DA3A6")
        ivState.visibility = View.VISIBLE
        tvName.setTextColor(unableTextColor)
        tvSettings.setTextColor(unableTextColor)
        tvLocation.setTextColor(unableTextColor)
        tvSeats.setTextColor(unableTextColor)
        tvAdmin.setTextColor(unableTextColor)
        tvRemark.setTextColor(unableTextColor)

    }

}