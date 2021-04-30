package cn.flyrise.feep.workplan7

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.KeyEvent
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.workplan.PlanStatisticsDetailRequest
import cn.flyrise.android.protocol.entity.workplan.PlanStatisticsDetailResponse
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.common.utils.UIUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.event.EventPlanStatisticsListRefresh
import cn.flyrise.feep.location.adapter.SignInFragmentAdapter
import cn.flyrise.feep.workplan7.fragment.PlanSubmissionListFragment
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem
import kotlinx.android.synthetic.main.plan_classify_list_layout.*
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

/**
 * 新建：陈冕;
 *日期： 2018-6-28-13:43.
 * 查看计划提交详情（日统计、月统计、周统计）
 */
class PlanSubmissionTabActivity : BaseActivity() {

    private val fragments = ArrayList<Fragment>()
    private var mCurrentDate: String = ""

    private lateinit var mToolbar: FEToolbar

    private var item: PlanStatisticsListItem? = null

    companion object {

        val unsubmitted: Int = 0//未提交
        val lateDelivery: Int = 1//迟交
        val submission: Int = 2//按时提交

        val data = "data"

        fun start(context: Context, item: PlanStatisticsListItem) {
            val intent = Intent(context, PlanSubmissionTabActivity::class.java)
            intent.putExtra(data, GsonUtil.getInstance().toJson(item))
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_classify_list_layout)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        mToolbar = toolbar!!
        mToolbar.title = getString(R.string.plan_classify_head_title)
        mToolbar.rightText = getString(R.string.plan_rule_setting)
        mToolbar.setRightTextColor(Color.parseColor("#28B9FF"))
        mToolbar.setRightTextClickListener {
            PlanRuleCreateActivity.start(this, item!!)
        }
        mToolbar.setNavigationOnClickListener({
            finish()
            EventBus.getDefault().post(EventPlanStatisticsListRefresh())
        })
    }

    override fun bindData() {
        super.bindData()
        mViewPager!!.offscreenPageLimit = 3
        item = GsonUtil.getInstance().fromJson(intent!!.getStringExtra(data), PlanStatisticsListItem::class.java)
        if (item == null) return
        mToolbar.title = item?.title

        val calendar: Calendar = Calendar.getInstance()
        mCurrentDate = dateToRequestDefault(calendar.time)
        fragments.add(PlanSubmissionListFragment().newInstance(item?.id!!, unsubmitted, mCurrentDate, {
            mTabLayout!!.getTabAt(unsubmitted)!!.text = getString(R.string.plan_classify_head_unsubmitted) + "($it)"
            UIUtil.fixTabLayoutIndicatorWidth(mTabLayout)
        }))
        fragments.add(PlanSubmissionListFragment().newInstance(item?.id!!, lateDelivery, mCurrentDate))
        fragments.add(PlanSubmissionListFragment().newInstance(item?.id!!, submission, mCurrentDate))
        mViewPager!!.adapter = SignInFragmentAdapter(supportFragmentManager, fragments)
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.addTab(mTabLayout!!.newTab())
        mTabLayout!!.setupWithViewPager(mViewPager)
        mTabLayout!!.getTabAt(unsubmitted)!!.text = getString(R.string.plan_classify_head_unsubmitted)
        mTabLayout!!.getTabAt(lateDelivery)!!.text = getString(R.string.plan_classify_head_late_delivery)
        mTabLayout!!.getTabAt(submission)!!.text = getString(R.string.plan_classify_head_submission_on_time)
        UIUtil.fixTabLayoutIndicatorWidth(mTabLayout)
        notificationDate(textToDate(mCurrentDate))
    }

    override fun bindListener() {
        super.bindListener()
        mImgHeadFront!!.setOnClickListener({
            notificationDate(dateFront(mCurrentDate).time)
        })

        mImgHeadNext!!.setOnClickListener({
            notificationDate(dateNext(mCurrentDate).time)
        })
    }

    private fun notificationDate(date: Date) {
        if (date.time > Calendar.getInstance().time.time) {
            FEToast.showMessage(getString(R.string.plan_submission_future_data_hint))
            return
        }
        mCurrentDate = dateToRequestDefault(date)
        uploadData(item?.id!!, mCurrentDate)
    }

    private fun uploadData(id: String, date: String) {
        LoadingHint.show(this)
        Observable
                .create { f: Subscriber<in PlanStatisticsDetailResponse> ->
                    FEHttpClient.getInstance().post(PlanStatisticsDetailRequest(id, date), object : ResponseCallback<PlanStatisticsDetailResponse>() {
                        override fun onCompleted(t: PlanStatisticsDetailResponse?) {
                            if (TextUtils.equals(t!!.errorCode, "0")) f.onNext(t) else f.onError(Throwable(t.errorMessage))
                        }
                    })
                }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (LoadingHint.isLoading()) LoadingHint.hide()
                    val isShowPraise = CommonUtil.isEmptyList(it.noPost!!) && CommonUtil.isEmptyList(it.postLater!!)
                    fragments.forEachIndexed { index, fragment ->
                        mTvHeadDay!!.text = getHeadTilte(it!!.caption!!)
                        when (index) {
                            unsubmitted -> (fragment as PlanSubmissionListFragment)
                                    .uploadData(it.noPost!!.toList(), isShowPraise, isCurrenDate(mCurrentDate))
                            lateDelivery -> (fragment as PlanSubmissionListFragment)
                                    .uploadData(it.postLater!!.toList(), isShowPraise, isCurrenDate(mCurrentDate))
                            submission -> (fragment as PlanSubmissionListFragment)
                                    .uploadData(it.havePost!!.toList(), isShowPraise, isCurrenDate(mCurrentDate))
                        }
                    }
                }, {
                    if (LoadingHint.isLoading()) LoadingHint.hide()
                })
    }

    private fun getHeadTilte(text: String) = if (text.contains(",")) {
        val startDate = textToDate(text.split(",")[0])
        val endDate = textToDate(text.split(",")[1])
        dateToStandard(startDate) + "-" + dateTitleEnd(startDate, endDate, dateToStandard(endDate))
    } else {
        dateToStandard(textToDate(text))
    }

    private fun dateTitleEnd(startDate: Date, endDate: Date, text: String) = if (!TextUtils.isEmpty(text)
            && getCalendar(startDate).get(Calendar.YEAR) == getCalendar(endDate).get(Calendar.YEAR)
            && text.contains("年")) {
        text.subSequence(text.indexOf("年") + 1, text.length).toString()
    } else text

    private fun getCalendar(date: Date): Calendar {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar
    }

    @SuppressLint("SimpleDateFormat")
    private fun textToDate(dateText: String): Date {//2018-05-18转date
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.parse(dateText)
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateToStandard(date: Date): String {//date转2018年05月18日
        val sdf = SimpleDateFormat("yyyy年MM月dd日")
        return sdf.format(date)
    }

    @SuppressLint("SimpleDateFormat")
    private fun dateToRequestDefault(date: Date): String {//date转2018-05-18
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        return sdf.format(date)
    }

    private fun isCurrenDate(dateText: String): Boolean {//是否为当前天、周、月
        val currentDate = Calendar.getInstance()
        val calendar = Calendar.getInstance()
        calendar.time = textToDate(dateText)
        return when (item?.fqcy) {
            K.plan.PLAN_FREQUENCY_DAT -> getYear(currentDate) == getYear(calendar)
                    && getMonth(currentDate) == getMonth(calendar)
                    && getMonthDay(currentDate) == getMonthDay(calendar)
            K.plan.PLAN_FREQUENCY_WEEK -> {
                getYear(currentDate) == getYear(calendar)
                        && getMonth(currentDate) == getMonth(calendar)
                        && getMonthWeek(currentDate) == getMonthWeek(calendar)
            }
            K.plan.PLAN_FREQUENCY_MONTH -> getYear(currentDate) == getYear(calendar)
                    && getMonth(currentDate) == getMonth(calendar)
            else -> false
        }
    }

    private fun getYear(calendar: Calendar) = calendar.get(Calendar.YEAR)
    private fun getMonth(calendar: Calendar) = calendar.get(Calendar.MONTH) + 1
    private fun getMonthDay(calendar: Calendar) = calendar.get(Calendar.DAY_OF_MONTH)
    private fun getMonthWeek(calendar: Calendar) = calendar.get(Calendar.WEEK_OF_MONTH)//当前月第几周

    private fun dateFront(dateText: String): Calendar {//减一天
        val calendar = Calendar.getInstance()
        calendar.time = textToDate(dateText)
        when (item?.fqcy) {
            K.plan.PLAN_FREQUENCY_DAT -> calendar.add(Calendar.DAY_OF_MONTH, -1)
            K.plan.PLAN_FREQUENCY_WEEK -> calendar.add(Calendar.DAY_OF_MONTH, -7)
            K.plan.PLAN_FREQUENCY_MONTH -> calendar.add(Calendar.MONTH, -1)
        }
        return calendar
    }

    private fun dateNext(dateText: String): Calendar {//加一天
        val calendar = Calendar.getInstance()
        calendar.time = textToDate(dateText)
        when (item?.fqcy) {
            K.plan.PLAN_FREQUENCY_DAT -> calendar.add(Calendar.DAY_OF_MONTH, 1)
            K.plan.PLAN_FREQUENCY_WEEK -> calendar.add(Calendar.DAY_OF_MONTH, 7)
            K.plan.PLAN_FREQUENCY_MONTH -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        EventBus.getDefault().post(EventPlanStatisticsListRefresh())
        return super.onKeyDown(keyCode, event)
    }
}