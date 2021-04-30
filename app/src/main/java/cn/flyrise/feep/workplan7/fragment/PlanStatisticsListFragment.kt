package cn.flyrise.feep.workplan7.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.event.EventPlanStatisticsListRefresh
import cn.flyrise.feep.workplan7.PlanRuleCreateActivity
import cn.flyrise.feep.workplan7.PlanSubmissionTabActivity
import cn.flyrise.feep.workplan7.adapter.PlanStatisticsListAdapter
import cn.flyrise.feep.workplan7.contract.PlanStaticsListContract
import cn.flyrise.feep.workplan7.model.PlanStatisticsListItem
import cn.flyrise.feep.workplan7.presenter.PlanRuleListPresenter
import kotlinx.android.synthetic.main.plan_fragment_main_statistics.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * KLC
 *计划统计
 * */
class PlanStatisticsListFragment : Fragment(), PlanStaticsListContract.IView {

    private lateinit var mAdapter: PlanStatisticsListAdapter
    private lateinit var mPresenter: PlanStaticsListContract.IPresenter
    private var mListener: ((Int) -> Unit)? = null

    companion object {
        fun getInstance(listener: ((Int) -> Unit)): PlanStatisticsListFragment {
            val fragment = PlanStatisticsListFragment()
            fragment.mListener = listener
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.plan_fragment_main_statistics, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAdapter = PlanStatisticsListAdapter(context!!)
        mPresenter = PlanRuleListPresenter(context!!, this)
        statisticsList.loadMoreRecyclerView.setBackgroundColor(context!!.resources.getColor(R.color.standard_bg_g_30))
        statisticsList.setAdapter(mAdapter)
        statisticsList.setRefreshListener { mPresenter.refresh() }
        statisticsList.setLoadMoreListener { mPresenter.loadMore() }
        mAdapter.clickListener = object : PlanStatisticsListAdapter.ClickListener {
            override fun onRemindClickListener(id: String) {
                mPresenter.remind(id)
            }

            override fun onItemClickListener(item: PlanStatisticsListItem) {
                PlanSubmissionTabActivity.start(context!!, item)
            }
        }

        btCreate.setOnClickListener {
            startActivity(Intent(activity!!, PlanRuleCreateActivity::class.java))
        }
    }

    override fun refreshSuccess(data: List<PlanStatisticsListItem>?, hasMore: Boolean) {
        statisticsList.setRefreshing(false)
        mAdapter.setData(data)
        setEmptyView()
    }

    override fun refreshFail() {
        statisticsList.setRefreshing(false)
        setEmptyView()
    }

    override fun onResume() {
        super.onResume()
        statisticsList.setRefreshing(true)
        mPresenter.refresh()
    }

    private fun setEmptyView() {
        mListener?.invoke(mAdapter.dataSourceCount)
        if (mAdapter.dataSourceCount == 0) {
            lyEmpty.visibility = View.VISIBLE
            listLayout.visibility = View.GONE
        } else {
            lyEmpty.visibility = View.GONE
            listLayout.visibility = View.VISIBLE
        }
    }

    override fun loadMoreSuccess(data: List<PlanStatisticsListItem>, hasMore: Boolean) {
        statisticsList.setRefreshing(false)
        mAdapter.addData(data)
    }

    override fun loadMoreFail() {
        statisticsList.setRefreshing(false)
        statisticsList.scrollLastItem2Bottom()
    }

    override fun showLoading(show: Boolean) {
        if (show) LoadingHint.show(activity)
        else LoadingHint.hide()
    }

    override fun remindError() {
        FEToast.showMessage(getString(R.string.plan_remind_error))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventStatisticsRefresh(refresh: EventPlanStatisticsListRefresh) {
        statisticsList.setRefreshing(true)
        mPresenter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}