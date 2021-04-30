package cn.flyrise.feep.form.presenter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.FormSendDoRequest
import cn.flyrise.android.protocol.entity.ReferenceItemsRequest
import cn.flyrise.android.protocol.entity.ReferenceItemsResponse
import cn.flyrise.android.protocol.model.AddressBookItem
import cn.flyrise.android.protocol.model.FormNodeItem
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.R
import cn.flyrise.feep.collaboration.utility.DataStack
import cn.flyrise.feep.commonality.CommonWordsActivity
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.form.FormAddsignActivity
import cn.flyrise.feep.form.FormSendToDisposeActivity
import cn.flyrise.feep.form.been.FormDisposeData
import cn.flyrise.feep.form.contract.FormInputContract
import cn.flyrise.feep.form.util.FormDataProvider
import cn.flyrise.feep.study.view.ElcSignVerifyManager
import cn.flyrise.feep.media.common.LuBan7
import java.text.SimpleDateFormat
import java.util.*

class FormInputPresenter(val mContext: Activity, val mView: FormInputContract.View) : FormInputContract.Presenter {

    private val ADD_ATTACHMENT_REQUEST_CODE = 100//添加附件请求码
    private var mDealType: Int = 0
    private var requestType: Int = 0

    private var added: ArrayList<AddressBookItem>? = arrayListOf()
    protected var mLocalAttachments: List<String>? = null// 本地附件

    private var mId: String? = null
    private var mCurrentFlowNodeGUID: String? = null
    private var mFormDataProvider: FormDataProvider? = null
    private var elcSignVerifyManager: ElcSignVerifyManager? = null

    override fun getCollaborationID() = mId

    override fun getRequstType() = requestType

    //获取Intent传过来的数据
    override fun getIntentData() {
        val intent = mContext.intent
        if (intent != null) {
            mId = intent.getStringExtra("collaborationID")
            mCurrentFlowNodeGUID = intent.getStringExtra("currentFlowNodeGUID")
            val dealTypeValue = intent.getIntExtra("dealTypeValue", 0)
            mDealType = when (dealTypeValue) {
                X.FormNode.Additional -> X.FormNode.Additional
                X.FormNode.Circulated -> X.FormNode.Circulated
                else -> X.FormNode.Normal
            }
            requestType = intent.getIntExtra("requestTypeValue", 0)
            mView.setToolarTitle(requestType)
        }
    }


    override fun selectedAttachment() {
        LuBan7.pufferGrenades(mContext as AppCompatActivity, mLocalAttachments, null, ADD_ATTACHMENT_REQUEST_CODE)
    }

    override fun sendElectSignature() {
        elcSignVerifyManager = ElcSignVerifyManager(mContext)
        elcSignVerifyManager?.setInfoId(mId)
        elcSignVerifyManager?.setAction(if (isReturn) 1 else 0)
        elcSignVerifyManager?.startVerify(object : ElcSignVerifyManager.ElcVerifyCallback {
            override fun onSuccess() {
                submit()
            }

            override fun onFail(msg: String?) {
                FEToast.showMessage(msg)
            }
        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_ATTACHMENT_REQUEST_CODE && data != null) {
            mLocalAttachments = data.getStringArrayListExtra("extra_local_file")
            mView.setAttachmentTitle(if (mLocalAttachments?.size == 0) mContext.getString(R.string.collaboration_attachment)
            else String.format(mContext.getString(R.string.collaboration_has_attachment), mLocalAttachments?.size))
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun submit() {
        val idea = getFormInputText()
        when {
            isSendDo() -> {// 送办
                if (mDealType == X.FormNode.Additional || mDealType == X.FormNode.Circulated) {
                    mFormDataProvider = FormDataProvider(mContext, mId, null)
                    if (!(mFormDataProvider?.isAllowSend ?: false)) return
                    mFormDataProvider?.isAllowSend = false
                    mFormDataProvider?.submit(getFormSendDoRequest(null, idea), mLocalAttachments)
                } else {
                    FormSendToDisposeActivity.startActivity(mContext, getFormDisposeData(idea, X.FormExitType.SendDo))
                }
            }
            isAddSign() -> {//加签
                DataStack.getInstance()[FormAddsignActivity.PERSONKEY] = added
                FormAddsignActivity.startActivity(mContext, getFormSendDoRequest(null, idea), mCurrentFlowNodeGUID, mLocalAttachments)
            }
            isReturn() -> {//退回
                FormSendToDisposeActivity.startActivity(mContext, getFormDisposeData(idea, X.FormExitType.Return))
            }
        }
    }

    private fun getFormDisposeData(idea: String, exitType: Int): FormDisposeData {
        val data = FormDisposeData()
        data.id = mId
        data.content = idea
        data.requiredData = null
        data.requestType = requestType
        data.exitRequestType = exitType
        data.isWait = mView.isWait
        data.isTrace = mView.isTrace
        data.isReturnCurrentNode = mView.isReturnCurrentNode
        data.attachemnts = mLocalAttachments
        return data
    }

    @SuppressLint("SimpleDateFormat")
    private fun getFormInputText(): String {
        val idea: String
        if (!mView.isWritting) { // 意见为打字输入 获取文本内容
            idea = mView.ideaEditText + mContext.getString(R.string.fe_from_android_mobile)
        } else { // 意见为手写 获取图片路径
            val dt = Date()
            val sdf = SimpleDateFormat("yyyyMMdd")
            val datePrefix = sdf.format(dt)
            val GUID = UUID.randomUUID().toString()
            idea = "FEHandwrittenGUID=$datePrefix$GUID"
            mView.saveWrittingBitmap(CoreZygote.getPathServices().tempFilePath + "/handwrittenFiles", "$datePrefix$GUID.png")
        }
        return idea
    }

    //设置FormSendDoRequest的属性
    private fun getFormSendDoRequest(nodeItems: List<FormNodeItem>?, suggestion: String?): FormSendDoRequest {
        val sendDoRequest = FormSendDoRequest()
        sendDoRequest.requestType = requestType
        sendDoRequest.id = mId
        sendDoRequest.dealType = mDealType
        sendDoRequest.suggestion = suggestion
        sendDoRequest.isTrace = mView.isTrace
        sendDoRequest.isWait = mView.isWait
        sendDoRequest.isReturnCurrentNode = mView.isReturnCurrentNode
        sendDoRequest.nodes = nodeItems

        // 加签手写意见 by 罗展健 2015-04-22
        if (suggestion != null && suggestion.startsWith("FEHandwrittenGUID=") && mView.isWritting) { // 意见为手写
            val attachmentGUID = suggestion.replace("FEHandwrittenGUID=", "")
            sendDoRequest.attachmentGUID = attachmentGUID
            sendDoRequest.suggestion = null
        }
        return sendDoRequest
    }

    override fun wordsDialog() {
        val application = mContext.getApplication() as FEApplication
        val commonWords = application.commonWords
        if (commonWords != null) {
            showCommonWordDialog(CommonWordsActivity.convertCommonWord(commonWords))
            return
        }

        LoadingHint.show(mContext)
        val commonWordsReq = ReferenceItemsRequest()
        commonWordsReq.requestType = ReferenceItemsRequest.TYPE_COMMON_WORDS
        FEHttpClient.getInstance().post(commonWordsReq, object : ResponseCallback<ReferenceItemsResponse>(this) {
            override fun onCompleted(responseContent: ReferenceItemsResponse) {
                LoadingHint.hide()
                val items = responseContent.items
                if ("-98" == responseContent.errorCode) {
                    application.commonWords = mContext.getResources().getStringArray(R.array.words)
                } else {
                    application.commonWords = CommonWordsActivity.convertCommonWords(items)
                }
                showCommonWordDialog(CommonWordsActivity.convertCommonWord(application.commonWords))
            }

            override fun onFailure(repositoryException: RepositoryException) {
                LoadingHint.hide()
                FEToast.showMessage(mContext.getResources().getString(R.string.lbl_retry_operator))
            }
        })
    }

    private fun showCommonWordDialog(commonWords: Array<String>) {
        FEMaterialDialog.Builder(mContext)
                .setCancelable(true)
                .setTitle(mContext.getResources().getString(R.string.common_language))
                .setItems(commonWords, { dialog, view, position ->
                    dialog.dismiss()
                    mView.ideaEditText = commonWords[position]
                })
                .setPositiveButton(mContext.getResources().getString(R.string.lbl_text_edit),
                        { dialog -> mContext.startActivity(Intent(mContext, CommonWordsActivity::class.java)) })
                .setNegativeButton(null, null)
                .build()
                .show()
    }

    //判断是否是加签处理
    override fun isAddSign() = requestType == X.FormRequestType.Additional

    //判断是否是正常处理
    override fun isSendDo() = requestType == X.FormRequestType.SendDo

    //判断是否是退回
    override fun isReturn() = requestType == X.FormRequestType.Return

}