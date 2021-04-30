package cn.flyrise.feep.meeting7.ui

import android.os.Bundle
import cn.flyrise.feep.meeting7.selection.date.DateSelectionFragment
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.PublishCompleted
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author ZYP
 * @since 2018-07-03 14:22
 *
 * 会议室预定-跨天
 * Intent启动参数如下：
 * @param roomInfo 会议室信息
 */
class DateSelectionBoardActivity : SelectionBoardActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    override fun bindData() {
        super.bindData()
        val dateSelectionBoard = DateSelectionFragment.newInstance(r)
        dateSelectionBoard.setOnDateChangeListener { startDate, endDate ->
            this.startDate = startDate
            this.endDate = endDate
            this.updateCreateButtonStyle()
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.nmsLayoutSelectionFragment, dateSelectionBoard)
                .show(dateSelectionBoard)
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