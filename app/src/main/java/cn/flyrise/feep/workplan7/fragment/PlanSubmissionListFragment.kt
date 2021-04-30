package cn.flyrise.feep.workplan7.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.workplan7.PlanSubmissionTabActivity
import cn.flyrise.feep.workplan7.adapter.PlanClassifyListAdapter
import cn.flyrise.feep.workplan7.provider.PlanStatisticsProvider
import kotlinx.android.synthetic.main.paln_classify_list_fragment.*
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * 新建：陈冕;
 *日期： 2018-6-28-14:28.
 * 统计详情
 */

class PlanSubmissionListFragment : Fragment() {

    private var mAdapter: PlanClassifyListAdapter? = null
    private var mType: Int = 0
    private var mDate: String = ""
    private var id: String = ""

    private var mListener: ((Int) -> Unit)? = null

    fun newInstance(id: String, type: Int, date: String): PlanSubmissionListFragment {
        return newInstance(id, type, date, null)
    }

    fun newInstance(id: String, type: Int, date: String, listener: ((Int) -> Unit)?): PlanSubmissionListFragment {
        val fragment = PlanSubmissionListFragment()
        fragment.id = id
        fragment.mType = type
        fragment.mDate = date
        fragment.mListener = listener
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.paln_classify_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindData()
        bindListener()
    }

    private fun bindData() {
        listView!!.setLayoutManager(LinearLayoutManager(context))
        mAdapter = PlanClassifyListAdapter(context!!)
        listView!!.adapter = mAdapter
    }

    private fun bindListener() {
        mTvRemind!!.setOnClickListener({ remind() })
        mLayoutRemindUn!!.setOnClickListener({ remind() })
    }

    fun uploadData(debugList: List<String>, isShowPraise: Boolean, isCurrentDate: Boolean) {
        when {
            mType == PlanSubmissionTabActivity.submission -> {
                ivEmpty!!.setTitle(getString(R.string.plan_classify_error_hint))
                ivEmpty!!.setEmptyIcon(R.drawable.plan_empty)
            }
            isShowPraise -> {
                ivEmpty!!.setTitle(getString(R.string.location_leader_sign_no_empty))
                ivEmpty!!.setEmptyIcon(R.drawable.location_detail_no_sign)
            }
            mType == PlanSubmissionTabActivity.lateDelivery -> {
                ivEmpty!!.setTitle(getString(R.string.plan_classify_error_delivery_hint))
                ivEmpty!!.setEmptyIcon(R.drawable.plan_empty)
            }
            mType == PlanSubmissionTabActivity.unsubmitted -> {
                ivEmpty!!.setTitle(getString(R.string.plan_classify_error_unsubmitted_hint))
                ivEmpty!!.setEmptyIcon(R.drawable.plan_empty)
            }
        }
        uploadData(debugList, isCurrentDate)
    }

    private fun uploadData(debugList: List<String>, isCurrentDate: Boolean) {
        if (listView == null || ivEmptyLayout == null) return
        when {
            mType == PlanSubmissionTabActivity.unsubmitted -> {
                listView!!.visibility = if (CommonUtil.isEmptyList(debugList)) View.GONE else View.VISIBLE
                ivEmptyLayout!!.visibility = if (CommonUtil.isEmptyList(debugList)) View.VISIBLE else View.GONE
                if (mListener != null) mListener!!(if (CommonUtil.isEmptyList(debugList)) 0 else debugList.size)
                mLayoutRemindUn!!.visibility = if (CommonUtil.nonEmptyList(debugList) && isCurrentDate) View.VISIBLE else View.GONE
            }
            mType == PlanSubmissionTabActivity.lateDelivery && CommonUtil.isEmptyList(debugList) -> {
                listView!!.visibility = View.GONE
                ivEmptyLayout!!.visibility = View.VISIBLE
            }
            mType == PlanSubmissionTabActivity.submission && CommonUtil.isEmptyList(debugList) -> {
                listView!!.visibility = View.GONE
                ivEmptyLayout!!.visibility = View.VISIBLE
                mTvRemind!!.visibility = if (isCurrentDate) View.VISIBLE else View.GONE
            }
            else -> {
                listView!!.visibility = View.VISIBLE
                ivEmptyLayout!!.visibility = View.GONE
            }
        }
        mAdapter!!.setData(debugList)
    }

    fun remind() {
        PlanStatisticsProvider().remind(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    FEToast.showMessage(context!!.getString(R.string.plan_rule_remind_success))
                })
    }
}