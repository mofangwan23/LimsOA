package cn.flyrise.feep.collaboration.search

import android.text.TextUtils
import cn.flyrise.android.protocol.entity.ListRequest
import cn.flyrise.android.protocol.entity.ListResponse
import cn.flyrise.android.protocol.model.ListTable
import cn.flyrise.feep.commonality.bean.FEListItem
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback

/**
 * @author ZYP
 * @since 2018-05-16 09:50
 */
interface ApprovalSearchPresenter {

    fun executeRefresh()                // 刷新数据

    fun executeQuery(keyword: String?)  // 查询数据

    fun executeLoadMore()               // 加载更多
}

class ApprovalSearchPresenterImpl(searchView: ApprovalSearchView) : ApprovalSearchPresenter {

    val mSearchView: ApprovalSearchView
    val mSearchRequest: ListRequest

    var mKeyword: String? = null
    var mIsLoading: Boolean = false
    var mTotalSize: Int = 0
    var mPageNumber: Int = 1            // 默认从第一页开始搜索

    init {
        mSearchView = searchView
        mSearchRequest = ListRequest()  // 一个界面包含一个请求就够了呀
        mSearchRequest.perPageNums = "20"
        mSearchRequest.requestType = mSearchView.getRequestType()
    }

    override fun executeRefresh() {
        mPageNumber = 1
        mSearchRequest.page = "${mPageNumber}"
        this.search()
    }

    override fun executeQuery(keyword: String?) {
        mPageNumber = 1
        mSearchRequest.searchKey = keyword
        mSearchRequest.page = "${mPageNumber}"
        this.search()
    }

    override fun executeLoadMore() {
        if (mIsLoading) return        // 正在加载数据
        if (!canLoadMore()) {         // 没有更多数据
            mIsLoading = false
            return
        }

        mIsLoading = true
        mPageNumber++
        mSearchRequest.page = "${mPageNumber}"
        this.search()
    }

    private fun search() {
        FEHttpClient.getInstance().post(mSearchRequest, object : ResponseCallback<ListResponse>() {
            override fun onCompleted(response: ListResponse?) {
                if (response == null || !TextUtils.equals(response?.errorCode, "0")) {
                    onFailure(null)
                    return
                }

                mIsLoading = false
                mTotalSize = CommonUtil.parseInt(response?.totalNums)
                val listItems = convertFEListItem(response?.table, mSearchView.isToDoApproval())
                mSearchView.searchSuccess(listItems, mPageNumber, canLoadMore())
            }

            override fun onFailure(repositoryException: RepositoryException?) {
                mIsLoading = false
                if (mPageNumber > 1) mPageNumber--
                mSearchView.searchFailure()
            }
        })
    }

    private fun canLoadMore(): Boolean = mTotalSize > mPageNumber * 20

    private fun convertFEListItem(listTable: ListTable?, isToDoApproval: Boolean): MutableList<FEListItem> {
        val feListItems = ArrayList<FEListItem>()
        if (listTable == null || CommonUtil.isEmptyList(listTable.tableRows)) return feListItems

        listTable.tableRows.forEach {
            val feListItem = FEListItem()
            it.forEach {
                when (it.name) {
                    "id" -> feListItem.id = it.value
                    "title" -> feListItem.title = it.value
                    "sendUser" -> feListItem.sendUser = it.value
                    "sendUserId" -> feListItem.sendUserId = it.value
                    "sendTime" -> feListItem.sendTime = it.value
                    "important" -> {
                        feListItem.important = if (isToDoApproval) it.value else ""
                    }
                    "level" -> feListItem.level = it.value
                }
            }
            feListItems.add(feListItem)
        }

        return feListItems
    }
}