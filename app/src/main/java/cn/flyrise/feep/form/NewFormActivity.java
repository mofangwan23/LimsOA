package cn.flyrise.feep.form;

import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.X.FormExitType;
import cn.flyrise.feep.core.common.X.FormRequestType;
import cn.flyrise.feep.core.common.X.JSActionType;
import cn.flyrise.feep.form.been.FormDisposeData;
import cn.flyrise.feep.x5.FormMolecule;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NewFormActivity extends FormBrowserActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		if (TextUtils.isEmpty(title)) {
			title = getResources().getString(R.string.approval_from);
		}
		toolbar.setTitle(title);
		toolbar.setRightText(R.string.form_dispose_sendtodo);
		toolbar.setRightTextClickListener(v -> fragment.requestSendForm());
	}

	@Subscribe(threadMode = ThreadMode.MAIN) public void onJSSend(FormMolecule molecule) {
		if (molecule.actionType == formIntent()) {
			FormSendToDisposeActivity.startActivity(NewFormActivity.this, getFormDisposeData(molecule.formKeyId));
		}
	}

	private FormDisposeData getFormDisposeData(String formId) {
		FormDisposeData data = new FormDisposeData();
		data.id = formId;
		data.content = null;
		data.requiredData = null;
		data.requestType = FormRequestType.NewForm;
		data.exitRequestType = FormExitType.NewForm;
		data.isWait = false;
		data.isTrace = true;
		data.isReturnCurrentNode = false;
		return data;
	}

	@Override protected int formIntent() {
		return JSActionType.Send;
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
