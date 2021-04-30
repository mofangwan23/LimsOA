package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import cn.flyrise.feep.R
import cn.flyrise.feep.commonality.view.SwipeLayout
import cn.flyrise.feep.core.base.views.adapter.BaseMessageRecyclerAdapter
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.adapter.NewLocationRecylerAdapter
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.SignPoiItem
import cn.flyrise.feep.location.contract.SignInMainTabContract
import com.amap.api.services.core.PoiItem
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.location_sign_in_nearby_fragment.*
import java.util.concurrent.TimeUnit

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:05.
 * 默认附近列表考勤
 */

open class SignInDefaultNearbyFragment : Fragment(), NewLocationRecylerAdapter.OnItemClickListener {

    protected var mRecylerAdapter: NewLocationRecylerAdapter? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    var mRange: Int = 0

    private var mListener: SignInMainTabContract.SignInTabListener? = null

    val poiItem: List<PoiItem>?
        get() = if (mRecylerAdapter == null) null else mRecylerAdapter!!.poiItem

    fun setListener(listener: SignInMainTabContract.SignInTabListener) {
        this.mListener = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_in_nearby_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mSwipeRefresh!!.setColorSchemeResources(R.color.login_btn_defulit)
        mLinearLayoutManager = LinearLayoutManager(context)
        mRecyclerView?.apply {
            setLayoutManager(mLinearLayoutManager)
            itemAnimator = DefaultItemAnimator()
            itemAnimator!!.changeDuration = 0
            mRecylerAdapter = NewLocationRecylerAdapter()
            adapter = mRecylerAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                var isSlidingToLast = false

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                        val lastVisibleItem = mLinearLayoutManager!!.findLastVisibleItemPosition()//获取最后一个完全显示的ItemPosition
                        val totalItemCount = recyclerView!!.adapter!!.itemCount// 判断是否滚动到底部
                        if (lastVisibleItem == totalItemCount - 1 && isSlidingToLast && mListener != null)
                            mListener!!.onLoadMoreData()//加载更多功能的代码
                    }

                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //滑动列表，延迟列表刷新
                        if (mListener != null) mListener!!.onFrontViewClick()
                        if (mRecylerAdapter != null) mRecylerAdapter!!.closeAllSwipeView()
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    isSlidingToLast = dy > 0
                }
            })
        }
        bindListener()
    }

    private fun bindListener() {
        mSwipeRefresh!!.setOnRefreshListener { if (mListener != null) mListener!!.onRestartLocation() }
        mRecylerAdapter!!.setOnItemClickListener(this)
        RxView.clicks(mTvErrorSignIn!!)//异常签到
                .throttleFirst(2, TimeUnit.SECONDS)
                .subscribe { a -> if (mListener != null) mListener!!.onSignInErrorItem() }
    }

    fun refreshListData(items: List<SignPoiItem>?) {
        nearbyPoiItemError(CommonUtil.isEmptyList(items))
        setSignInButtomEnabled(true)
        mSwipeRefresh!!.isRefreshing = false
        mRecylerAdapter!!.setDataList(items)
    }

    fun loadMoreListData(items: List<SignPoiItem>?) {
        mSwipeRefresh!!.isRefreshing = false
        mRecylerAdapter?.addPoiItems(items)
    }

    fun restartWorkingTime() {//重置数据,消除盖章、重置签到按钮
        mRecylerAdapter?.setCleanAllSignIcon()
        mRecylerAdapter?.notifyDataSetChanged()
    }

    private fun nearbyPoiItemError(isNearbyNull: Boolean) {//周边数据为空，异常拍照
        if (mLayoutNearbyError != null) mLayoutNearbyError!!.visibility = if (isNearbyNull) View.VISIBLE else View.GONE
        if (mSwipeRefresh != null) mSwipeRefresh!!.visibility = if (isNearbyNull) View.GONE else View.VISIBLE
    }

    fun setRefreshing(isRefresh: Boolean) {
        if (mSwipeRefresh != null) mSwipeRefresh!!.isRefreshing = isRefresh
    }

    fun setSwipeRefreshEnabled(isEnabled: Boolean) {
        if (mSwipeRefresh != null) mSwipeRefresh!!.isEnabled = isEnabled
    }

    fun signSuccessShowIcon() {
        mRecyclerView!!.scrollToPosition(mRecylerAdapter!!.selectedPosition)
        mRecylerAdapter!!.signSuccessSealPoiItem()
    }

    fun setSignInButtomEnabled(isEnabled: Boolean) {
        mRecylerAdapter!!.setEnabled(isEnabled)
    }

    fun loadMoreState(state: Int) {
        if (state == Sign.loadMore.can_load_more) {
            mRecylerAdapter!!.setLoadState(BaseMessageRecyclerAdapter.LOADING)
        } else if (state == Sign.loadMore.no_load_more) {
            mRecylerAdapter!!.setLoadState(BaseMessageRecyclerAdapter.LOADING_END)
        } else if (state == Sign.loadMore.success_load_more) {
            mRecylerAdapter!!.setLoadState(BaseMessageRecyclerAdapter.LOADING_COMPLETE)
        }
    }

    override fun onSignInClick(swipeLayout: SwipeLayout, saveItem: LocationSaveItem, position: Int) {//点击签到
        if (mListener != null) mListener!!.onSignInItem(saveItem)
    }

    override fun onFrontViewClick() {//点开后延迟刷新位置
        if (mListener != null) mListener!!.onFrontViewClick()
    }

    override fun onSignWorkingClick() {

    }

    companion object {

        fun getInstance(listener: SignInMainTabContract.SignInTabListener): SignInDefaultNearbyFragment {
            val fragment = SignInDefaultNearbyFragment()
            fragment.setListener(listener)
            return fragment
        }
    }
}
