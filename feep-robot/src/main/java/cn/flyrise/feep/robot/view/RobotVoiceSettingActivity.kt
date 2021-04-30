package cn.flyrise.feep.robot.view

import android.os.Bundle
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.robot.R
import kotlinx.android.synthetic.main.robot_voice_setting_layout.*

/**
 * 新建：陈冕;
 * 日期： 2017-10-17-9:19.
 * 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
</度丫丫></度逍遥> */

class RobotVoiceSettingActivity : BaseActivity() {

    private var mToolbar: FEToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot_voice_setting_layout)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        mToolbar = toolbar
    }

    override fun bindData() {
        super.bindData()
        mToolbar!!.title = resources.getString(R.string.more_setting_title)
        val isOpenVoice = SpUtil.get(ROBOT_VOICE_SWITCH, true)!!
        mVoiceButton!!.isChecked = isOpenVoice
    }

    override fun bindListener() {
        super.bindListener()
        mVoiceButton!!.setOnCheckedChangeListener { _, isChecked -> SpUtil.put(ROBOT_VOICE_SWITCH, isChecked) }
    }

    companion object {

        private val ROBOT_VOICE_SWITCH = "robot_voice_switch"
    }
}
