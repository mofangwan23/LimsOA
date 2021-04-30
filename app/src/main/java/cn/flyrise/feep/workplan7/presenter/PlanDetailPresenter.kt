package cn.flyrise.feep.workplan7.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity
import cn.flyrise.feep.collection.FavoriteRepository
import cn.flyrise.feep.collection.bean.CollectionEvent
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.media.common.AttachmentBeanConverter
import cn.flyrise.feep.schedule.NewScheduleActivity
import cn.flyrise.feep.workplan7.contract.PlanDetailContract
import cn.flyrise.feep.workplan7.provider.PlanDetaiProvider
import org.greenrobot.eventbus.EventBus
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PlanDetailPresenter(val mContext: Context,val mView: PlanDetailContract.IView, val intent: Intent) : PlanDetailContract.IPresenter {

    private val prodiver: PlanDetailContract.IProvider = PlanDetaiProvider()
    private val repository = FavoriteRepository()
    private var detailData: WorkPlanDetailResponse? = null
    private val messageId = intent.getStringExtra(K.plan.EXTRA_MESSAGEID)
    private val businessId = intent.getStringExtra(K.plan.EXTRA_BUSINESSID)

    override fun requestDetailInfo() {
        mView.showLoading(true)
        prodiver.requestDetailInfo(businessId, messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mView.showLoading(false)
                    this.detailData = it
                    mView.displayHeadInfo(it)
                    mView.displayContent(it)
                    mView.displayAttachment(AttachmentBeanConverter.convert(it.attachments))
                    mView.displayReplyInfo(it.replies)
//                    mView.showReplyButton(it.sendUserID != CoreZygote.getLoginUserServices().userId)
                }, {
                    mView.showLoading(false)
                    mView.displayDetailFail()
                })
    }

    override fun plan2Collaboration(activity: Activity) {
        if (detailData == null) return
        NewCollaborationActivity.startForWorkPlan(activity, detailData!!.title, detailData!!.content, detailData!!.attachments)
    }

    override fun plan2Schedule(activity: Activity) {
        if (detailData == null) return
        NewScheduleActivity.startActivityFromWorkPlan(
                activity, detailData!!.title, detailData!!.content, detailData!!.attachments
        )
    }

    override fun reply(replyId: String?, replyContent: String, attachments: List<String>?) {
        prodiver.reply(mView.getActivity(), detailData!!.id, replyId, replyContent, attachments,
                object : OnProgressUpdateListenerImpl() {
                    override fun onPreExecute() {
                        mView.showLoading(true)
                    }

                    override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                        val progress = (currentBytes * 100 / contentLength * 1.0f).toInt()
                        mView.showLoadingProgress(progress)
                    }
                },
                object : ResponseCallback<ResponseContent>() {
                    override fun onCompleted(t: ResponseContent) {
                        mView.showLoading(false)
                        if (ResponseContent.OK_CODE != t.errorCode) {
                            mView.replyFail()
                        } else {
                            mView.replySuccess()
                        }
                    }

                    override fun onFailure(repositoryException: RepositoryException?) {
                        super.onFailure(repositoryException)
                        mView.replyFail()
                    }
                })
    }

    /**
     * 添加到收藏夹
     */
    override fun addToFavoriteFolder(favoriteId: String) {
        LoadingHint.show(mView as Context)
        repository.addToFolder(favoriteId, detailData?.id, "${X.Func.Plan}",
                detailData?.title, detailData?.sendUserID, detailData?.sendTime)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    LoadingHint.hide();
                    if (it.errorCode == 0) {
                        FEToast.showMessage(mContext.getString(R.string.plan_collection_success))
                        mView.onFavoriteStateChange(true)
                        detailData?.favoriteId = favoriteId
                        EventBus.getDefault().post(CollectionEvent(200))
                        return@subscribe
                    }
                    FEToast.showMessage(it.errorMessage)
                }, {
                    LoadingHint.hide()
                    it.printStackTrace()
                    FEToast.showMessage(mContext.getString(R.string.plan_collection_failure))
                })
    }

    /**
     * 从收藏夹移除
     */
    override fun removeFromFavoriteFolder() {
        LoadingHint.show(mView as Context)
        repository.removeFromFolder(detailData?.favoriteId, detailData?.id, "${X.Func.Plan}")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    LoadingHint.hide();
                    if (it.errorCode == 0) {
                        FEToast.showMessage(mContext.getString(R.string.plan_cancel_collection_success))
                        mView.onFavoriteStateChange(false)
                        detailData?.favoriteId = ""
                        EventBus.getDefault().post(CollectionEvent(200))
                        return@subscribe
                    }
                    FEToast.showMessage(it.errorMessage)
                }, {
                    LoadingHint.hide()
                    it.printStackTrace()
                    FEToast.showMessage(mContext.getString(R.string.plan_cancel_collection_failure))
                })
    }

}