package cn.flyrise.feep.workplan7.presenter

import android.content.Context
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.workplan7.contract.PlanStaticsListContract
import cn.flyrise.feep.workplan7.provider.PlanStatisticsProvider
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PlanRuleListPresenter(context: Context, view: PlanStaticsListContract.IView) : PlanStaticsListContract.IPresenter {

    private val mContext = context
    private val mProvider = PlanStatisticsProvider()
    private val mView = view

    override fun refresh() {
        mProvider.getStaticsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it != null) mView.refreshSuccess(it.data, true)
                }, {
                    mView.refreshFail()
                })
    }

    override fun loadMore() {
        mProvider.getStaticsList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.loadMoreSuccess(it!!.data!!, false)
                }, {
                    mView.loadMoreFail()
                })
    }

    override fun remind(id: String) {
        mProvider.remind(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    FEToast.showMessage(mContext.getString(R.string.plan_rule_remind_success))
                    refresh()
                }, {
                    mView.remindError()
                })
    }
}