package cn.flyrise.feep.meeting7.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.OccupyRoom
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers


/**
 * @author ZYP
 * @since 2018-07-03 16:53
 */
class RoomServiceCondition : Fragment() {

    private lateinit var tvTitle: TextView
    private lateinit var tvTime: TextView
    private lateinit var tvTime2: TextView
    private lateinit var tvCompere: TextView
    private var or: OccupyRoom? = null

    companion object {
        fun new(or: OccupyRoom): RoomServiceCondition {
            val instance = RoomServiceCondition()
            instance.or = or
            return instance
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.nms_item_meeting_room_book_detail, container, false)
        bindView(contentView)
        return contentView
    }

    private fun bindView(contentView: View) {
        tvTitle = contentView.findViewById(R.id.nmsTvTitle)
        tvTime = contentView.findViewById(R.id.nmsTvTime)
        tvTime2 = contentView.findViewById(R.id.nmsTvTime2)
        tvCompere = contentView.findViewById(R.id.nmsTvCompere)

        tvTitle.text = or?.topic

        if (or?.isSameDay() ?: true) {
            tvTime.text = String.format("%d年%02d月%02d日 %02d:%02d-%02d:%02d",
                    or?.startYear, (or?.startMonth ?: 0) + 1, or?.startDay, or?.startHour, or?.startMinute, or?.endHour, or?.endMinute)
        } else {
            tvTime2.visibility = View.VISIBLE
            tvTime.text = String.format("%d年%02d月%02d日 %02d:%02d", or?.startYear,
                    (or?.startMonth ?: 0) + 1, or?.startDay, or?.startHour, or?.startMinute)
            tvTime2.text = String.format("%d年%02d月%02d日 %02d:%02d", or?.endYear,
                    (or?.endMonth ?: 0) + 1, or?.endDay, or?.endHour, or?.endMinute)
        }

        CoreZygote.getAddressBookServices().queryUserDetail(or?.userId)
                .subscribe({ user ->
                    tvCompere.text = user?.name ?: "无"
                }, { error ->
                    tvCompere.text = "无"
                })
    }

}