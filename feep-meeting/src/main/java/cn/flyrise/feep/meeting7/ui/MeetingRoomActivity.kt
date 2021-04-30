package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.PublishCompleted
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import cn.squirtlez.frouter.annotations.Route
import kotlinx.android.synthetic.main.nms_activity_meeting_room.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author 社会主义接班人
 * @since 2018-07-31 16:00
 */
@Route("/meeting/room")
class MeetingRoomActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.nms_activity_meeting_room)
    }

    override fun bindData() {
        val navigation = resources.getDrawable(cn.flyrise.feep.core.R.mipmap.core_icon_back)
        navigation.setColorFilter(Color.parseColor("#484848"), PorterDuff.Mode.SRC_ATOP)

        nmsIvBack.setImageDrawable(navigation)
        nmsIvBack.setOnClickListener { finish() }

        nmsTvRightText.setOnClickListener {
            val intent = Intent(MeetingManagerActivity@ this, NewMeetingActivity::class.java)
            intent.putExtra("isCustomMeeting", true)
            intent.putExtra("roomInfo", RoomInfo())
            startActivity(intent)
        }

        val roomPage = MeetingRoomFragment()
        supportFragmentManager.beginTransaction()
                .add(R.id.layoutFragmentContainer, roomPage)
                .show(roomPage)
                .commit()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPublishCompleted(c: PublishCompleted) {
        if (c.code == 200) {
            finish()
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}