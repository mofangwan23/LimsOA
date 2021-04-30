package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.EventLog
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.meeting7.selection.time.TimeSelectionFragment
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.bean.MSTimeItem
import cn.flyrise.feep.meeting7.selection.time.TimeSelectionDialog
import cn.flyrise.feep.meeting7.ui.bean.PublishCompleted
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author ZYP
 * @since 2018-06-27 17:11
 *
 * 会议室预定-当天
 * Intent启动参数如下：
 * @param roomInfo 会议室信息
 */
class TimeSelectionBoardActivity : SelectionBoardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar?.rightText = "跨天"
        toolbar?.rightTextView?.setTextColor(Color.parseColor("#28B9FF"))
        toolbar?.setRightTextClickListener {
            val intent = Intent(this, DateSelectionBoardActivity::class.java)
            val roomInfo = RoomInfo()
            roomInfo.roomName = r.roomName
            roomInfo.roomId = r.roomId
            roomInfo.type = 1
            intent.putExtra("roomInfo", roomInfo)
            startActivity(intent)
        }
    }

    override fun bindData() {
        super.bindData()
        val timeSelectionBoard = TimeSelectionFragment.newInstance(r)
        timeSelectionBoard.setOnDateChangeListener { startDate, endDate ->
            this.startDate = startDate
            this.endDate = endDate
            this.updateCreateButtonStyle()
        }

        timeSelectionBoard.setOnSelectionInterceptListener {
            TimeSelectionDialog.newInstance(it) { s, e ->
                startDate = s
                endDate = e
                createMeeting()
            }.show(supportFragmentManager, "")
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.nmsLayoutSelectionFragment, timeSelectionBoard)
                .show(timeSelectionBoard)
                .commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPublishCompleted(c: PublishCompleted) {
        if (c.code == 200) finish()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}