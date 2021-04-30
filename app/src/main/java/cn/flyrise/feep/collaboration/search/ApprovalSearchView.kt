package cn.flyrise.feep.collaboration.search

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.bean.FEListItem
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.function.AppSubMenu
import cn.flyrise.feep.core.watermark.WMAddressDecoration
import cn.flyrise.feep.core.watermark.WMStamp
import cn.flyrise.feep.notification.NotificationController
import cn.flyrise.feep.particular.ParticularActivity
import cn.flyrise.feep.particular.ParticularIntent
import cn.flyrise.feep.particular.presenter.ParticularPresenter
import com.dk.view.badge.BadgeUtil

/**
 * @author ZYP
 * @since 2018-05-16 09:59
 */
interface ApprovalSearchView {
    fun getRequestType(): Int

    fun isToDoApproval(): Boolean

    fun searchSuccess(list: MutableList<FEListItem>, page: Int, hasMore: Boolean)    // 搜索成功

    fun searchFailure()                                                              // 搜索失败：包含刷新、下拉
}

class ApprovalSearchFragment : Fragment(), ApprovalSearchView {

    private var isNeedRefreshInResume = false
    var mKeyword: String? = null
    var mMenuInfo: AppSubMenu? = null

    lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    lateinit var mRecyclerView: LoadMoreRecyclerView
    lateinit var mAdapter: ApprovalSearchAdapter
    lateinit var mPresenter: ApprovalSearchPresenter

    companion object {
        fun newInstance(menuInfo: AppSubMenu, keyword: String?): ApprovalSearchFragment {
            val app = ApprovalSearchFragment()
            app.mMenuInfo = menuInfo
            app.mKeyword = keyword
            app.mPresenter = ApprovalSearchPresenterImpl(app)
            return app
        }
    }

    override fun getRequestType() = mMenuInfo!!.menuId

    override fun isToDoApproval(): Boolean = mMenuInfo!!.menuId == 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_approval_collaboration, container, false)
        bindView(contentView!!)
        return contentView
    }

    override fun onResume() {
        super.onResume()
        if (isToDoApproval()) {
            if (!TextUtils.isEmpty(mKeyword) && isNeedRefreshInResume) {
                mSwipeRefreshLayout.postDelayed({ mSwipeRefreshLayout.isRefreshing = true }, 500)
                mPresenter.executeQuery(mKeyword)
            }
            isNeedRefreshInResume = true
        }
    }

    private fun bindView(view: View) {
        // 下拉刷新
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent)
        mSwipeRefreshLayout.setOnRefreshListener { mPresenter.executeRefresh() }    // 下拉刷新

        // RecyclerView 相关设置
        val emptyView = view.findViewById(R.id.ivEmptyView) as ImageView
        mAdapter = ApprovalSearchAdapter(emptyView)
        mAdapter.setKeyword(mKeyword)

        mRecyclerView = view.findViewById(R.id.recyclerView)
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.layoutManager = LinearLayoutManager(activity)
        mAdapter.setHasStableIds(true)
        mRecyclerView.adapter = mAdapter

        // 加载更多
        mRecyclerView.setOnLoadMoreListener { mPresenter.executeLoadMore() }

        // 滚动监听
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // 保证只有当 RecyclerView 禁止，并且已经到达顶部时，SwipeRefreshLayout 才可以下拉
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
                    mSwipeRefreshLayout?.setEnabled(firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1))
                }
            }
        })

        // 审批点击
        mAdapter.setOnApprovalItemClickListener {
            if (isToDoApproval() && mAdapter!!.hasImpormantApproval() && TextUtils.isEmpty(it.level)) {
                // 有更重要的待办事项还没处理
                FEToast.showMessage(getString(R.string.collaboration_higher_task_hint))
            }

            if (it.isNews) {
                NotificationController.messageReaded(activity, it.getId())
                val feApplication = activity!!.applicationContext as FEApplication
                val num = feApplication.cornerNum - 1
                BadgeUtil.setBadgeCount(activity, num)
                feApplication.cornerNum = num
            }

            ParticularIntent.Builder(activity)
                    .setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
                    .setTargetClass(ParticularActivity::class.java)
                    .setBusinessId(it.getId())
                    .setListRequestType(mMenuInfo?.menuId ?: -1)
                    .create()
                    .start()
        }

        // 水印
        val watermark = WMStamp.getInstance().waterMarkText
        mRecyclerView.addItemDecoration(WMAddressDecoration(watermark))
    }

    fun executeSearch(keyword: String?) {
        mKeyword = keyword
        if (TextUtils.isEmpty(keyword) || keyword?.trim()?.length == 0) return
        mSwipeRefreshLayout.postDelayed({ mSwipeRefreshLayout.isRefreshing = true }, 200)
        mAdapter.setKeyword(keyword)
        mPresenter.executeQuery(keyword)
    }

    fun executeClear() = mAdapter.clearDataSources()

    override fun searchSuccess(listItems: MutableList<FEListItem>, page: Int, hasMore: Boolean) {
        this.stopRefreshing()
        if (page == 1) {
            mAdapter.setDataSources(listItems)
            if (CommonUtil.nonEmptyList(listItems)) {
                mRecyclerView.scrollToPosition(0)
            }
        } else {
            mAdapter.appendDataSource(listItems)
        }

        if (hasMore) {
            mAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading)
        } else {
            mAdapter.removeFooterView()
        }
    }

    override fun searchFailure() {
        this.stopRefreshing()
        mAdapter.searchFailure()
        mRecyclerView.scrollLastItem2Bottom(mAdapter)
    }

    private inline fun stopRefreshing() {
        mSwipeRefreshLayout.postDelayed({ mSwipeRefreshLayout?.isRefreshing = false }, 1000)
    }

}