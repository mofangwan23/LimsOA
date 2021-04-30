package cn.flyrise.feep.location.views

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.adapter.SignInLeaderMonthStatisDetailAdapter
import cn.flyrise.feep.location.bean.SignInLeaderMonthDetail
import cn.flyrise.feep.location.contract.SignInLeaderMonthDetailContract
import cn.flyrise.feep.location.presenter.SignInLeaderMonthDetailPresenter
import cn.flyrise.feep.location.widget.BaseSuspensionBar
import kotlinx.android.synthetic.main.location_leader_month_summary_detail_layout.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-10-11:19.
 * 领导月统计详情
 */

class SignInLeaderMonthDetailActivity : BaseActivity(), SignInLeaderMonthDetailContract.IView, SignInLeaderMonthStatisDetailAdapter
.OnClickeItemListener, BaseSuspensionBar.NotificationBarDataListener {

    private var mAdapter: SignInLeaderMonthStatisDetailAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mCurrentType: Int = 0
    private var mCurrentMonth: String? = null
    private var mPresenter: SignInLeaderMonthDetailPresenter? = null
    private var mToolbar: FEToolbar? = null
    private val mHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_leader_month_summary_detail_layout)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        mToolbar = toolbar
    }

    override fun bindView() {
        super.bindView()
        mLinearLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.setLayoutManager(mLinearLayoutManager)
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
    }

    override fun bindData() {
        super.bindData()
        if (intent == null) return
        mToolbar?.setTitle(intent.getStringExtra(SignInLeaderMonthDetailContract.IView.title))
        mPresenter = SignInLeaderMonthDetailPresenter(this)
        mCurrentType = intent.getIntExtra(SignInLeaderMonthDetailContract.IView.TYPE, 0)
        mCurrentMonth = intent.getStringExtra(SignInLeaderMonthDetailContract.IView.MONTH)
        mAdapter = SignInLeaderMonthStatisDetailAdapter(this, mCurrentType, this)
        mRecyclerView!!.setAdapter(mAdapter)
        mPresenter?.requestMonthDetail(mCurrentMonth, mCurrentType)
    }

    override fun bindListener() {
        super.bindListener()
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

    override fun onMonthSummaryItem(position: Int) {
        mHandler.postDelayed({
            if (mRecyclerView != null) mRecyclerView!!.scrollToPosition(position)
            setScrollListSummaryBar()
        }, 230)
    }

    override fun onMonthMore(userId: String) {
        SignInMonthStatisActivity.start(this, mCurrentMonth!!, userId)
    }

    override fun resultData(summaryItems: List<SignInLeaderMonthDetail>) {
        showError(CommonUtil.isEmptyList(summaryItems))
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
        mMonthSummaryBar!!.updateSuspensionBar(item, mCurrentType)
    }

    override fun onClickBarItem(position: Int) {
        FELog.i("-->>>>>month--bar:" + mMonthSummaryBar!!.getPosition())
        mAdapter!!.showDetaile(position, mAdapter!!.getItem(position))
        onMonthSummaryItem(position)
    }

    override fun onNotificatiionBarData() {
        setScrollListSummaryBar()
    }

    companion object {

        fun start(context: Context, month: String, type: Int, title: String) {
            val intent = Intent(context, SignInLeaderMonthDetailActivity::class.java)
            intent.putExtra(SignInLeaderMonthDetailContract.IView.MONTH, month)
            intent.putExtra(SignInLeaderMonthDetailContract.IView.TYPE, type)
            intent.putExtra(SignInLeaderMonthDetailContract.IView.title, title)
            context.startActivity(intent)
        }
    }
}
