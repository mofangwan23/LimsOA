package cn.flyrise.feep.workplan7.contract

import android.app.Activity
import android.content.Intent
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.android.protocol.model.Reply
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.particular.ParticularIntent
import rx.Observable

interface PlanDetailContract {

    interface IView {
        fun displayHeadInfo(detail: WorkPlanDetailResponse)
        fun displayContent(detail: WorkPlanDetailResponse)
        fun displayAttachment(attachment: List<Attachment>?)
        fun displayReplyInfo(replys: List<Reply>?)
        fun displayDetailFail()
        fun showLoading(show: Boolean)
        fun showLoadingProgress(progress: Int)
        fun showReplyButton(show: Boolean)
        fun replySuccess()
        fun replyFail()
        fun getActivity(): Activity
        fun onFavoriteStateChange(isAdd: Boolean)
    }

    interface IPresenter {
        fun requestDetailInfo()
        fun plan2Collaboration(activity: Activity)
        fun plan2Schedule(activity: Activity)
        fun reply(replyId: String?, replyContent: String, attachments: List<String>?)
        fun addToFavoriteFolder(favoriteId: String)
        fun removeFromFavoriteFolder()
    }

    interface IProvider {
        fun requestDetailInfo(businessId: String?, messageId: String?): Observable<WorkPlanDetailResponse>
        fun reply(
                activity: Activity, planId: String, replyId: String?, replyContent: String,
                attachments: List<String>?, progressListener: OnProgressUpdateListenerImpl?,
                responseCallBack: ResponseCallback<ResponseContent>?
        )
    }
}