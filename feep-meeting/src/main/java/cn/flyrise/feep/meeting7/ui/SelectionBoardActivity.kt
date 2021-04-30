package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo

/**
 * @author ZYP
 * @since 2018-07-03 14:29
 */
abstract class SelectionBoardActivity : BaseActivity() {

    protected lateinit var r: RoomInfo
    protected lateinit var tvCreate: TextView
    protected var startDate: MSDateItem? = null
    protected var endDate: MSDateItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        r = intent.getParcelableExtra("roomInfo")
        setContentView(R.layout.nms_activity_selection)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        toolbar?.setTitle(r.roomName)
    }

    override fun bindView() {
        tvCreate = findViewById(R.id.nmsTvCreateMeeting)
    }

    override fun bindData() {
        startDate = r.startDate()
        endDate = r.endDate()
        updateCreateButtonStyle()
    }

    override fun bindListener() {
        tvCreate.setOnClickListener {
            if (startDate == null || endDate == null) return@setOnClickListener
            createMeeting()
        }
    }

    protected fun createMeeting() {
        r.updateInfo(startDate, endDate)
        val intent = Intent(this, NewMeetingActivity::class.java)
        intent.putExtra("roomInfo", r)
        startActivity(intent)
    }

    protected fun updateCreateButtonStyle() {
        if (startDate == null || endDate == null) {
            tvCreate.isEnabled = false
            tvCreate.setTextColor(Color.parseColor("#80FFFFFF"))
            return
        }

        if (startDate is MSTimeItem && endDate is MSTimeItem) {
            val s = startDate as MSTimeItem
            val e = endDate as MSTimeItem
            if (s.hour == 0 && e.hour == 0 && s.minute == 0 && e.minute == 0) {
                tvCreate.isEnabled = false
                tvCreate.setTextColor(Color.parseColor("#80FFFFFF"))
                return
            }
        }

        tvCreate.isEnabled = true
        tvCreate.setTextColor(Color.WHITE)
    }

}