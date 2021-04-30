package cn.flyrise.feep.robot.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.request.RemoteRequest
import cn.flyrise.feep.robot.Robot
import cn.flyrise.feep.robot.bean.ContactResponse
import cn.flyrise.feep.robot.contract.*
import cn.flyrise.feep.robot.entity.RobotResultData
import cn.flyrise.feep.robot.event.EventRobotModule
import cn.flyrise.feep.robot.module.RobotModuleItem
import cn.flyrise.feep.robot.operation.RobotOperation
import cn.flyrise.feep.robot.operation.iflytek.NewScheduleIflytek
import cn.flyrise.feep.robot.util.RobotVoiceReadingAloud
import cn.flyrise.feep.robot.view.RobotUnderstanderActivity
import org.greenrobot.eventbus.EventBus

/**
 * 新建：陈冕;
 * 日期： 2017-12-1-9:39.
 * 理解实体分类
 */

class AiuiEntryManager private constructor(builder: Builder) {
    private val mContext: Context?
    private val mView: RobotUnderstanderActivity?
    private val mListener: OnRobotClickeItemListener?
    val vocieAloud: RobotVoiceReadingAloud
    private val mAiuiOperationManager: AiuiOperationManager
    var robotOperation: RobotOperation? = null
        private set

    init {
        this.mContext = builder.mContext
        this.mView = builder.mView
        this.mListener = builder.mListener
        vocieAloud = RobotVoiceReadingAloud(mContext)
        mAiuiOperationManager = AiuiOperationManager(mContext, mView)
    }

    //开始解析
    fun analysis(data: RobotResultData): AiuiEntryManager {
        vocieAloud.stop()
        mAiuiOperationManager.setRobotResultData(data)
        when {
            mAiuiOperationManager.grammarUserNameError() -> return this
            TextUtils.equals(feoaMessage, data.service) -> {//fe功能模块
                mAiuiOperationManager.setFeOAMessageModuleItem()
                robotUnderstanderSuccess(data)
            }
            TextUtils.equals(scheduleX, data.service) -> createSchedule(data)//提醒(新建日程)
            TextUtils.equals(weather, data.service) -> mAiuiOperationManager.setWeatherModuleItem()//天气预报
            TextUtils.equals(poetry, data.service) -> mAiuiOperationManager.setPoetryModuleItem()//诗歌
            isMp3Module(data.service) -> mAiuiOperationManager.setPlayVoiceModule()
            TextUtils.equals(train, data.service) -> mAiuiOperationManager.setTrainModule()//火车
            TextUtils.equals(riddle, data.service) -> mAiuiOperationManager.setRiddle()//谜语
            TextUtils.equals(brainTeaser, data.service) -> mAiuiOperationManager.setBrainTeaser()//脑筋急转弯
            TextUtils.equals(holiday, data.service) -> mAiuiOperationManager.setHolidayQuery()//节假日查询
            isQAModule(data.service) -> mAiuiOperationManager.setOpenQAAndGrammarError(mView!!.currentProcess)
            else -> mAiuiOperationManager.setOpenQAAndGrammarError(mView!!.currentProcess)
        }
        return this
    }

    private fun isQAModule(service: String): Boolean {
        return (TextUtils.equals(openQA, service)//问答及理解错误的
                || TextUtils.equals(baike, service)//百科
                || TextUtils.equals(calc, service)//计算
                || TextUtils.equals(wordFinding, service)//近反义词
                || TextUtils.equals(datetime, service)//时间
                || TextUtils.equals(cookbook, service)//菜谱
                || TextUtils.equals(flight, service)//飞机
                || TextUtils.equals(translation, service)//翻译 无信源
                || TextUtils.equals(motorViolation, service)//违章查询 无信源
                || TextUtils.equals(musicX, service)//音乐 无信源
                || TextUtils.equals(stock, service)//股票 无信源
                || TextUtils.equals(dream, service)//周公解梦
                || TextUtils.equals(chineseZodiac, service))//生肖
    }

    //播放音频
    private fun isMp3Module(service: String): Boolean {
        return (TextUtils.equals(joke, service)//笑话
                || TextUtils.equals(news, service)//新闻
                || TextUtils.equals(story, service)//故事
                || TextUtils.equals(englishEveryday, service))//每日一句英语
    }

    //操作oa
    private fun robotUnderstanderSuccess(robotResultData: RobotResultData) {
        val operationManager = FeepOperationManager()
        operationManager.setContext(mContext)
        operationManager.setUnderstanderData(robotResultData)
        operationManager.setProcess(mView!!.currentProcess)
        operationManager.setListener(object : FeepOperationManager.OnMessageGrammarResultListener {
            override fun onGrammarResultItems(robotModuleItems: List<RobotModuleItem>?) {
                if (CommonUtil.isEmptyList(robotModuleItems)) return
                when {
                    robotModuleItems!!.size > 1 -> robotModuleItems.forEach { notificationData(it, robotResultData) }
                    isClickeItem(robotModuleItems[0]) -> notificationData(robotModuleItems[0], robotResultData)
                    else -> mListener?.robotClickeItem(robotModuleItems[0])
                }
            }

            override fun onGrammarMessage(messageId: Int, operation: String?) {
                when (messageId) {
                    X.Func.Location -> mView.operationLocationSign(operation)
                    X.Func.Schedule -> mView.intentSchedule()
                    else -> mAiuiOperationManager.grammarError()
                }
            }

            override fun onGrammarText(text: String) {
                mAiuiOperationManager.setServiceModuleItemHint(text, Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content)
            }

            override fun onGrammarModule(robotModuleItem: RobotModuleItem) {
                notificationData(robotModuleItem, robotResultData)
            }

            override fun onError() {
                mAiuiOperationManager.grammarError()
            }

            override fun onShowOALeftHint() {
                mAiuiOperationManager.getLeftModuleItem()
            }
        })
        robotOperation = operationManager.startOperation()
    }

    //新建日程
    private fun createSchedule(data: RobotResultData) {
        NewScheduleIflytek.getInstance().setContext(mContext)
                .setListener(object : FeepOperationManager.OnMessageGrammarResultListener {
                    override fun onShowOALeftHint() {

                    }

                    override fun onGrammarResultItems(robotModuleItems: List<RobotModuleItem>) {

                    }

                    override fun onGrammarMessage(messageId: Int, operation: String) {

                    }

                    override fun onGrammarText(text: String) {
                        mAiuiOperationManager.setServiceModuleItemHint(text, Robot.adapter.ROBOT_INPUT_LEFT, Robot.process.content)
                    }

                    override fun onGrammarModule(robotModuleItem: RobotModuleItem) {
                        notificationData(robotModuleItem, data)
                    }

                    override fun onError() {
                        mAiuiOperationManager.grammarError()
                    }
                }).createSecedule(data)
//                }).secedule(data, mView!!.currentProcess)
    }

    //语音合成
    fun robotBaiduVoices(robotModuleItems: List<RobotModuleItem>) {
        if (CommonUtil.isEmptyList(robotModuleItems)) {
            return
        }
        var robotModuleItem: RobotModuleItem
        for (i in robotModuleItems.indices.reversed()) {
            robotModuleItem = robotModuleItems[i]
            if (robotBaiduVoice(robotModuleItem)) return
        }
    }

    //语音合成
    private fun robotBaiduVoice(robotModuleItem: RobotModuleItem?): Boolean {
        if (robotModuleItem == null) return false
        val text: String? = when {
            robotModuleItem.indexType == Robot.adapter.ROBOT_INPUT_LEFT && !TextUtils.isEmpty(robotModuleItem.title) -> {
                robotModuleItem.title
            }
            (TextUtils.equals(robotModuleItem.service, weather)
                    || TextUtils.equals(robotModuleItem.service, poetry)
                    || TextUtils.equals(robotModuleItem.service, train))
                    && !TextUtils.isEmpty(robotModuleItem.content) -> {
                robotModuleItem.content
            }

            TextUtils.equals(robotModuleItem.service, scheduleX) -> {
                when {
                    robotModuleItem.process == Robot.schedule.send_hint && !TextUtils.isEmpty(robotModuleItem.htmlContent) -> {
                        robotModuleItem.htmlContent.toString()
                    }
                    robotModuleItem.process == Robot.schedule.end && !TextUtils.isEmpty(robotModuleItem.title) -> {
                        robotModuleItem.title
                    }
                    else -> ""
                }
            }
            else -> ""
        }
        vocieAloud.start(text)
        return !TextUtils.isEmpty(text)
    }

    //打电话
    fun searchContactInfo(type: Int, userId: String) {
        val request = RemoteRequest.buildUserDetailInfoRequest(userId)
        FEHttpClient.getInstance().post(request, object : ResponseCallback<ContactResponse>() {
            override fun onCompleted(response: ContactResponse?) {
                if (response == null || !TextUtils.equals(response.errorCode, "0")) {
                    mAiuiOperationManager.setTheContaceGrammarError()
                    return
                }
                val result = response.result
                if (result == null) {
                    mAiuiOperationManager.setTheContaceGrammarError()
                    return
                }
                val phone: String = when {
                    !TextUtils.isEmpty(result.phone) -> result.phone
                    !TextUtils.isEmpty(result.tel) -> result.tel
                    !TextUtils.isEmpty(result.phone1) -> result.phone1
                    !TextUtils.isEmpty(result.phone2) -> result.phone2
                    else -> ""
                }
                if (TextUtils.isEmpty(phone)) {
                    mAiuiOperationManager.setTheContaceGrammarError()
                    return
                }
                if (type == 662) {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse(TEL + phone)
                    mContext!!.startActivity(intent)
                } else if (type == 663) {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.data = Uri.parse(SMS_TO + phone)
                    mContext!!.startActivity(intent)
                }
            }

            override fun onFailure(repositoryException: RepositoryException) {
                mAiuiOperationManager.setTheContaceGrammarError()
            }
        })
    }

    //唯一列表时是否需要用户选择
    private fun isClickeItem(robotModuleItem: RobotModuleItem?): Boolean {
        return robotModuleItem != null
                && (robotModuleItem.moduleParentType == 46
                || robotModuleItem.moduleParentType == 37
                && TextUtils.equals(robotModuleItem.operationType, Robot.operation.createType))
    }

    fun onDestroy() {
        vocieAloud.onDestroy()
    }

    class Builder {
        var mContext: Context? = null
        var mView: RobotUnderstanderActivity? = null
        var mListener: OnRobotClickeItemListener? = null

        fun setmContext(mContext: Context): Builder {
            this.mContext = mContext
            return this
        }

        fun setmView(mView: RobotUnderstanderActivity): Builder {
            this.mView = mView
            return this
        }

        fun setmListener(mListener: OnRobotClickeItemListener?): Builder {
            this.mListener = mListener
            return this
        }

        fun createAiuiEntryManager(): AiuiEntryManager {
            return AiuiEntryManager(this)
        }
    }

    fun notificationData(robotModuleItem: RobotModuleItem, robotResultData: RobotResultData) {
        if (robotModuleItem.indexType == Robot.adapter.ROBOT_INPUT_RIGHT && robotResultData.inputType == Robot.input.text) return
        EventBus.getDefault().post(EventRobotModule(robotModuleItem))
    }

    companion object {

        private val TEL = "tel:"
        private val SMS_TO = "smsto:"
    }
}
