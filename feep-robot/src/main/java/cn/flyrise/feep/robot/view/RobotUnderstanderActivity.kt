package cn.flyrise.feep.robot.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.robot.BaseRobotActivity
import cn.flyrise.feep.robot.R
import cn.flyrise.feep.robot.adapter.RobotUnderstanderAdapter
import cn.flyrise.feep.robot.contract.RobotUnderstanderContract
import cn.flyrise.feep.robot.event.EventRobotModule
import cn.flyrise.feep.robot.module.RobotModuleItem
import cn.flyrise.feep.robot.presenter.RobotUnderstanderPresenter
import cn.flyrise.feep.robot.util.RobotWeatherType
import cn.flyrise.feep.robot.util.VibrateAndSoundHelp
import cn.squirtlez.frouter.FRouter
import kotlinx.android.synthetic.main.robot_understander_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 陈冕;
 */

class RobotUnderstanderActivity : BaseRobotActivity(), RobotUnderstanderContract.View {

    private var mPresenter: RobotUnderstanderPresenter? = null
    private var mAdapter: RobotUnderstanderAdapter? = null
    private var mVibrateAndSound: VibrateAndSoundHelp? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.robot_understander_layout)
        EventBus.getDefault().register(this)

    }

    override fun bindData() {
        super.bindData()
        mPresenter = RobotUnderstanderPresenter(this)
        mAdapter = RobotUnderstanderAdapter(this)
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        recyclerView!!.adapter = mAdapter
        recyclerView!!.itemAnimator = DefaultItemAnimator()
        mVibrateAndSound = VibrateAndSoundHelp(this)
        mLayoutMore!!.visibility = View.VISIBLE
        mPresenter!!.showWhatCanSayFragment()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindListener() {
        super.bindListener()
        mAdapter!!.setOnRobotClickeItemListener(object : RobotUnderstanderAdapter.OnRobotClickeItemListener {
            override fun onItem(detail: RobotModuleItem) {
                mPresenter!!.robotOperationItem(detail)
            }
        })
        mRobotFinish!!.setOnClickListener { finish() }
        mRobotSetting!!.setOnClickListener { showSettingDialog() }
        mSayButton!!.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mHandler.postDelayed({ mVibrateAndSound!!.play() }, 200)
                    mAdapter!!.cancle()
                    mPresenter!!.startRobotUnderstander()
                }
                MotionEvent.ACTION_UP -> mPresenter!!.stopRobotUnderstander()
            }
            true
        }
    }

    override fun startWaveView() {
        mWaveView?.start()
        mRobotLoadingTitle.text = getString(R.string.robot_hint_operation_down)
    }

    override fun stopWaveView() {
        mWaveView?.stop()
        mRobotLoadingTitle.text = getString(R.string.robot_hint_operation_up)
    }

    override fun setWaveViewSeep(seep: Int) {//音量

    }

    override fun setMoreLayout(isShow: Boolean) {
        mLayoutMore!!.visibility = if (isShow) View.VISIBLE else View.GONE
    }

    override fun getCurrentProcess() = mAdapter?.process

    override fun onResume() {//移动数据统计分析
//        FlowerCollector.onResume(this)
//        FlowerCollector.onPageStart(TAG)
        super.onResume()
    }

    override fun onPause() {//移动数据统计分析
//        FlowerCollector.onPageEnd(TAG)
//        FlowerCollector.onPause(this)
        super.onPause()
        mPresenter!!.onPause()
    }

    private fun showSettingDialog() {
        FEMaterialDialog.Builder(this)
                .setItems(R.array.robot_setting_titles) { dialog, _, position ->
                    when (position) {
                        0 -> mAdapter?.setCancleData()
                        1 -> mPresenter!!.showWhatCanSayFragment()
                        2 -> startActivity(Intent(this, RobotVoiceSettingActivity::class.java))
                    }
                    dialog.dismiss()
                }.build().show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun EventModuleDetailItem(module: EventRobotModule) {//更新数据
        mAdapter!!.setGrammarItem(module.moduleItem)
        recyclerView.smoothScrollToPosition(mAdapter!!.getItemCount() - 1)
        val moduleItems = ArrayList<RobotModuleItem>()
        moduleItems.add(module.moduleItem)
        mPresenter!!.speechSynthesis(moduleItems)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMailAccountChange(event: RobotUnderstanderAdapter.OnMailAccountChangeEvent) { //邮件
        val mCurrentMailAccount = event.newAccount
        val defaultMailAccount = CoreZygote.getLoginUserServices().userName
        if (TextUtils.equals(defaultMailAccount, mCurrentMailAccount)) {
            FRouter.build(this, "/mail/search")
                    .withString("extra_type", resources.getString(R.string.lbl_text_mail_box))
                    .withString("extra_box_name", "InBox/Inner")
                    .withString("mail_search_text", event.userName).go()
        } else {
            FRouter.build(this, "/mail/search")
                    .withString("extra_type", resources.getString(R.string.lbl_text_mail_box))
                    .withString("extra_box_name", "InBox/Inner")
                    .withString("extra_mail_account", mCurrentMailAccount)
                    .withString("mail_search_text", event.userName).go()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter!!.cancle()
        mVibrateAndSound!!.onDestroy()
        EventBus.getDefault().unregister(this)
        mPresenter!!.onDestroy()
        RobotWeatherType.getInstance().onDestroy()
    }


    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (mLayoutMore!!.visibility == View.VISIBLE && mPresenter!!.isShowMoreFragment) {
            mPresenter!!.showWhatCanSayFragment()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun networkError() {
        if (this.isFinishing) return
        mPresenter?.networkError()
    }


    companion object {

        private val TAG = RobotUnderstanderActivity::class.java.simpleName
    }

}
