package cn.flyrise.feep.location.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.adapter.SignInLeaderMonthStatisAdapter
import cn.flyrise.feep.location.bean.SignInLeaderMonthItem
import cn.flyrise.feep.location.bean.SignInLeaderMonthStatisList
import cn.flyrise.feep.location.dialog.SignInLoadingHint
import cn.flyrise.feep.location.model.SignInStatisModel
import cn.flyrise.feep.location.util.LocationMonthPickerUtil
import cn.flyrise.feep.location.views.SignInLeaderMonthDetailActivity
import kotlinx.android.synthetic.main.location_leader_statis_month_fragment.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-23-15:10.
 * 考勤统计界面-月统计
 */

class LeaderMonthStatisFragment : Fragment(), LocationMonthPickerUtil.LocationMonthPickerListener, SignInLeaderMonthStatisAdapter.OnClickeItemListener {

    private var mAdapter: SignInLeaderMonthStatisAdapter? = null
    private var mPickerMonthUtil: LocationMonthPickerUtil? = null
    private var mModel: SignInStatisModel? = null
    private var mLoadingHint = SignInLoadingHint()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_leader_statis_month_fragment, container, false)
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
        mAdapter = SignInLeaderMonthStatisAdapter(context, this)
        mRecyclerView!!.setAdapter(mAdapter)
    }

    private fun bindData() {
        mModel = SignInStatisModel()
        val calendar = Calendar.getInstance()
        mPickerMonthUtil = LocationMonthPickerUtil(context, calendar, this)
        requestLeaderMonth(mPickerMonthUtil!!.getCalendarToYears(calendar), true)
        mTvMonthTitle!!.text = mPickerMonthUtil!!.getCalendarToText(calendar)
    }

    private fun bindListener() {
        mLayoutMonthPicker!!.setOnClickListener { v -> mPickerMonthUtil!!.showMonPicker(mTvMonthTitle!!.text.toString().trim { it <= ' ' }) }
    }

    fun requestLeaderMonth(month: String, isFirst: Boolean) {//month:2018-03
        if (!isFirst) mLoadingHint.showDialog(context!!, true)
        mModel!!.requestLeaderMonth(month).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mLoadingHint.hide()
                    this.resultLeaderMonth(it)
                }) { error ->
                    mLoadingHint.hide()
                    showMonthError(true)
                }
    }

    @SuppressLint("StringFormatMatches")
    private fun resultLeaderMonth(data: SignInLeaderMonthStatisList?) {
        showMonthError(data == null || CommonUtil.isEmptyList<SignInLeaderMonthItem>(data.record))
        if (data == null) return
        mAdapter?.setData(data.record)
        val text = String.format(resources.getString(R.string.location_leader_month_all_user), data.count, data.exceptCount)
        val spannableString = SpannableString(text)
        spannableString.setSpan(ForegroundColorSpan(Color.parseColor("#FF3B2F")), text.indexOf("，") + 1, text.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        mTvMonthStatisNum?.text = spannableString
    }

    override fun onLearderMonthSummaryItem(position: Int) {
        val item = mAdapter!!.getItem(position)
        if (item == null) return
        SignInLeaderMonthDetailActivity.start(context!!
                , mPickerMonthUtil!!.getCalendarToDate(mTvMonthTitle!!.text.toString().trim { it <= ' ' })
                , item.sumId, item.sumTitle)
    }

    override fun dateMonthPicker(year: Int, month: Int, years: String) {
        mTvMonthTitle?.text = mPickerMonthUtil!!.getCalendarToText(years)
        requestLeaderMonth(years, false)
    }


    private fun showMonthError(error: Boolean) {
        mRecyclerView?.visibility = if (error) View.GONE else View.VISIBLE
    }
}
