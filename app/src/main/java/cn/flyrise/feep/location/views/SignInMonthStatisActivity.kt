package cn.flyrise.feep.location.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.location.Sign.state.monthStatis
import cn.flyrise.feep.location.adapter.SignInMonthStatisAdapter
import cn.flyrise.feep.location.adapter.SignInMonthStatisSubItemAdapter
import cn.flyrise.feep.location.bean.SignInCalendarData
import cn.flyrise.feep.location.bean.SignInMonthStatisItem
import cn.flyrise.feep.location.contract.SignInMonthStatisContract
import cn.flyrise.feep.location.presenter.SignInMonthStatisPresenter
import cn.flyrise.feep.location.util.LocationMonthPickerUtil
import cn.flyrise.feep.location.widget.BaseSuspensionBar
import kotlinx.android.synthetic.main.location_month_summary_layout.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.*
import kotlin.collections.HashMap

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:19.
 * 月汇总
 */

class SignInMonthStatisActivity : BaseActivity(), SignInMonthStatisContract.IView, LocationMonthPickerUtil.LocationMonthPickerListener,
        SignInMonthStatisAdapter.OnClickeItemListener, BaseSuspensionBar.NotificationBarDataListener {

    private var mAdapter: SignInMonthStatisAdapter? = null
    private var mPresenter: SignInMonthStatisContract.IPresenter? = null
    private var mDatePickerUtil: LocationMonthPickerUtil? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var existMap = HashMap<Int, String>()//能够查看详情的子项
    private var cuccessSummary: List<SignInMonthStatisItem>? = null
    private val mHandler = Handler()

    private val userId: String
        get() = if (intent != null && !TextUtils.isEmpty(intent.getStringExtra(SignInMonthStatisContract.IView.USER_ID)))
            intent.getStringExtra(SignInMonthStatisContract.IView.USER_ID)
        else
            CoreZygote.getLoginUserServices().userId

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_month_summary_layout)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = resources.getString(R.string.location_month_summary_title)
    }

    override fun bindView() {
        super.bindView()
        mLinearLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(mLinearLayoutManager)
        mAdapter = SignInMonthStatisAdapter(this, this, object : SignInMonthStatisSubItemAdapter.OnClickeSubItemListener {
            override fun onClickListner(date: String?, sumId: Int) {
                subItmeClick(date!!, sumId)
            }
        })
        mRecyclerView!!.setAdapter(mAdapter)
    }

    private fun subItmeClick(text: String, sumId: Int) {
        existMap.clear()
        if (!monthStatis.contains(sumId)) return
        cuccessSummary?.filter {
            monthStatis.contains(it.sumId) && !CommonUtil.isEmptyList(it.subItems)
        }?.forEach {
            existMap[it.sumId] = "${it.sumTitle}(${it.subItems.size})"
        }
        val data = SignInCalendarData()
        data.userId = userId
        data.day = text
        data.isAllowSwicth = false
        data.existMap = existMap
        data.selectedSumId = sumId
        SignInCalendarActivity.start(this, data)
    }

    override fun bindData() {
        super.bindData()
        mPresenter = SignInMonthStatisPresenter(this)
        val mCalendar = Calendar.getInstance()
        if (intent != null) resetMonth(mCalendar)
        var userId = userId
        if (TextUtils.isEmpty(userId)) userId = CoreZygote.getLoginUserServices().userId
        mDatePickerUtil = LocationMonthPickerUtil(this, mCalendar, this)
        mPresenter!!.requestMonthAndUserId(mDatePickerUtil!!.getCalendarToYears(mCalendar), userId)
        initHead(mCalendar, userId)
    }

    private fun initHead(calendar: Calendar, userId: String) {
        mTvYears!!.text = mDatePickerUtil!!.getCalendarToText(calendar)
        CoreZygote.getAddressBookServices().queryUserDetail(userId)
                .subscribe({
                    if (it != null) {
                        mTvUserName!!.text = it.name
                        FEImageLoader.load(this, mImgUserIcon, CoreZygote.getLoginUserServices().serverAddress + it.imageHref
                                , it.userId, it.name)
                    } else {
                        FEImageLoader.load(this, mImgUserIcon, R.drawable.administrator_icon)
                    }
                }, { error ->
                    FEImageLoader.load(this, mImgUserIcon, R.drawable.administrator_icon)
                })
//        mTvUserName!!.text = addressBook.name
//        FEImageLoader.load(this, mImgUserIcon, CoreZygote.getLoginUserServices().serverAddress + addressBook.imageHref, addressBook.userId, addressBook.name)
    }

    private fun resetMonth(mCalendar: Calendar) {
        if (TextUtils.isEmpty(intent.getStringExtra(SignInMonthStatisContract.IView.MONTH))) return
        mCalendar.time = mPresenter!!.textToDate(intent.getStringExtra(SignInMonthStatisContract.IView.MONTH))
    }

    override fun bindListener() {
        super.bindListener()
        mLayoutDatePicker!!.setOnClickListener { mDatePickerUtil!!.showMonPicker(mTvYears!!.text.toString().trim { it <= ' ' }) }
        mMonthSummaryBar!!.setNotificationBarDataListener(this)
        mRecyclerView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                mMonthSummaryBar!!.setSuspensionHeight()
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                mMonthSummaryBar!!.onScrolled(mLinearLayoutManager!!)
            }
        })
    }

    override fun dateMonthPicker(year: Int, month: Int, years: String) {
        if (mTvYears != null) mTvYears!!.text = mDatePickerUtil!!.getCalendarToText(years)
        mPresenter!!.requestMonthAndUserId(years, CoreZygote.getLoginUserServices().userId)
    }

    override fun onMonthSummaryItem(position: Int) {
        mHandler.postDelayed({
            mRecyclerView!!.scrollToPosition(position)
            setScrollListSummaryBar()
        }, 430)
    }

    override fun resultData(summaryItems: List<SignInMonthStatisItem>) {
        showError(CommonUtil.isEmptyList(summaryItems))
        cuccessSummary = summaryItems;
        mAdapter?.setData(summaryItems)
        setScrollListSummaryBar()
    }

    override fun resultError() {
        showError(true)
    }

    private fun showError(error: Boolean) {
        if (mLayoutContent != null) mLayoutContent!!.visibility = if (error) View.GONE else View.VISIBLE
        if (mLayoutListError != null) mLayoutListError!!.visibility = if (error) View.VISIBLE else View.GONE
    }

    private fun setScrollListSummaryBar() {
        if (mAdapter == null || mMonthSummaryBar == null) return
        val item = mAdapter!!.getItem(mMonthSummaryBar!!.getPosition()) ?: return
        mMonthSummaryBar!!.updateSuspensionBar(item)
    }

    override fun onClickBarItem(position: Int) {
        mAdapter!!.showDetaile(position)
        onMonthSummaryItem(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        setResult(Activity.RESULT_OK)
    }

    override fun onNotificatiionBarData() {
        setScrollListSummaryBar()
    }

    companion object {

        fun start(context: Context, month: String, userId: String) {
            val intent = Intent(context, SignInMonthStatisActivity::class.java)
            intent.putExtra(SignInMonthStatisContract.IView.MONTH, month)
            intent.putExtra(SignInMonthStatisContract.IView.USER_ID, userId)
            context.startActivity(intent)
        }
    }
}