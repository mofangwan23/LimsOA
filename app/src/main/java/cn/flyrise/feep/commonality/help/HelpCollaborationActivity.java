package cn.flyrise.feep.commonality.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.function.FunctionManager;

/**
 * 欢迎界面协同办公
 * Created by Administrator on 2016-1-22.
 */
public class HelpCollaborationActivity extends BaseActivity {

	private RelativeLayout help_collaboration_1;
	private RelativeLayout help_collaboration_2;
	private RelativeLayout help_collaboration_3;
	private RelativeLayout help_collaboration_4;
	private RelativeLayout help_collaboration_5;
	private RelativeLayout help_collaboration_6;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_collaboration);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.collaboration_help);
	}

	@Override
	public void bindView() {
		help_collaboration_1 = (RelativeLayout) this.findViewById(R.id.help_collaboration_1);
		help_collaboration_2 = (RelativeLayout) this.findViewById(R.id.help_collaboration_2);
		help_collaboration_3 = (RelativeLayout) this.findViewById(R.id.help_collaboration_3);
		help_collaboration_4 = (RelativeLayout) this.findViewById(R.id.help_collaboration_4);
		help_collaboration_5 = (RelativeLayout) this.findViewById(R.id.help_collaboration_5);
		help_collaboration_6 = (RelativeLayout) this.findViewById(R.id.help_collaboration_6);

		if (!FunctionManager.isAssociateExist()) {
			help_collaboration_5.setVisibility(View.GONE);
			help_collaboration_6.setVisibility(View.GONE);
		}
	}

	@Override public void bindListener() {
		help_collaboration_1.setOnClickListener(onClickListener);
		help_collaboration_2.setOnClickListener(onClickListener);
		help_collaboration_3.setOnClickListener(onClickListener);
		help_collaboration_4.setOnClickListener(onClickListener);
		help_collaboration_5.setOnClickListener(onClickListener);
		help_collaboration_6.setOnClickListener(onClickListener);
	}

	private final View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(HelpCollaborationActivity.this, HelpWebViewActivity.class);
			switch (v.getId()) {
				case R.id.help_collaboration_1:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 4);
					break;
				case R.id.help_collaboration_2:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 5);
					break;
				case R.id.help_collaboration_3:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 6);
					break;
				case R.id.help_collaboration_4:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 7);
					break;
				case R.id.help_collaboration_5:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 8);
					break;
				case R.id.help_collaboration_6:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 9);
					break;
			}
			startActivity(intent);
		}
	};
}
