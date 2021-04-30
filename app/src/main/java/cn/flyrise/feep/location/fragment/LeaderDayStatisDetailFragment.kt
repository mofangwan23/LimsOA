package cn.flyrise.feep.location.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.adapter.SignInLeaderDayStatisDetailAdapter
import cn.flyrise.feep.location.model.SignInStatisModel
import kotlinx.android.synthetic.main.location_leader_day_detail_fragment.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * 新建：陈冕;
 * 日期： 2018-5-19-11:43.
 * 领导日统计详情
 */

class LeaderDayStatisDetailFragment : Fragment() {

    private var mCurrentDay: String? = null
    private var mCurrentType: Int = 0
    private var mModel: SignInStatisModel? = null
    private var mAdapter: SignInLeaderDayStatisDetailAdapter? = null
    private var mListener: ((Int) -> Unit)? = null

    private fun setDay(day: String) {
        this.mCurrentDay = day
    }

    private fun setType(type: Int) {
        this.mCurrentType = type
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_leader_day_detail_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = SignInLeaderDayStatisDetailAdapter(context)
        mRecyclerView!!.setAdapter(mAdapter)

        if (mCurrentType == Sign.state.NO_SIGN) {
            mLayoutListError!!.setTitle(resources.getString(R.string.location_leader_sign_no_empty))
            mLayoutListError!!.setEmptyIcon(R.drawable.location_detail_no_sign)
        }
        LoadingHint.show(context)
        mModel = SignInStatisModel()
        mModel!!.requestLeaderDayDetail(mCurrentDay, mCurrentType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    if (LoadingHint.isLoading()) LoadingHint.hide()
                    showError(CommonUtil.isEmptyList(data))
                    mListener?.invoke(if (CommonUtil.isEmptyList(data)) 0 else data.size)
                    mAdapter?.addItem(data)
                }) { error ->
                    if (LoadingHint.isLoading()) LoadingHint.hide()
                    showError(true)
                }
    }

    private fun showError(error: Boolean) {
        mRecyclerView?.visibility = if (error) View.GONE else View.VISIBLE
        mLayoutListError?.visibility = if (error) View.VISIBLE else View.GONE
    }

    companion object {

        fun getInstace(day: String, type: Int, mListener: ((Int) -> Unit)?): LeaderDayStatisDetailFragment {
            val fragment = LeaderDayStatisDetailFragment()
            fragment.setDay(day)
            fragment.setType(type)
            fragment.mListener = mListener
            return fragment
        }
    }
}

