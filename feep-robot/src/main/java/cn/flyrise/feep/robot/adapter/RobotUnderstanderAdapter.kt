package cn.flyrise.feep.robot.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.robot.R
import cn.flyrise.feep.robot.Robot
import cn.flyrise.feep.robot.adapter.holder.*
import cn.flyrise.feep.robot.bean.RobotAdapterListData
import cn.flyrise.feep.robot.module.RobotModuleItem
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2017-6-12.
 */

class RobotUnderstanderAdapter(private val context: Context) : RecyclerView.Adapter<RobotViewHodler>() {

    private var mModuleList: MutableList<RobotModuleItem>? = mutableListOf()
    private var mListener: OnRobotClickeItemListener? = null
    private val viewHodlers = LinkedList<RobotViewHodler>()

    //获取当前列表中的流程
    val process: List<RobotAdapterListData>?
        get() {
            if (CommonUtil.isEmptyList(mModuleList)) return null
            val listDatas = LinkedList<RobotAdapterListData>()
            var listData: RobotAdapterListData
            for (robotModuleItem in mModuleList!!) {
                listData = RobotAdapterListData()
                listData.process = robotModuleItem.process
                listData.title = robotModuleItem.title
                listData.messageId = robotModuleItem.moduleId
                listData.service = robotModuleItem.service
                listData.adapterIndex = robotModuleItem.indexType
                listDatas.add(listData)
            }
            return listDatas
        }

    fun setGrammarItem(moduleItem: RobotModuleItem?) {
        if (moduleItem == null) return
        mModuleList!!.add(moduleItem)
        deleteScheduleHint()
        notifyDataSetChanged()
    }

    fun setCancleData() {
        if (CommonUtil.isEmptyList(mModuleList)) return
        mModuleList!!.clear()
        notifyDataSetChanged()
    }


    fun cancle() {//清空数据
        if (CommonUtil.isEmptyList(viewHodlers)) return
        viewHodlers.forEach { it.onDestroy() }
        viewHodlers.clear()
    }

    //清除日程的提示框
    private fun deleteScheduleHint() {
        if (mModuleList!!.size <= 3) return
        if (mModuleList!![mModuleList!!.size - 1].process == Robot.process.end && mModuleList!![mModuleList!!.size - 2].process == Robot.schedule.send_hint) {
            mModuleList!!.removeAt(mModuleList!!.size - 2)
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (mModuleList == null || mModuleList!!.size <= 0) {
            return Robot.adapter.ROBOT_INPUT_LEFT
        }
        return mModuleList!![position].indexType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        Robot.adapter.ROBOT_CONTENT_HINT ->
            ContentViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_content_item, parent, false), context,
                    mListener)
        Robot.adapter.ROBOT_INPUT_LEFT ->
            LeftViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_left_item, parent, false), context)
        Robot.adapter.ROBOT_INPUT_RIGHT ->
            RightViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_right_item, parent, false), context)
        Robot.adapter.ROBOT_CONTENT_LIST ->
            MessageListViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_list_item, parent, false), context, mListener)
        Robot.adapter.ROBOT_CONTENT_EMAIL ->
            EmailViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_content_email_layout, parent, false), context)
        Robot.adapter.ROBOT_CONTENT_HINT_TITLE ->
            TitleViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_title_layout, parent, false))
        Robot.adapter.ROBOT_WEATHER_HINT_LIST ->
            WeatherViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_weather_item, parent, false), context)
        Robot.adapter.ROBOT_PLAY_VOICE ->
            PlayVoiceViewHolder(LayoutInflater.from(context).inflate(R.layout.robot_paly_voice_layout, parent, false), context)
        Robot.adapter.ROBOT_CONTENT_TRAIN ->
            TrainViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_train_layout, parent, false), context)
        Robot.adapter.ROBOT_CONTENT_HOLIDAY ->
            HolidayViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_holiday_layout, parent, false), context)
        Robot.adapter.ROBOT_CONTENT_RIDDLE ->
            RiddleViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_content_riddle, parent, false), context)
        else ->
            LeftViewHodler(LayoutInflater.from(context).inflate(R.layout.robot_understander_left_item, parent, false), context)
    }

    override fun onBindViewHolder(holder: RobotViewHodler, position: Int) {
        viewHodlers.add(holder)
        holder.setRobotModuleItem(mModuleList!![position])
        when (holder) {
            is ContentViewHodler -> holder.setContentViewHodler(position, mModuleList!!.size)//居中
            is LeftViewHodler -> holder.setLeftViewHodler()//左边（AIUI反馈）
            is RightViewHodler -> holder.setRightViewHodler()//右边(一般为用户输入)
            is MessageListViewHodler -> holder.setListViewHodler()//列表
            is EmailViewHodler -> holder.setEmailViewHodler()//邮件列表
            is TitleViewHodler -> holder.setTitleViewHodler()//列表标题
            is WeatherViewHodler -> holder.setWeatherViewHodler()//天气
            is PlayVoiceViewHolder -> holder.setPlayVoiceViewHolder()//音频播放
            is TrainViewHodler -> holder.setTrainViewHodler()//车票
            is HolidayViewHodler -> holder.setHolidayViewHodler()//节假日查询
            is RiddleViewHodler -> holder.setContentViewHodler()//猜谜语
        }
    }

    override fun getItemCount(): Int {
        return if (CommonUtil.isEmptyList(mModuleList)) 0 else mModuleList!!.size
    }


    class OnMailAccountChangeEvent {
        var newAccount: String? = null
        var userName: String? = null
    }

    interface OnRobotClickeItemListener {

        fun onItem(detail: RobotModuleItem)
    }

    fun setOnRobotClickeItemListener(listener: OnRobotClickeItemListener) {
        this.mListener = listener
    }

}