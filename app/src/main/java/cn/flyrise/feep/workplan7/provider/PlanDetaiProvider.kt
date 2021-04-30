package cn.flyrise.feep.workplan7.provider

import android.app.Activity
import cn.flyrise.android.protocol.entity.SendReplyRequest
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailRequest
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.FileRequest
import cn.flyrise.feep.core.network.request.FileRequestContent
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.network.uploader.UploadManager
import cn.flyrise.feep.workplan7.contract.PlanDetailContract
import rx.Observable
import java.util.*

class PlanDetaiProvider : PlanDetailContract.IProvider {

	override fun requestDetailInfo(businessId: String?, messageId: String?): Observable<WorkPlanDetailResponse> {

		return Observable.create {
			val request = WorkPlanDetailRequest()
			request.msgId = messageId
			request.id = businessId
			FEHttpClient.getInstance().post(request, object : ResponseCallback<WorkPlanDetailResponse>() {
				override fun onCompleted(t: WorkPlanDetailResponse) {
					if (t.errorCode == "0") {
						it.onNext(t)
					}
					else {
						it.onError(Throwable(t.errorMessage))
					}
				}

				override fun onFailure(repositoryException: RepositoryException?) {
					super.onFailure(repositoryException)
					it.onError(repositoryException?.exception())
				}
			})
		}
	}

	override fun reply(activity: Activity, planId: String, replyId: String?, replyContent: String, attachments: List<String>?,
		progressListener: OnProgressUpdateListenerImpl?,
		responseCallBack: ResponseCallback<ResponseContent>?) {
		val guid = UUID.randomUUID().toString()
		val fileRequest = FileRequest()
		if (attachments != null && attachments.isNotEmpty()) {
			val fileContent = FileRequestContent()
			fileContent.attachmentGUID = guid
			fileContent.files = attachments
			fileRequest.fileContent = fileContent
		}
		val replyRequest = SendReplyRequest()
		replyRequest.attachmentGUID = guid
		replyRequest.content = replyContent
		replyRequest.id = planId
		replyRequest.replyID = replyId
		replyRequest.replyType = X.ReplyType.WorkPlan
		fileRequest.requestContent = replyRequest

		UploadManager(activity)
			.fileRequest(fileRequest)
			.progressUpdateListener(progressListener)
			.responseCallback(responseCallBack)
			.execute()

	}

}