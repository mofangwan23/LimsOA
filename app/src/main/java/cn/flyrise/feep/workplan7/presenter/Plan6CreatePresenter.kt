package cn.flyrise.feep.workplan7.presenter

import android.app.Activity
import android.content.Context
import android.content.Intent
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.utils.ContactsIntent
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl
import cn.flyrise.feep.core.network.request.ResponseContent
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.media.attachments.bean.Attachment
import cn.flyrise.feep.media.attachments.bean.NetworkAttachment
import cn.flyrise.feep.media.attachments.repository.AttachmentConverter
import cn.flyrise.feep.media.common.LuBan7
import cn.flyrise.feep.workplan7.contract.Plan6CreateContract
import cn.flyrise.feep.workplan7.contract.PlanCreateContract
import cn.flyrise.feep.workplan7.model.PlanContent
import cn.flyrise.feep.workplan7.provider.PlanCreateProvider

/**
 * author : klc
 * Msg :
 */
class Plan6CreatePresenter(view: Plan6CreateContract.IView, context: Context) : Plan6CreateContract.Presenter {

    private val provider = PlanCreateProvider()
    private var planContent = PlanContent()

    private val mView: Plan6CreateContract.IView = view
    private val mContext: Context = context


    override fun clickChooseUser(activity: Activity, requestCode: Int) {
        var persons: List<AddressBook>? = null
        when (requestCode) {
            PlanCreateContract.REQUSETCODE_RECEIVER -> persons = planContent.receiver
            PlanCreateContract.REQUESTCODE_CCUSER -> persons = planContent.cc
            PlanCreateContract.REQUESTCODE_NOTIFIER -> persons = planContent.notifier
        }
        if (CommonUtil.nonEmptyList(persons)) {
            DataKeeper.getInstance().keepDatas(requestCode, persons)
        }
        ContactsIntent(activity).targetHashCode(requestCode).requestCode(requestCode).userCompanyOnly()
                .title(CommonUtil.getString(R.string.lbl_message_title_plan_choose)).withSelect().open()
    }

    override fun handleUserResult(requestCode: Int): Boolean {
        val data = DataKeeper.getInstance().getKeepDatas(requestCode)
        if (data == null) return false
        when (requestCode) {
            PlanCreateContract.REQUSETCODE_RECEIVER -> {
                planContent.receiver = data as List<AddressBook>
                mView.showReceiverUser(planContent.receiver)
                return true
            }
            PlanCreateContract.REQUESTCODE_CCUSER -> {
                planContent.cc = data as List<AddressBook>
                mView.showCCUser(planContent.cc)
                return true
            }
            PlanCreateContract.REQUESTCODE_NOTIFIER -> {
                planContent.notifier = data as List<AddressBook>
                mView.showNotifierUser(planContent.notifier)
                return true
            }
        }
        return false
    }

    override fun clickAttachment(activity: Activity, attachments: List<Attachment>?) {
        val localAttachment = ArrayList<String>()
        val newWorkAttachment = ArrayList<NetworkAttachment>()
        if (!CommonUtil.isEmptyList(attachments)) {
            for (attachment in attachments!!) {
                if (attachment is NetworkAttachment) newWorkAttachment.add(attachment)
                else localAttachment.add(attachment.path)
            }
        }
        LuBan7.pufferGrenades(activity, localAttachment, newWorkAttachment, PlanCreateContract.REQUSETCODE_ATTACHMENT)
    }

    override fun handleAttachmentResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == 101) {
            if (data != null) {
                val localAttachment: List<String>? = data.getStringArrayListExtra("extra_local_file")
                val newWorkAttachment: List<NetworkAttachment>? = data.getParcelableArrayListExtra("extra_network_file")
                val attachments = ArrayList<Attachment>()
                attachments.addAll(newWorkAttachment.orEmpty())
                attachments.addAll(AttachmentConverter.convertAttachments(localAttachment).orEmpty())
                mView.showAttachment(attachments)
            }
            return true
        }
        return false
    }

    override fun createPlan() {
        mView.getViewValue(planContent)
        if (planContent.startTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_start_time_none))
            return
        }
        if (planContent.endTime == null) {
            mView.saveFail(mContext.getString(R.string.plan_create_end_time_none))
            return
        }
        if (planContent.startTime?.timeInMillis ?: 0 > planContent.endTime?.timeInMillis ?: 0) {
            mView.saveFail(mContext.getString(R.string.plan_create_time_error))
            return
        }
        if (planContent.title.isNullOrEmpty()) {
            mView.saveFail(mContext.getString(R.string.plan_create_title_none))
            return
        }
        if (planContent.content.isNullOrEmpty()) {
            mView.saveFail(mContext.getString(R.string.plan_create_context_none))
            return
        }
        if (CommonUtil.isEmptyList(planContent.receiver)) {
            mView.saveFail(mContext.getString(R.string.plan_create_recevier_user_none))
            return
        }
        planContent.isCreate = true
        toSave()
    }

    override fun savePlan() {
        mView.getViewValue(planContent)
        planContent.isCreate = false
        toSave()
    }

    private fun toSave() {
        provider.savePlan(mView.getActivity(), planContent, object : OnProgressUpdateListenerImpl() {
            override fun onPreExecute() {
                super.onPreExecute()
                mView.showLoading()
            }

            override fun onProgressUpdate(currentBytes: Long, contentLength: Long, done: Boolean) {
                val progress = (currentBytes * 100 / contentLength * 1.0f).toInt()
                mView.showProgress(progress)
            }
        }, object : ResponseCallback<ResponseContent>() {
            override fun onCompleted(t: ResponseContent?) {
                mView.hideLoading()
                if ("0" == t!!.errorCode)
                    mView.saveSuccess()
                else
                    mView.saveFail(mContext.getString(R.string.plan_rule_submit_error))
            }

            override fun onFailure(repositoryException: RepositoryException?) {
                super.onFailure(repositoryException)
                mView.hideLoading()
                mView.saveFail(mContext.getString(R.string.plan_rule_submit_error))
            }
        })
    }
}