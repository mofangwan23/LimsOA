package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.adapter.SignInLeaderDayStatisFieldPersonnelAdapter
import cn.flyrise.feep.location.bean.SignInCalendarData
import cn.flyrise.feep.location.bean.SignInFieldPersonnel
import cn.flyrise.feep.location.bean.SignInLeaderDayStatis
import cn.flyrise.feep.location.dialog.SignInLoadingHint
import cn.flyrise.feep.location.model.SignInStatisModel
import cn.flyrise.feep.location.util.LocationDayPickerUtil
import cn.flyrise.feep.location.views.SignInCalendarActivity
import cn.flyrise.feep.location.views.SignInLeaderDayDetailActivity
import kotlinx.android.synthetic.main.location_leader_statis_day_fragment.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-23-15:10.
 * 考勤统计界面-日统计
 */

class LeaderDayStatisFragment : Fragment(), LocationDayPickerUtil.LocationDayPickerListener, SignInLeaderDayStatisFieldPersonnelAdapter.OnFieldPersonnelItemListener {

    private var mPickerDayUtil: LocationDayPickerUtil? = null
    private var mModel: SignInStatisModel? = null

    private var mAdapter: SignInLeaderDayStatisFieldPersonnelAdapter? = null
    private var isLeader: Boolean = false
    private var mLoadingHint = SignInLoadingHint()

    fun setLeader(isLeaders: Boolean) {
        isLeader = isLeaders
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_leader_statis_day_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindView()
        bindData()
        bindListener()
    }

    private fun bindView() {
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mRecyclerView!!.isNestedScrollingEnabled = false
        mAdapter = SignInLeaderDayStatisFieldPersonnelAdapter(context, this)
        mRecyclerView!!.setAdapter(mAdapter)
    }

    private fun bindData() {
        val calendar = Calendar.getInstance()
        mModel = SignInStatisModel()
        mPickerDayUtil = LocationDayPickerUtil(context, calendar, this)
        mTvDayTitle!!.text = mPickerDayUtil!!.getCalendarToText(calendar)
        requestLeaderDay(mPickerDayUtil!!.getCalendarToYears(calendar))
    }

    private fun bindListener() {
        mLayoutDayPicker!!.setOnClickListener { v -> mPickerDayUtil!!.showMonPicker(mTvDayTitle!!.text.toString().trim { it <= ' ' }) }
        mProgressDayView!!.setOnClickeDetailListener { v ->
            SignInLeaderDayDetailActivity.start(context!!, mPickerDayUtil!!
                    .getCalendarToDate(mTvDayTitle!!.text.toString().trim { it <= ' ' }))
        }
    }

    private fun requestLeaderDay(month: String) {//month:2018-03-03
        mLoadingHint.showDialog(context!!,true)
        mModel!!.requestLeaderDay(month).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mLoadingHint.hide()
                    this.resultLeaderDayData(it)
                }) { error ->
                    mLoadingHint.hide()
                    mProgressDayView!!.resetData()
                    showMonthError(true)
                }
    }

    private fun resultLeaderDayData(dayStatis: SignInLeaderDayStatis?) {
        showMonthError(dayStatis == null || CommonUtil.isEmptyList(dayStatis.outWork))
        if (dayStatis == null) {
            mProgressDayView!!.resetData()
            return
        }
        if (CommonUtil.isEmptyList(dayStatis.dayItems)) return
        for (dayItem in dayStatis.dayItems) {
            if (dayItem == null) continue
            when (dayItem.sumId) {
                Sign.state.LATE -> setTextViewData(mTvDayLate, lateTitle, dayItem.count)
                Sign.state.EARLY -> setTextViewData(mTvDayEarly, earlyTitle, dayItem.count)
                Sign.state.ABSENCE_DUTY -> setTextViewData(mTvDayaBsenceDuty, absenceTitle, dayItem.count)
                Sign.state.ABSENTEEISM -> setTextViewData(mTvDayAbsenteeism, absenteeismTitle, dayItem.count)
                Sign.state.SHOULD_BE -> mProgressDayView!!.setShouldBeNum(dayItem.count)
                Sign.state.ALREADY_SIGN -> mProgressDayView!!.setArriveNum(dayItem.count)
            }
        }
        mAdapter!!.addItem(dayStatis.outWork)
        mProgressDayView!!.setProgress()
    }

    private fun setTextViewData(view: TextView, title: TextView, count: Int?) {
        view.apply {
            text = count.toString()
            isEnabled = count ?: 0 > 0
        }
        title.isEnabled = count ?: 0 > 0
    }

    override fun dateDayPicker(day: String) {
        if (mTvDayTitle != null) mTvDayTitle!!.text = mPickerDayUtil!!.getCalendarToText(day)
        if (mProgressDayView != null) mProgressDayView!!.resetData()
        requestLeaderDay(day)
    }

    override fun onFieldPersonnelItem(item: SignInFieldPersonnel) {//外勤人员
        SignInCalendarActivity.start(context!!, SignInCalendarData().apply {
            day = mPickerDayUtil!!.getCalendarToDate(mTvDayTitle!!.text.toString().trim { it <= ' ' })
            userId = item.userId
            isLeader = isLeader
            isTrack = true
        })
    }

    private fun showMonthError(error: Boolean) {
        if (mRecyclerView != null) mRecyclerView!!.visibility = if (error) View.GONE else View.VISIBLE
        if (mLayoutListError != null) mLayoutListError!!.visibility = if (error) View.VISIBLE else View.GONE
    }
}

