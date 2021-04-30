/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-10 上午9:45:40
 */

package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import cn.flyrise.android.protocol.entity.FormSendDoRequest;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.android.protocol.model.FormNodeItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.DataStack;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.cordova.view.CordovaFragment;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.FormExitType;
import cn.flyrise.feep.core.common.X.FormNode;
import cn.flyrise.feep.core.common.X.FormRequestType;
import cn.flyrise.feep.core.common.X.JSActionType;
import cn.flyrise.feep.core.function.IFunctionProxy;
import cn.flyrise.feep.form.been.FormDisposeData;
import cn.flyrise.feep.form.been.FormSendToJSControlInfo;
import cn.flyrise.feep.form.util.FormDataProvider;
import cn.flyrise.feep.study.view.ElcSignVerifyManager;

import java.util.ArrayList;
import java.util.List;


public class FormHandleActivity extends FormCordovaActivity {

	/**
	 * 表单处理类型：0:正常办理，1:加签，2:传阅【重要】
	 */
	private int mDealType;

	private int requestType;

	private String FORM_ID;

	private ArrayList<AddressBookItem> added;
	private FormDataProvider mFormDataProvider;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String title = getResources().getString(R.string.approval_from);
		String webViewUrl = "";
		added = new ArrayList<>();
		if (getIntent() != null) {
			webViewUrl = getIntent().getStringExtra(K.form.URL_DATA_KEY);
			String titleStr = getIntent().getStringExtra(K.form.TITLE_DATA_KEY);
			if (!TextUtils.isEmpty(titleStr)) {
				title = titleStr;
			}
		}
		fragment.loadUrl(CoreZygote.getLoginUserServices().getServerAddress() + webViewUrl);
		fragment.setFormJsActionListener(listener);
		setTooleBar(title);
	}

	private void setTooleBar(String title) {
		getIntentData();
		/*--发送按钮点击监听--*/
		if ((requestType == FormRequestType.SendDo) && (mDealType == FormNode.Additional || mDealType == FormNode.Circulated)) {
			mToolbar.setRightText(R.string.form_submit);
		}
		else {
			mToolbar.setRightText(R.string.form_dispose_sendtodo);
		}
		mToolbar.setRightTextClickListener(new FormClickListener());
	}

	/**
	 * 获取Intent传过来的数据
	 */
	private void getIntentData() {
		final Intent intent = getIntent();
		if (intent != null) {
			FORM_ID = intent.getStringExtra("collaborationID");
			final int dealTypeValue = intent.getIntExtra("dealTypeValue", 0);
			if (dealTypeValue == FormNode.Additional) {
				mDealType = FormNode.Additional;
			}
			else if (dealTypeValue == FormNode.Circulated) {
				mDealType = FormNode.Circulated;
			}
			else {
				mDealType = FormNode.Normal;
			}

			final int requestTypeValue = intent.getIntExtra("requestTypeValue", 0);
			if (requestTypeValue == FormRequestType.Additional) {
				requestType = FormRequestType.Additional;
			}
			else if (requestTypeValue == FormRequestType.SendDo) {
				requestType = FormRequestType.SendDo;
			}
			else if (requestTypeValue == FormRequestType.Return) {
				requestType = FormRequestType.Return;
			}
		}
	}

	private CordovaFragment.FormJsActivionListener listener = new CordovaFragment.FormJsActivionListener() {
		@Override
		public void JSActionSend(JSControlInfo controlInfo) {
		}

		@Override
		public void JSActionGetData(JSControlInfo controlInfo) {
			JSActionGetDatas(controlInfo);
		}

		@Override
		public void JSActionSearch(JSControlInfo controlInfo) {

		}

		@Override
		public void doAfterCheck(JSControlInfo controlInfo) {
			doAfterChecks(controlInfo);
		}
	};

	/**
	 * OnClickListener点击事件
	 */
	private class FormClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (requestType!=FormRequestType.Additional){
				ElcSignVerifyManager signVerifyManager = new ElcSignVerifyManager(FormHandleActivity.this);
				signVerifyManager.setInfoId(FORM_ID);
				signVerifyManager.setAction(requestType);
				signVerifyManager.startVerify(new ElcSignVerifyManager.ElcVerifyCallback() {
					@Override
					public void onSuccess() {
						final FormSendToJSControlInfo info = new FormSendToJSControlInfo();
						info.setActionType(JSActionType.Check); // 检查非空性
						fragment.sendToJavascript(info.getProperties());
					}

					@Override
					public void onFail(String msg) {
						FEToast.showMessage(msg);
					}
				});
			}else {
				final FormSendToJSControlInfo info = new FormSendToJSControlInfo();
				info.setActionType(JSActionType.Check); // 检查非空性
				fragment.sendToJavascript(info.getProperties());
			}
		}
	}

	private void doAfterChecks(JSControlInfo controlInfo) {
		final FormSendToJSControlInfo info = new FormSendToJSControlInfo();
		info.setActionType(JSActionType.FetchData); // actiontype为4 ，获取页面数据
		fragment.sendToJavascript(info.getProperties());
	}

	private void JSActionGetDatas(final JSControlInfo controlInfo) {
		if (requestType == FormRequestType.SendDo) { // 表单送办
			if (mDealType == FormNode.Additional || mDealType == FormNode.Circulated) {// 上一节点加签过来的，并且上勾选了等待
				final FormSendDoRequest sendDoRequest = getFormSendDoRequest(FormRequestType.SendDo, null, FORM_ID,
						controlInfo.getWebData());
				mFormDataProvider = new FormDataProvider(this, FORM_ID, null);
				if (!mFormDataProvider.isAllowSend) return;
				mFormDataProvider.isAllowSend = false;
				mFormDataProvider.submit(sendDoRequest);
			}
			else {
				FormSendToDisposeActivity.startActivity(FormHandleActivity.this, getFormDisposeData(controlInfo.getWebData()));
			}
		}
		else if (requestType == FormRequestType.Additional) {// 表单加签
			DataStack.getInstance().put(FormAddsignActivity.PERSONKEY, added);
			final FormSendDoRequest sendDoRequest = getFormSendDoRequest(FormRequestType.Additional, null, FORM_ID,
					controlInfo.getWebData());
			FormAddsignActivity.startActivity(FormHandleActivity.this, sendDoRequest, FORM_ID, null);
		}
	}

	private FormSendDoRequest getFormSendDoRequest(int requestype, List<FormNodeItem> nodeItems, String Form_ID, String requiredData) {
		final FormSendDoRequest sendDoRequest = new FormSendDoRequest();
		sendDoRequest.setRequestType(requestype);
		sendDoRequest.setId(Form_ID);
		sendDoRequest.setDealType(FormNode.Additional);
		sendDoRequest.setRequiredData(requiredData);
		sendDoRequest.setNodes(nodeItems);
		return sendDoRequest;
	}

	private FormDisposeData getFormDisposeData(String requiredData) {
		FormDisposeData data = new FormDisposeData();
		data.id = FORM_ID;
		data.content = null;
		data.requiredData = requiredData;
		data.requestType = FormRequestType.SendDo;
		data.exitRequestType = FormExitType.SendDo;
		data.isWait = false;
		data.isTrace = true;
		data.isReturnCurrentNode = false;
		return data;
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.FormHandle);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.FormHandle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DataStack.getInstance().remove(FormAddsignActivity.PERSONKEY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}
}

//public class FormHandleActivity extends FormBrowserActivity {
//
//	private String formId;          // 表单 ID
//	private int dealType;           // 表单处理类型：0:正常办理，1:加签，2:传阅【重要】
//	private int requestType;        // 请求类型
//	private ArrayList<AddressBookItem> added;
//
//	@Override protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		EventBus.getDefault().register(this);
//		Intent intent = getIntent();
//		formId = intent.getStringExtra("collaborationID");
//		dealType = intent.getIntExtra("dealTypeValue", FormNode.Normal);
//		requestType = intent.getIntExtra("requestTypeValue", 0);
//	}
//
//	@Override protected void toolBar(FEToolbar toolbar) {
//		if (TextUtils.isEmpty(title)) {
//			title = getResources().getString(R.string.approval_from);
//		}
//		toolbar.setTitle(title);
//
//		String rightText = (requestType == FormRequestType.SendDo)
//				&& (dealType == FormNode.Additional || dealType == FormNode.Circulated)
//				? getString(R.string.form_submit)
//				: getString(R.string.form_dispose_sendtodo);
//		toolbar.setRightText(rightText);
//		toolbar.setRightTextClickListener(view -> fragment.requestSendForm());
//	}
//
//	private FormSendDoRequest getFormSendDoRequest(int requestype, List<FormNodeItem> nodeItems, String Form_ID, String requiredData) {
//		final FormSendDoRequest sendDoRequest = new FormSendDoRequest();
//		sendDoRequest.setRequestType(requestype);
//		sendDoRequest.setId(Form_ID);
//		sendDoRequest.setDealType(FormNode.Additional);
//		sendDoRequest.setRequiredData(requiredData);
//		sendDoRequest.setNodes(nodeItems);
//		return sendDoRequest;
//	}
//
//	@Subscribe(threadMode = ThreadMode.MAIN) public void onJsFetchData(FormMolecule molecule) {
//		if (molecule.actionType == formIntent()) {
//			if (requestType == FormRequestType.Additional) {
//				// 表单加签
//				DataStack.getInstance().put(FormAddsignActivity.PERSONKEY, added);
//				final FormSendDoRequest sendDoRequest = getFormSendDoRequest(FormRequestType.Additional, null, formId, molecule.webData);
//				FormAddsignActivity.startActivity(FormHandleActivity.this, sendDoRequest, formId, null);
//				return;
//			}
//
//			if (dealType == FormNode.Additional || dealType == FormNode.Circulated) {
//				// 上一节点加签过来的，并且上勾选了等待
//				final FormSendDoRequest sendDoRequest = getFormSendDoRequest(FormRequestType.SendDo, null, formId, molecule.webData);
//				new FormDataProvider(this, formId, null).submit(sendDoRequest);
//				return;
//			}
//
//			FormSendToDisposeActivity.startActivity(FormHandleActivity.this, getFormDisposeData(molecule.webData));
//		}
//	}
//
//	private FormDisposeData getFormDisposeData(String requiredData) {
//		FormDisposeData data = new FormDisposeData();
//		data.id = formId;
//		data.content = null;
//		data.requiredData = requiredData;
//		data.requestType = FormRequestType.SendDo;
//		data.exitRequestType = FormExitType.SendDo;
//		data.isWait = false;
//		data.isTrace = true;
//		data.isReturnCurrentNode = false;
//		return data;
//	}
//
//	@Override protected void onDestroy() {
//		DataStack.getInstance().remove(FormAddsignActivity.PERSONKEY);
//		EventBus.getDefault().unregister(this);
//		super.onDestroy();
//	}
//
//
//	@Override protected int formIntent() {
//		return JSActionType.FetchData;
//	}
//}
