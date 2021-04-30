package cn.flyrise.feep.workplan7.provider

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailRequest
import cn.flyrise.android.protocol.entity.workplan.WorkPlanDetailResponse
import cn.flyrise.android.protocol.model.Flow
import cn.flyrise.android.protocol.model.FlowNode
import cn.flyrise.android.protocol.model.User
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.FileRequest
import cn.flyrise.feep.core.network.request.FileRequestContent
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.network.uploader.UploadManager
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.common.AttachmentBeanConverter
import cn.flyrise.feep.workplan7.contract.PlanCreateContract
import cn.flyrise.feep.workplan7.model.AttendanceRequest
import cn.flyrise.feep.workplan7.model.AttendanceResponse
import cn.flyrise.feep.workplan7.model.NewWorkPlanRequest
import cn.flyrise.feep.workplan7.model.PlanContent
import rx.Observable
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * author : klc
 * Msg :
 */
class PlanCreateProvider : PlanCreateContract.Provider {

    override fun getPlanWeekStart(): Observable<String> {//请求每个星期的第一天是星期几，0：星期天；1、星期一
        return Observable.create {
            FEHttpClient.getInstance().post(AttendanceRequest(), object : ResponseCallback<AttendanceResponse>() {
                override fun onCompleted(t: AttendanceResponse) {
                    if(TextUtils.equals("0",t.errorCode)){
                        it.onNext(t.startWeekDay)
                    }else{
                        it.onError(Throwable("get Plan Week Start error"))
                    }
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                    it.onError(Throwable("get Plan Week Start error"))
                }
            })
        }
    }

    override fun getPlanDetail(planID: String): Observable<PlanContent> {
        return Observable.create {
            val request = WorkPlanDetailRequest()
            request.id = planID
            FEHttpClient.getInstance().post(request, object : ResponseCallback<WorkPlanDetailResponse>() {
                override fun onCompleted(t: WorkPlanDetailResponse) {
                    if (t.errorCode == "0") {
                        val planContent = PlanContent()
                        planContent.planID = t.id
                        planContent.title = t.title
                        planContent.content = t.content
                        planContent.originalAttachment = AttachmentBeanConverter.convert(t.attachments!!)
                        planContent.type = t.type?.toInt()
                        planContent.receiver = folw2PersonList(t.receiveUsers)
                        planContent.cc = folw2PersonList(t.ccUsers)
                        planContent.notifier = folw2PersonList(t.noticeUsers)
                        planContent.attachmentGuid = t.attachmentGUID
                        planContent.startTime = textToCalendar(t.startTime!!)
                        planContent.endTime = textToCalendar(t.endTime!!)
                        it.onNext(planContent)
                    } else {
                        it.onError(Throwable("get WorkPlanDetail error"))
                    }
                }

                override fun onFailure(repositoryException: RepositoryException?) {
                    super.onFailure(repositoryException)
                    it.onError(Throwable("get WorkPlanDetail error"))
                }
            })
        }
    }

    override fun savePlan(context: Context, planContent: PlanContent, progressListener: OnProgressUpdateListenerImpl,
                          responseCallBack: ResponseCallback<ResponseContent>) {
        if (planContent.attachmentGuid.isNullOrEmpty()) {
            planContent.attachmentGuid = UUID.randomUUID().toString()
        }
        val request = NewWorkPlanRequest()
        request.id = planContent.planID
        request.userId = planContent.userId
        request.type = planContent.type?.toString()
        request.title = planContent.title
        request.content = planContent.content
        request.startTime = DateUtil.calendar2StringDate(planContent.startTime)
        request.endTime = DateUtil.calendar2StringDate(planContent.endTime)
        request.receiveUsers = personList2Flow(planContent.receiver)
        request.ccUsers = personList2Flow(planContent.cc)
        request.noticeUsers = personList2Flow(planContent.notifier)
        request.attachments = planContent.attachmentGuid
//        request.setMethod(if (planContent.isCreate!!) "ADD" else "SAVE")
        request.setMethod(if (planContent.isCreate!!) "ADD" else "save")

        val fileRequestContent = FileRequestContent()
        fileRequestContent.attachmentGUID = planContent.attachmentGuid
        fileRequestContent.files = getLocalAttachments(planContent.attachments)
        fileRequestContent.deleteFileIds = getDelNetWorkFileID(planContent.originalAttachment, planContent.attachments)

        val fileRequest = FileRequest()
        fileRequest.fileContent = fileRequestContent
        fileRequest.requestContent = request

        UploadManager(context).fileRequest(fileRequest).progressUpdateListener(progressListener).responseCallback(responseCallBack)
                .execute()

    }

    private fun personList2Flow(persons: List<AddressBook>?): Flow? {
        if (CommonUtil.isEmptyList(persons)) {
            return Flow()
        }
        val userID = CoreZygote.getLoginUserServices().userId
        val userName: String = CoreZygote.getLoginUserServices().userName
        val flow = Flow()
        flow.guid = UUID.randomUUID().toString()
        flow.name = userName
        val nodes = ArrayList<FlowNode>()
        val rootNode = FlowNode()
        rootNode.name = userName
        rootNode.guid = UUID.randomUUID().toString()
        rootNode.value = userID
        rootNode.isEndorse = false
        rootNode.type = X.AddressBookType.Staff
        nodes.add(rootNode)
        flow.nodes = nodes
        rootNode.subnode = ArrayList()
        for (person in persons!!) {
            val flowNode = FlowNode()
            flowNode.name = person.name
            flowNode.guid = UUID.randomUUID().toString()
            flowNode.value = person.userId
            flowNode.isEndorse = false
            flowNode.type = X.AddressBookType.Staff
            rootNode.subnode.add(flowNode)
        }
        return flow
    }

    /**
     * 发送计划的时候会把人员列表转为FLOW流程，再获取暂存内容的时候相对应要转回来
     */
    private fun folw2PersonList(users: java.util.ArrayList<User>): List<AddressBook>? {
        if (CommonUtil.isEmptyList(users)) return null
        return CoreZygote.getAddressBookServices().queryUserIds(users.map { it.id })
    }

    private fun getLocalAttachments(attachments: List<Attachment>?): List<String>? {
        FELog.i("-->>>attachment:${GsonUtil.getInstance().toJson(attachments)}")
        if (CommonUtil.isEmptyList(attachments)) return null
        val localAttachments = ArrayList<String>()
        for (attachment: Attachment in attachments!!) {
            if (attachment !is NetworkAttachment) {
                localAttachments.add(attachment.path)
            }
        }
        return localAttachments
    }

    /**
     * 获取暂存后被删除的网络附件
     */
    private fun getDelNetWorkFileID(nowAttachments: List<Attachment>?, mOriginNetworkAttachments: List<Attachment>?): List<String>? {
        if (CommonUtil.isEmptyList(mOriginNetworkAttachments)) return null
        val deleteIDs = ArrayList<String>()
        if (CommonUtil.isEmptyList(nowAttachments)) {
            for (attachment in mOriginNetworkAttachments!!) {
                if (TextUtils.isEmpty(attachment.id)) continue
                deleteIDs.add(attachment.id)
            }
        } else {
            for (attachment in mOriginNetworkAttachments!!) {
                if (nowAttachments!!.contains(attachment) && !TextUtils.isEmpty(attachment.id)) {
                    deleteIDs.add(attachment.id)
                }
            }
        }
        return deleteIDs
    }

    @SuppressLint("SimpleDateFormat")
    private fun textToCalendar(text: String): Calendar? {
        if (TextUtils.isEmpty(text)) return null
        val simp = SimpleDateFormat("yyyy-MM-dd")
        val calendar = Calendar.getInstance()
        calendar.time = simp.parse(text)
        return calendar
    }

}