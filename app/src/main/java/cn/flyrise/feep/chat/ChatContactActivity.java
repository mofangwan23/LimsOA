package cn.flyrise.feep.chat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.squirtlez.frouter.annotations.Route;

/**
 * Created by klc on 2017/11/17.
 */

@Route("/im/forward")
public class ChatContactActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_contanct);
		String msgID = getIntent().getStringExtra(ChatContanct.EXTRA_FORWARD_MSG_ID);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.layoutContent, ChatContactFragment.getInstance(msgID)).commit();
		setStatusBar();
	}

	private void setStatusBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			boolean statusBarColorApplySuccess = false;
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

			if (FEStatusBar.canModifyStatusBar(getWindow())) {
				statusBarColorApplySuccess = FEStatusBar.setDarkStatusBar(this);
				getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimary));
			}

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				getWindow().setStatusBarColor(getResources().getColor(R.color.defaultColorPrimary));
				return;
			}
			if (!statusBarColorApplySuccess) getWindow().setStatusBarColor(Color.BLACK);
		}
	}
}
