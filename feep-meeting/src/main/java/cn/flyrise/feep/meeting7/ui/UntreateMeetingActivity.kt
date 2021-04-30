package cn.flyrise.feep.meeting7.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.meeting7.selection.time.MINE_TYPE_DONT_DEAL
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.protocol.MeetingListRequest
import cn.flyrise.feep.meeting7.repository.MeetingDataRepository
import cn.flyrise.feep.meeting7.ui.adapter.MineMeetingListAdapter
import cn.flyrise.feep.meeting7.ui.bean.DetailChange
import cn.flyrise.feep.meeting7.ui.bean.MeetingDescription
import cn.flyrise.feep.meeting7.ui.component.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-06-28 18:38
 */
class UntreateMeetingActivity : BaseActivity() {

    private lateinit var statusView: StatusView
    private lateinit var xRecyclerView: XRecyclerView
    private lateinit var adapter: MineMeetingListAdapter

    private var totalPage = 0           // 往下总页数
    private var currentPage = 1         // 当前页码

    private var loadingDialog: FELoadingDialog? = null
    private lateinit var repository: MeetingDataRepository
    private lateinit var loadDataRequest: MeetingListRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setContentView(R.layout.nms_activity_untreate)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        toolbar?.title = "未办理"
    }

    override fun bindView() {
        xRecyclerView = findViewById(R.id.nmsRecyclerView)
        xRecyclerView.layoutManager = LinearLayoutManager(this)
        xRecyclerView.itemAnimator = DefaultItemAnimator()
        xRecyclerView.isFocusableInTouchMode = false
        adapter = MineMeetingListAdapter()
        xRecyclerView.adapter = adapter

        statusView = findViewById(R.id.nmsStatusView)

        val footerView = LayoutInflater.from(this).inflate(
                cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading, xRecyclerView, false)
        xRecyclerView.setFooterView(footerView, object : FooterViewCallback.SimpleFooterViewCallback() {
            override fun onSetNoMore(footerView: View?, noMore: Boolean) {
                xRecyclerView.setLoadingMoreEnabled(!noMore)
            }
        })

        xRecyclerView.setLoadingListener(object : XRecyclerView.LoadingListener {
            override fun onRefresh() {}

            override fun onLoadMore() {
                execute()
            }
        })

        adapter.setOnItemClickListener { description, _ ->
            val intent = Intent(UntreateMeetingActivity@ this, MeetingDetailActivity::class.java)
            intent.putExtra("meetingId", description.meetingId)
            intent.putExtra("meetingType", MINE_TYPE_DONT_DEAL)
            intent.putExtra("requestType", "1")
            startActivity(intent)
        }

        xRecyclerView.setPullRefreshEnabled(false)
        xRecyclerView.setNoMore(true)
    }

    override fun bindData() {
        repository = MeetingDataRepository()
        loadDataRequest = MeetingListRequest()
        loadDataRequest.meetingType = MINE_TYPE_DONT_DEAL
        loadDataRequest.isOver = false

        this.execute()
    }

    private fun execute() {
        if (currentPage == 1) loading(true)
        loadDataRequest.page = currentPage
        repository.requestMeetingList(loadDataRequest)
                .map {
                    this.totalPage = it.totalPage
                    val meetings = mutableListOf<MeetingDescription>()
                    it.data.forEach { meetings.add(MeetingDescription.newInstance(it)) }
                    meetings
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ loadDataSuccess(it) }, { loadDataFailure() })
    }

    private fun loadDataSuccess(dataSource: MutableList<MeetingDescription>) {
        statusView.visibility = View.GONE
        adapter.appendDataSource(dataSource, currentPage == 1)

        if (currentPage == 1) {
            loading(false)
            if (CommonUtil.isEmptyList(dataSource)) {
                statusView.visibility = View.VISIBLE
                statusView.setStatus(STATE_EMPTY)
                xRecyclerView.visibility = View.GONE
            }
        } else {
            xRecyclerView.loadMoreComplete()
        }

        if (currentPage >= totalPage) {
            xRecyclerView.setNoMore(true)
        } else {
            currentPage++
            xRecyclerView.setNoMore(false)
        }

        adapter.notifyDataSetChanged()
    }

    private fun loadDataFailure() {
        currentPage--
        if (currentPage <= 0) currentPage = 1
        if (currentPage == 1) {
            loading(false)
            statusView.visibility = View.VISIBLE
            statusView.setStatus(STATE_ERROR)
            statusView.setOnRetryClickListener(View.OnClickListener {
                execute()
            })
        } else {
            statusView.visibility = View.GONE
            xRecyclerView.loadMoreComplete()
        }
        adapter?.notifyDataSetChanged()
        FEToast.showMessage("数据加载失败，请重试")
    }

    private fun loading(display: Boolean) {
        if (display) {
            if (loadingDialog == null) {
                loadingDialog = FELoadingDialog.Builder(this).setCancelable(false).create()
            }
            loadingDialog!!.show()
        } else {
            if (loadingDialog != null && loadingDialog!!.isShowing()) {
                loadingDialog!!.hide()
                loadingDialog = null
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun detailChange(data: DetailChange) {
        currentPage = 1
        this.execute()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}