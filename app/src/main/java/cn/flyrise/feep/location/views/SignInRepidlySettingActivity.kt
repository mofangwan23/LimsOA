package cn.flyrise.feep.location.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.widget.RelativeLayout
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.location.util.LocationDateTimePickerUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * 新建：陈冕;
 *日期： 2018-6-8-9:26.
 * 急速签到
 */
class SignInRepidlySettingActivity : BaseActivity(), LocationDateTimePickerUtil.LocationTimeListener {

    private val interval: Array<String>? = arrayOf("5分钟", "10分钟", "15分钟", "20分钟", "25分钟", "30分钟", "60分钟", "2小时", "3小时")

    private var mLayoutGoToWork: RelativeLayout? = null
    private var mLayoutGoOffWork: RelativeLayout? = null
    private var mLayoutForeInterval: RelativeLayout? = null
    private var mLayoutLastInterval: RelativeLayout? = null

    private var mTvGoToWorkTime: TextView? = null
    private var mTvGoOffWorkTime: TextView? = null
    private var mTvForeInterval: TextView? = null
    private var mTvLastInterval: TextView? = null
    private var mTvIntervalHind: TextView? = null

    private var mDateTimePicker: LocationDateTimePickerUtil? = null
    private var isGotoWorkTime: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_rapidly_setting_layout)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar!!.title = "急速签到"
    }

    override fun bindView() {
        super.bindView()
        mLayoutGoToWork = findViewById<RelativeLayout>(R.id.go_to_work_layout)
        mLayoutGoOffWork = findViewById<RelativeLayout>(R.id.go_off_work_layout)
        mLayoutForeInterval = findViewById<RelativeLayout>(R.id.sign_in_interval_layout)
        mLayoutLastInterval = findViewById<RelativeLayout>(R.id.go_off_work_interval_layout)

        mTvGoToWorkTime = findViewById<TextView>(R.id.go_to_work_time)
        mTvGoOffWorkTime = findViewById<TextView>(R.id.go_off_work_time)
        mTvForeInterval = findViewById<TextView>(R.id.sign_in_interval_time)
        mTvLastInterval = findViewById<TextView>(R.id.go_off_work_interval_time)
        mTvIntervalHind = findViewById<TextView>(R.id.go_to_work_interval)

    }

    override fun bindData() {
        super.bindData()
        mDateTimePicker = LocationDateTimePickerUtil(this, Calendar.getInstance(), this)
    }

    override fun bindListener() {
        super.bindListener()
        mLayoutGoToWork!!.setOnClickListener {
            isGotoWorkTime = true
            mDateTimePicker!!.showMonPicker(mTvGoToWorkTime!!.text.toString())
        }

        mLayoutGoOffWork!!.setOnClickListener {
            isGotoWorkTime = false
            mDateTimePicker!!.showMonPicker(mTvGoOffWorkTime!!.text.toString())
        }

        mLayoutForeInterval!!.setOnClickListener {
            showIntervalDialog(true)
        }

        mLayoutLastInterval!!.setOnClickListener {
            showIntervalDialog(false)
        }
    }


    override fun dateTimePicker(day: String?) {
        if (isGotoWorkTime) {
            mTvGoToWorkTime!!.text = day
        } else {
            mTvGoOffWorkTime!!.text = day
        }
        notificationIntervalHind()
    }

    private fun showIntervalDialog(isForeInterval: Boolean = true) {
        FEMaterialDialog.Builder(this)
                .setTitle("${if (isForeInterval) "提前" else "延后"}签到时间")
                .setItems(interval, { dialog, _, position ->
                    if (isForeInterval) {
                        mTvForeInterval!!.text = "提前" + interval!![position]
                    } else {
                        mTvLastInterval!!.text = "延后" + interval!![position]
                    }
                    notificationIntervalHind()
                    dialog.dismiss()
                }).build().show()
    }

    private fun notificationIntervalHind() {
        val goToWork: String? = textToTime(true, mTvGoToWorkTime!!.text.toString(), workTime(mTvForeInterval!!.text.toString()))
        val goOffWork: String? = textToTime(false, mTvGoOffWorkTime!!.text.toString(), workTime(mTvLastInterval!!.text.toString()))
        mTvIntervalHind!!.text = "上班最早签到时间：$goToWork，下班最晚签到时间：$goOffWork"
    }

    @SuppressLint("SimpleDateFormat")
    private fun textToTime(isGoToWork: Boolean, text: String?, interval: Int? = 0): String? {
        if (TextUtils.isEmpty(text)) return ""
        val sdFormat: SimpleDateFormat? = SimpleDateFormat("HH:mm")
        val calendar: Calendar? = Calendar.getInstance()
        val clone = calendar!!.clone() as Calendar
        clone.time = sdFormat!!.parse(text)
        clone.set(Calendar.MINUTE, if (isGoToWork) -interval!! else +interval!!)
        return "${textComplement(clone.get(Calendar.HOUR_OF_DAY))} : ${textComplement(clone.get(Calendar.MINUTE))}"
    }

    private fun textComplement(time: Int) = if (time <= 9) "0" + time else time.toString()

    private fun workTime(workText: String) = when {
        TextUtils.isEmpty(workText) -> 0
        workText.contains("分钟") -> workText.substring(2, workText.indexOf("分钟")).toInt()
        workText.contains("小时") -> workText.substring(2, workText.indexOf("小时")).toInt() * 60
        else -> 0
    }
}