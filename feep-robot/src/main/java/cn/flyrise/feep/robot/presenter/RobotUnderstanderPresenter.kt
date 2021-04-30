package cn.flyrise.feep.robot.presenter

import android.content.Context
import android.os.Handler
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.robot.R
import cn.flyrise.feep.robot.Robot
import cn.flyrise.feep.robot.adapter.MoreDetailAdapter
import cn.flyrise.feep.robot.bean.FeSearchMessageItem
import cn.flyrise.feep.robot.contract.OnRobotClickeItemListener
import cn.flyrise.feep.robot.contract.RobotUnderstanderContract
import cn.flyrise.feep.robot.entity.RobotResultData
import cn.flyrise.feep.robot.event.EventMoreDetail
import cn.flyrise.feep.robot.event.EventRobotModule
import cn.flyrise.feep.robot.fragment.MoreDetailFragment
import cn.flyrise.feep.robot.fragment.RobotQuickPrompFragment
import cn.flyrise.feep.robot.fragment.WhatCanSayFragment
import cn.flyrise.feep.robot.manager.AiuiEntryManager
import cn.flyrise.feep.robot.manager.RobotAiuiManager
import cn.flyrise.feep.robot.module.RobotModuleItem
import cn.flyrise.feep.robot.operation.iflytek.NewScheduleIflytek
import cn.flyrise.feep.robot.operation.message.KnowledgeOperation
import cn.flyrise.feep.robot.view.RobotUnderstanderActivity
import cn.squirtlez.frouter.FRouter
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * 新建：陈冕;
 * 日期：2017-6-16.
 */
class RobotUnderstanderPresenter(private val mContext: Context) : RobotUnderstanderContract.Presenter, RobotAiuiManager.AIUIEventReturnListener, WhatCanSayFragment.OnClickeMoreListener, MoreDetailFragment.OnClickeCancleListener, MoreDetailAdapter.OnClickeItemListener {

    private val mView: RobotUnderstanderActivity?
    private val mRobotAiuiManager: RobotAiuiManager   //语音识别类
    private val mEntityManager: AiuiEntryManager//理解实体分类
    private var mCanSayFragment: WhatCanSayFragment? = null//更多列表
    private var mMoreDetailFragment: MoreDetailFragment? = null//更多详情
    private var mQuickPrompFragment: RobotQuickPrompFragment? = null//快速提示语
    private val mHandler = Handler()

    init {
        mView = mContext as RobotUnderstanderActivity
        mRobotAiuiManager = RobotAiuiManager(mContext, this)
        mEntityManager = AiuiEntryManager.Builder().setmContext(mContext).setmView(mView)
                .setmListener(object : OnRobotClickeItemListener {
                    override fun robotClickeItem(detail: RobotModuleItem?) {
                        robotOperationItem(detail!!)
                    }
                })
                .createAiuiEntryManager()
    }

    override fun startRobotUnderstander() {
        showQuickPrompFragment(RobotQuickPrompFragment.startPromp)
        mRobotAiuiManager.startVoiceUnderstander()
    }

    override fun stopRobotUnderstander() {
        showQuickPrompFragment(RobotQuickPrompFragment.startPromp)
        mHandler.postDelayed({ mRobotAiuiManager.stopVoiceUnderstander() }, 100)
    }

    override fun moreDetailClickeItem(title: String) {//提示语点击事件
        mRobotAiuiManager.startTextUnderstander(title)
        mView!!.setMoreLayout(false)
        notificationData(RobotModuleItem.Builder()
                .setIndexType(Robot.adapter.ROBOT_INPUT_RIGHT)
                .setProcess(Robot.process.start)
                .setTitle(title)
                .create())
    }

    override fun showWhatCanSayFragment() { //提示用户能说的内容
        mView!!.setMoreLayout(true)
        val ft: FragmentTransaction = (mContext as AppCompatActivity).supportFragmentManager.beginTransaction()
        hintAllFragment(ft)
        if (mCanSayFragment == null) {
            mCanSayFragment = WhatCanSayFragment()
            mCanSayFragment!!.setOnClickMoreListener(this)
            ft.add(R.id.mLayoutMore, mCanSayFragment!!)
        }
        ft.show(mCanSayFragment!!)
        ft.commitAllowingStateLoss()
    }

    override fun isShowMoreFragment(): Boolean {
        return mMoreDetailFragment?.isVisible ?: false
    }

    override fun onClickeMore(eventMoreDetail: EventMoreDetail) { //点击了查看更多详情
        val ft: FragmentTransaction = (mContext as AppCompatActivity).supportFragmentManager.beginTransaction()
        if (mMoreDetailFragment == null) {
            mMoreDetailFragment = MoreDetailFragment(this)
            mMoreDetailFragment!!.setOnClickeCancleListener(this)
            mMoreDetailFragment!!.setMoreDetail(eventMoreDetail)
            ft.add(R.id.mLayoutMore, mMoreDetailFragment!!)
        }
        hintAllFragment(ft)
        ft.show(mMoreDetailFragment!!)
        ft.commitAllowingStateLoss()
    }

    private fun showQuickPrompFragment(prompType: Int) {
        mView!!.setMoreLayout(true)
        val ft: FragmentTransaction = (mContext as AppCompatActivity).supportFragmentManager.beginTransaction()
        if (mQuickPrompFragment == null) {
            mQuickPrompFragment = RobotQuickPrompFragment()
            ft.add(R.id.mLayoutMore, mQuickPrompFragment!!)
        }
        hintAllFragment(ft)
        ft.show(mQuickPrompFragment!!)
        mQuickPrompFragment!!.showType(prompType)
        ft.commitAllowingStateLoss()
    }

    private fun hintAllFragment(ft: FragmentTransaction) {
        if (mCanSayFragment != null) ft.hide(mCanSayFragment!!)
        if (mMoreDetailFragment != null) ft.hide(mMoreDetailFragment!!)
        if (mQuickPrompFragment != null) ft.hide(mQuickPrompFragment!!)
    }

    override fun onClickeCancle() {
        showWhatCanSayFragment()
    }

    override fun onPause() {
        mRobotAiuiManager.stopVoiceUnderstander()
    }

    override fun onDestroy() {
        mRobotAiuiManager.onDestroy()
        NewScheduleIflytek.getInstance().onDestroy()
        mEntityManager.onDestroy()
    }

    /***讯飞云反馈监听 */

    override fun eventWakeup() {

    }

    override fun eventResult(result: RobotResultData?) { //数据请求成功
        when {
            result == null -> {
//                showQuickPrompFragment(RobotQuickPrompFragment.shortPromp)
            }
            TextUtils.isEmpty(result.text) && TextUtils.isEmpty(result.query) -> {
                showQuickPrompFragment(RobotQuickPrompFragment.noTalkPromp)
            }
            else -> {
                mView!!.setMoreLayout(false)
                mEntityManager.analysis(result)
            }
        }
    }

    override fun eventError(text: String) {
        mView?.stopWaveView()
        FELog.i("-->>>>promp:3:$text")
    }

    override fun eventVad(volume: Int) {//返回的音量
        mView!!.setWaveViewSeep(volume)
    }

    override fun eventStartRecord() {//开始录音
        mEntityManager.vocieAloud.stop()
        mView!!.startWaveView()
    }

    override fun eventStopRecord() {//录音停止
        mView!!.stopWaveView()
    }

    fun networkError() {
        showQuickPrompFragment(RobotQuickPrompFragment.networkEorror)
    }

    override fun eventNoTalk() {
        showQuickPrompFragment(RobotQuickPrompFragment.noTalkPromp)
    }

    /***讯飞云反馈监听END */

    override fun robotOperationItem(detail: RobotModuleItem) { //操作oa
        when {
            detail.moduleParentType == X.Func.Schedule ->
                mView?.intentSchedule()
            detail.indexType == Robot.adapter.ROBOT_CONTENT_LIST && detail.feListItem != null ->
                startMessageDetail(detail.feListItem)
            mEntityManager.robotOperation != null && mEntityManager.robotOperation is KnowledgeOperation ->
                (mEntityManager.robotOperation as KnowledgeOperation).knowledgeIntent(detail.moduleId)
            detail.moduleParentType == 661 && detail.addressBook != null -> //聊天
                FRouter.build(mContext, "/im/chat").withString("Extra_chatID", getImUserId(detail.addressBook.userId)).go()
            (detail.moduleParentType == 662 || detail.moduleParentType == 663) && detail.addressBook != null ->//打电话、发短信
                mEntityManager.searchContactInfo(detail.moduleParentType, detail.addressBook.userId)
            detail.moduleParentType == 664 && detail.addressBook != null -> {//打开用户详情
                FRouter.build(mContext, "/addressBook/detail")
                        .withString("user_id", detail.addressBook.userId)
                        .withString("department_id", detail.addressBook.deptId).go()
            }
        }
    }

    fun speechSynthesis(moduleItems: List<RobotModuleItem>) {
        mEntityManager.robotBaiduVoices(moduleItems)
    }

    private fun notificationData(robotModuleItem: RobotModuleItem?) {
        EventBus.getDefault().post(EventRobotModule(robotModuleItem!!))
    }

    //查看消息详情
    private fun startMessageDetail(detail: FeSearchMessageItem?) {
        val messageClass = FRouter.getRouteClasss("/util/message/detail")
        var cloneable: Constructor<*>? = null
        var method: Method? = null
        try {
            cloneable = messageClass.getConstructor(Context::class.java, Int::class.java
                    , String::class.java, String::class.java, String::class.java, Int::class.java)
            method = messageClass.getMethod("startIntent")
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

        try {
            val messageDetail = cloneable?.newInstance(mContext, detail?.ListRequestType
                    , detail?.BusinessId, detail?.messageId, detail?.title, detail?.moduleItemType)
            method?.invoke(messageDetail)
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }
    }

    private fun getImUserId(userId: String): String {
        if (CoreZygote.getLoginUserServices() == null) return userId
        val companyId = CoreZygote.getLoginUserServices().companyGUID
        return if (companyId.length > 32 && companyId.contains("-")) {
            companyId + "_" + userId
        } else companyId + userId
    }
}
