package cn.flyrise.feep.workplan7.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.event.EventPlanListRefresh
import cn.flyrise.feep.notification.NotificationController
import cn.flyrise.feep.utils.Patches
import cn.flyrise.feep.workplan7.PlanDetailActivity
import cn.flyrise.feep.workplan7.PlanFilterActivity
import cn.flyrise.feep.workplan7.adapter.PlanListAdapter
import cn.flyrise.feep.workplan7.contract.PlanListContract
import cn.flyrise.feep.workplan7.model.PlanFilterContent
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean
import cn.flyrise.feep.workplan7.presenter.Plan6ListPresenter
import cn.flyrise.feep.workplan7.presenter.Plan7ListPresenter
import com.dk.view.badge.BadgeUtil
import kotlinx.android.synthetic.main.plan_fragment_list.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PlanListFragment : Fragment(), PlanListContract.IView {

    private lateinit var mAdapter: PlanListAdapter
    private lateinit var mPresenter: PlanListContract.IPresenter
    private var isReceive: Boolean = false
    private var isFilter: Boolean = false
    private var userId: String? = null
    private var hasMoreData: Boolean = false

    companion object {
        fun newInstance(isReceiver: Boolean): PlanListFragment {
            val fragment = PlanListFragment()
            fragment.isReceive = isReceiver
            return fragment
        }

        fun newInstance(isReceiver: Boolean, userId: String): PlanListFragment {
            val fragment = PlanListFragment()
            fragment.isReceive = isReceiver
            fragment.userId = userId
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        return inflater.inflate(R.layout.plan_fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAdapter = PlanListAdapter(activity!!, isReceive)
        mAdapter.setHasStableIds(true)
        planList.setAdapter(mAdapter)
        planList.setLayoutManager(LinearLayoutManager(context))
        planList.setItemAnimator(DefaultItemAnimator())
        listRefresh.setColorSchemeResources(cn.flyrise.feep.core.R.color.core_default_accent_color)
        mPresenter = getPresenter()
        listRefresh.setOnRefreshListener { mPresenter.refresh() }
        planList.setOnLoadMoreListener {
            if (!hasMoreData) return@setOnLoadMoreListener
            mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING)
            mPresenter.loadMore()
        }
        tvFilter.visibility = if (FunctionManager.hasPatch(Patches.PATCH_PLAN) && isReceive) View.VISIBLE else View.GONE
        bindListener()
        if (!userId.isNullOrEmpty()) mPresenter.setFilterUser(userId)
    }

    override fun onResume() {
        super.onResume()
        listRefresh.setRefreshing(true)
        mPresenter.refresh()
    }

    private fun isShowFilter(data: List<WorkPlanListItemBean>?) = FunctionManager.hasPatch(Patches.PATCH_PLAN)
            && isReceive && !CommonUtil.isEmptyList(data)

    private fun bindListener() {
        tvFilter.setOnClickListener {
            val intent = Intent(activity, PlanFilterActivity::class.java)
            val filter = mPresenter.getFilterContent()
            if (filter != null) intent.putExtra("filter", filter)
            startActivityForResult(intent, 100)
        }

        mAdapter.setOnItemClickListener { _, clickItem ->
            val item = clickItem as WorkPlanListItemBean
            if (item.isNews()) {
                NotificationController.messageReaded(context, item.getId())
                val feApplication = context!!.getApplicationContext() as FEApplication
                val num = feApplication.cornerNum - 1
                BadgeUtil.setBadgeCount(context, num)//角标
                feApplication.cornerNum = num
            }
            PlanDetailActivity.startActivity(activity!!, "", item.id)
        }
    }

    private fun getPresenter(): PlanListContract.IPresenter {
        return if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
            Plan7ListPresenter(this, isReceive)
        } else {
            Plan6ListPresenter(this, isReceive)
        }
    }

    override fun refreshSuccess(data: ArrayList<WorkPlanListItemBean>, hasMore: Boolean) {
        if (activity?.isFinishing ?: true) return
        mAdapter.setData(ArrayList(data))
        hasMoreData = hasMore
        mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE)
        setEmpty(data)
        planList?.postDelayed({ listRefresh?.setRefreshing(false) }, 500)
    }

    override fun refreshFail() {
        if (activity?.isFinishing ?: true) return
        planList.postDelayed({
            listRefresh.setRefreshing(false)
            setEmpty(mAdapter.getData())
        }, 500)
    }

    override fun loadMoreSuccess(data: ArrayList<WorkPlanListItemBean>, hasMore: Boolean) {
        if (activity?.isFinishing ?: true) return
        mAdapter.addData(ArrayList(data))
        hasMoreData = hasMore
        if (hasMore) {
            mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE)
        } else {
            mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_END)
        }
    }

    override fun loadMoreFail() {
        if (activity?.isFinishing ?: true) return
        mAdapter.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE)
    }

    private fun setEmpty(data: List<WorkPlanListItemBean>?) {
        tvFilter.visibility = if (isShowFilter(data) || isFilter) View.VISIBLE else View.GONE
        if (CommonUtil.isEmptyList(data)) {
            planList.visibility = View.GONE
            lyEmpty.visibility = View.VISIBLE
        } else {
            planList.visibility = View.VISIBLE
            lyEmpty.visibility = View.GONE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val filter = data.getParcelableExtra<PlanFilterContent>("filter")
            if (filter.userIDs.isNullOrEmpty() && filter.endTime == null && filter.startTime == null && filter.type == null) {
                tvFilter.setTextColor(Color.parseColor("#FF9DA3A6"))
            } else {
                tvFilter.setTextColor(Color.parseColor("#FF28B9FF"))
            }
            isFilter = true
            mPresenter.setFilterContent(filter)
            mPresenter.refresh()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventListRefresh(eventPlanListRefresh: EventPlanListRefresh) {
        mPresenter.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}