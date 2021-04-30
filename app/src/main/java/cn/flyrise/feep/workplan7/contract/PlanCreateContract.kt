package cn.flyrise.feep.workplan7.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.workplan7.model.AttendanceResponse
import cn.flyrise.feep.workplan7.model.PlanContent
import rx.Observable

/**
 * author : klc
 * Msg : 新建计划
 */
interface PlanCreateContract {

    companion object {
        val REQUSETCODE_CONTENT = 100
        val REQUSETCODE_ATTACHMENT = 101
        val REQUSETCODE_RECEIVER = 102
        val REQUESTCODE_CCUSER = 103
        val REQUESTCODE_NOTIFIER = 104
    }

    interface IView {
        fun getWeekStartComplete()
        fun showTempData(planContent: PlanContent)
        fun showReceiverUser(users: List<AddressBook>?)
        fun showCCUser(users: List<AddressBook>?)
        fun showNotifierUser(users: List<AddressBook>?)
        fun showAttachment(attachments: ArrayList<Attachment>?)
        fun getViewValue(planContent: PlanContent)
        fun displayWaitSendData(planContent: PlanContent)
        fun showLoading()
        fun showProgress(progress: Int)
        fun hideLoading()
        fun saveSuccess()
        fun saveFail(errorMsg: String)
        fun getActivity(): Activity
    }

    interface Presenter {
        fun initReceiverUser(userIds: ArrayList<String>?)
        fun clickChooseUser(activity: Activity, requestCode: Int)
        fun handleUserResult(requestCode: Int): Boolean
        fun clickAttachment(activity: Activity, attachments: List<Attachment>?)
        fun handleAttachmentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean
        fun getTempPlanData(planId: String)
        fun createPlan()
        fun savePlan()
    }

    interface Provider {
        fun getPlanWeekStart(): Observable<String>
        fun getPlanDetail(planID: String): Observable<PlanContent>
        fun savePlan(
                context: Context,
                planContent: PlanContent,
                progressListener: OnProgressUpdateListenerImpl,
                responseCallBack: ResponseCallback<ResponseContent>
        )
    }

}