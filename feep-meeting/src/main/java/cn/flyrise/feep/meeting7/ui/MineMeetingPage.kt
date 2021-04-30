package cn.flyrise.feep.meeting7.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.presenter.MineMeetingPresenter
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.selection.time.SP_HIDE_PULL_DOWN_PROMPT
import cn.flyrise.feep.meeting7.ui.adapter.MineMeetingListAdapter
import cn.flyrise.feep.meeting7.ui.bean.DetailChange
import cn.flyrise.feep.meeting7.ui.bean.MeetingDescription
import cn.flyrise.feep.meeting7.ui.component.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * @author ZYP
 * @since 2018-06-19 10:09
 */

interface MineMeetingView {

    fun loadStaleDataSourceSuccess(staleData: MutableList<MeetingDescription>)                   // 成功加载过期数据
    fun loadStaleDataSourceFailure()                                         // 加载过期数据失败
    fun loadDataSourceSuccess(dataSource: MutableList<MeetingDescription>, isFirstLoad: Boolean) // 成功加载更多数据
    fun loadDataSourceFailure(isFirstLoad: Boolean)                                              // 加载更多数据失败
    fun displayPromptHeader()                                                   // 首次进入显示“下拉加载过期数据”的提示
    fun displayUntreatedCount(untreatedCount: Int)                              // 显示未处理数据
    fun enableLoadStaleData(enable: Boolean)                                    // 没有更多过期数据
    fun enableLoadMoreData(enable: Boolean)                                     // 没有更多数据
    fun scrollToRecently(recentlyPosition: Int)
    fun showLoading()
    fun hideLoading()

}

class MineMeetingPage : Fragment(), MineMeetingView {

    companion object {
        fun newInstance(meetingType: String, untreatedCallback: ((Int) -> Unit)?): MineMeetingPage {
            val page = MineMeetingPage()
            page.meetingType = meetingType
            page.untreatedCallback = untreatedCallback
            page.presenter = MineMeetingPresenter(meetingType, page, MeetingDataRepository())
            return page
        }
    }

    private var untreatedCallback: ((Int) -> Unit)? = null
    private var loadingDialog: FELoadingDialog? = null
    private var presenter: MineMeetingPresenter? = null

    private lateinit var meetingType: String
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var mAdapter: MineMeetingListAdapter

    private lateinit var statusView: StatusView
    private lateinit var recentlyLayout: View
    private lateinit var recentlyUpLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        EventBus.getDefault().register(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.nms_fragment_mine_meeting_page, container, false)
        bindView(contentView!!)
        return contentView
    }

    private fun bindView(contentView: View) {
        xRecyclerView = contentView.findViewById(R.id.nmsRecyclerView)
        xRecyclerView.layoutManager = LinearLayoutManager(activity)
        xRecyclerView.itemAnimator = DefaultItemAnimator()
        xRecyclerView.isFocusableInTouchMode = false
        mAdapter = MineMeetingListAdapter()
        xRecyclerView.adapter = mAdapter
        xRecyclerView.setRefreshHeader(ForwardLoadHeaderView(activity))

        statusView = contentView.findViewById(R.id.nmsEmtpyView)
        recentlyLayout = contentView.findViewById(R.id.nmsLayoutRecently)
        recentlyUpLayout = contentView.findViewById(R.id.nmsLayoutRecentlyUp)

        val scrollToRecently = fun(_: View) {
            if (mAdapter.findRecentlyItemPosition() >= 0) {
                (xRecyclerView.getLayoutManager() as LinearLayoutManager).scrollToPositionWithOffset(mAdapter.findRecentlyItemPosition(), 0)
                recentlyLayout.visibility = View.GONE
                recentlyUpLayout.visibility = View.GONE
            }
        }

        contentView.findViewById<TextView>(R.id.nmsTvRecently).setOnClickListener(scrollToRecently)
        contentView.findViewById<TextView>(R.id.nmsTvRecentlyUp).setOnClickListener(scrollToRecently)

        val footerView = LayoutInflater.from(activity).inflate(
                cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading, xRecyclerView, false)

        with(xRecyclerView) {
            setFooterView(footerView, object : FooterViewCallback.SimpleFooterViewCallback() {
                override fun onSetNoMore(footerView: View?, noMore: Boolean) {
                    setLoadingMoreEnabled(!noMore)
                }
            })
            setLoadingListener(object : XRecyclerView.LoadingListener {
                override fun onRefresh() {
                    presenter?.loadStaleDataSource()
                }

                override fun onLoadMore() {
                    presenter?.loadMoreDataSource()
                }
            })
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (xRecyclerView.hasHeaderView()) {
                        xRecyclerView.removeHeaderView()
                        SpUtil.put(SP_HIDE_PULL_DOWN_PROMPT, true)
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    if (newState == SCROLL_STATE_IDLE) {
                        val lm = recyclerView?.layoutManager as? LinearLayoutManager ?: return
                        val firstItemPosition = lm.findFirstVisibleItemPosition()                       // 第一个可见的 Item
                        val recentlyItemPosition = mAdapter.findRecentlyItemPosition()    //  最近的 Item
                        if (recentlyItemPosition >= 0
                                && firstItemPosition < recentlyItemPosition
                                && recentlyItemPosition - firstItemPosition >= 2) {
                            recentlyLayout.visibility = View.VISIBLE
                            recentlyUpLayout.visibility = View.GONE
                        } else if (recentlyItemPosition >= 0
                                && firstItemPosition > recentlyItemPosition
                                && firstItemPosition - recentlyItemPosition >= 2) {
                            recentlyLayout.visibility = View.GONE
                            recentlyUpLayout.visibility = View.VISIBLE
                        } else {
                            recentlyLayout.visibility = View.GONE
                            recentlyUpLayout.visibility = View.GONE
                        }
                    }
                }
            })
            setNoMore(true)
            setPullRefreshEnabled(false)
        }

        mAdapter.setOnItemClickListener { description, _ ->
            val intent = Intent(activity, MeetingDetailActivity::class.java)
            intent.putExtra("meetingId", description.meetingId)
            intent.putExtra("meetingType", meetingType)
            intent.putExtra("requestType", "1")
            activity!!.startActivity(intent)
        }

        presenter?.start()
    }

    fun refresh() {
        presenter?.start()
    }

    override fun displayPromptHeader() {
        val promptView = PromptHeaderView(activity as Context)
        promptView.setOnClickListener {
            xRecyclerView.removeHeaderView()
        }
        xRecyclerView.addHeaderView(promptView)
    }

    override fun loadStaleDataSourceSuccess(staleData: MutableList<MeetingDescription>) {
        statusView.visibility = View.GONE
        var count = staleData.size
        if (mAdapter.itemCount > staleData.size) {
            count = mAdapter.itemCount - staleData.size
        }

        mAdapter.appendToFirst(staleData)
        xRecyclerView.refreshComplete()
        mAdapter.notifyItemRangeInserted(0, staleData.size)

        if (count > 0) {
            (xRecyclerView.getLayoutManager() as LinearLayoutManager).scrollToPositionWithOffset(count, 0)
        }
    }

    override fun loadStaleDataSourceFailure() {
        statusView.visibility = View.VISIBLE
        statusView.setStatus(STATE_ERROR)
        statusView.setOnRetryClickListener(View.OnClickListener {
            presenter?.start()
        })
        xRecyclerView.refreshComplete()
        mAdapter?.notifyDataSetChanged()
        FEToast.showMessage(getString(R.string.meeting7_mine_list_load_error))
    }

    override fun loadDataSourceSuccess(dataSource: MutableList<MeetingDescription>, isFirstLoad: Boolean) {
        statusView.visibility = View.GONE

        if (isFirstLoad) mAdapter.clearDataSource()
        mAdapter.appendDataSource(dataSource, isFirstLoad)
        if (isFirstLoad) {
            if (CommonUtil.isEmptyList(dataSource)) {
                statusView.visibility = View.VISIBLE
                statusView.setStatus(STATE_EMPTY)
            }
        } else {
            xRecyclerView.loadMoreComplete()
        }
        mAdapter.notifyDataSetChanged()
    }

    override fun loadDataSourceFailure(isFirstLoad: Boolean) {
        if (isFirstLoad) {  // 首次加载失败
            statusView.visibility = View.VISIBLE
            statusView.setStatus(STATE_ERROR)
            statusView.setOnRetryClickListener(View.OnClickListener {
                presenter?.start()
            })
        } else {
            statusView.visibility = View.GONE
            xRecyclerView.loadMoreComplete()
        }
        mAdapter?.notifyDataSetChanged()
        FEToast.showMessage(getString(R.string.meeting7_mine_list_load_error))
    }

    override fun displayUntreatedCount(untreatedCount: Int) {
        if (untreatedCallback != null) {
            untreatedCallback!!.invoke(untreatedCount)
        }
    }

    override fun enableLoadMoreData(enable: Boolean) {
        xRecyclerView.setNoMore(!enable)
        mAdapter.notifyDataSetChanged()
    }

    override fun enableLoadStaleData(enable: Boolean) {
        xRecyclerView.setPullRefreshEnabled(enable)
        mAdapter.notifyDataSetChanged()
    }

    override fun scrollToRecently(recentlyPosition: Int) {
        xRecyclerView.smoothScrollToPosition(recentlyPosition)
    }

    override fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = FELoadingDialog.Builder(activity).setCancelable(false).create()
        }
        loadingDialog!!.show()
    }

    override fun hideLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing()) {
            loadingDialog!!.hide()
            loadingDialog = null
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun detailChange(data: DetailChange) {
//        if (TextUtils.equals(data.meetingType, meetingType)) {
        refresh()
//        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }
}