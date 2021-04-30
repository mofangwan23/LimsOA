package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.BadgeView
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.ui.bean.PublishCompleted
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import kotlinx.android.synthetic.main.nms_activity_meeting_manager.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author ZYP
 * @since 2018-06-15 14:56
 *                                                                   |- 全部
 *                         |- MineMeetingFragment - MineMeetingPage -|- 我参与
 * MeetingManagerActivity -|                                         |- 我发起
 *                         |- MeetingRoomFragment
 */
class MeetingManagerActivity : BaseActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvRightText: TextView
    private lateinit var tvBadge: BadgeView

    private lateinit var mineMeetingFragment: MineMeetingFragment
    private lateinit var meetingRoomFragment: MeetingRoomFragment
    private var untreatedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nms_activity_meeting_manager)
    }

    override fun bindView() {
        val ivBack = findViewById(R.id.nmsIvBack) as ImageView
        val navigation = resources.getDrawable(cn.flyrise.feep.core.R.mipmap.core_icon_back)
        navigation.setColorFilter(Color.parseColor("#484848"), PorterDuff.Mode.SRC_ATOP)

        ivBack?.setImageDrawable(navigation)
        ivBack?.setOnClickListener { finish() }

        tvTitle = findViewById(R.id.nmsTvTitle) as TextView
        tvRightText = findViewById(R.id.nmsTvRightText) as TextView
        tvBadge = findViewById(R.id.nmsTvBadge) as BadgeView

        nmsMeetingMine.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nmsMeetingMineRoom.isChecked = false
                switchPage(mineMeetingFragment)
            }
        }

        nmsMeetingMineRoom.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                nmsMeetingMine.isChecked = false
                switchPage(meetingRoomFragment)
            }
        }

    }

    override fun bindData() {
        mineMeetingFragment = MineMeetingFragment()
        meetingRoomFragment = MeetingRoomFragment()
        mineMeetingFragment.setUntreatedCallback {
            untreatedCount = it
            if (it in 1..98) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = "${it}"
//                tvBadge.textSize = PixelUtil.dipToPx(4.0f).toFloat()
            } else if (it > 99) {
                tvBadge.visibility = View.VISIBLE
                tvBadge.text = "..."
//                tvBadge.textSize = PixelUtil.dipToPx(2.0f).toFloat()
            } else {
                tvBadge.visibility = View.GONE
            }
            tvRightText.text = getString(R.string.meeting7_detail_not_handled)
            tvRightText.setOnClickListener {
                val intent = Intent(MeetingManagerActivity@ this, UntreateMeetingActivity::class.java)
                startActivity(intent)
            }
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.layoutFragmentContainer, mineMeetingFragment)
                .add(R.id.layoutFragmentContainer, meetingRoomFragment)
                .hide(meetingRoomFragment)
                .show(mineMeetingFragment)
                .commit()
    }

    private fun switchPage(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        if (fragment is MineMeetingFragment) {
            tvTitle.text = getString(R.string.meeting7_main_manager_title)
            tvRightText.text = getString(R.string.meeting7_detail_not_handled)
            tvBadge.visibility = if (untreatedCount > 0) View.VISIBLE else View.GONE
            tvRightText.setOnClickListener {
                val intent = Intent(MeetingManagerActivity@ this, UntreateMeetingActivity::class.java)
                startActivity(intent)
            }
            transaction.hide(meetingRoomFragment).show(fragment)
        } else {
            tvTitle.text = getString(R.string.meeting7_main_room)
            tvRightText.text = getString(R.string.meeting7_main_custom)
            tvBadge.visibility = View.GONE
            tvRightText.setOnClickListener {
                val intent = Intent(MeetingManagerActivity@ this, NewMeetingActivity::class.java)
                intent.putExtra("isCustomMeeting", true)
                intent.putExtra("roomInfo", RoomInfo())
                startActivity(intent)
            }
            transaction.hide(mineMeetingFragment).show(fragment)
        }
        transaction.commitAllowingStateLoss()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onPublishCompleted(c: PublishCompleted) {
        if (c.code == 200) {
            nmsMeetingMine.isChecked = true
            nmsMeetingMineRoom.isChecked = false
            switchPage(mineMeetingFragment)
            mineMeetingFragment.swtichToMineSponsorPage()
            meetingRoomFragment.refreshWhenNewMeetingCreate()
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}