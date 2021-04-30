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
 * 欢迎界面企业文化
 * Created by Administrator on 2016-1-22.
 */
public class HelpCorporateCultureActivity extends BaseActivity {

	private RelativeLayout help_corporate_culture_2;
	private RelativeLayout help_corporate_culture_3;
	private RelativeLayout help_corporate_culture_4;
	private RelativeLayout help_corporate_culture_1;
	private RelativeLayout help_corporate_culture_5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_corporate_culture);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.corporate_culture_help);
	}

	@Override public void bindView() {
		help_corporate_culture_1 = (RelativeLayout) this.findViewById(R.id.help_corporate_culture_1);
		help_corporate_culture_2 = (RelativeLayout) this.findViewById(R.id.help_corporate_culture_2);
		help_corporate_culture_4 = (RelativeLayout) this.findViewById(R.id.help_corporate_culture_4);
		help_corporate_culture_5 = (RelativeLayout) this.findViewById(R.id.help_corporate_culture_5);

		help_corporate_culture_3 = (RelativeLayout) this.findViewById(R.id.help_corporate_culture_3);
		if (!FunctionManager.isAssociateExist()) {
			help_corporate_culture_3.setVisibility(View.GONE);
		}
		help_corporate_culture_4.setVisibility(View.GONE);

	}

	@Override
	public void bindListener() {
		help_corporate_culture_1.setOnClickListener(onClickListener);
		help_corporate_culture_2.setOnClickListener(onClickListener);
		help_corporate_culture_4.setOnClickListener(onClickListener);
		help_corporate_culture_5.setOnClickListener(onClickListener);
		help_corporate_culture_3.setOnClickListener(onClickListener);
	}

	private final View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(HelpCorporateCultureActivity.this, HelpWebViewActivity.class);
			switch (v.getId()) {
				case R.id.help_corporate_culture_1:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 10);
					break;
				case R.id.help_corporate_culture_2:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 12);
					break;
				case R.id.help_corporate_culture_3:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 13);
					break;
				case R.id.help_corporate_culture_4:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 14);
					break;
				case R.id.help_corporate_culture_5:
					intent.putExtra(HelpWebViewActivity.OPEN_URL, 11);
					break;
			}
			startActivity(intent);
		}
	};
}
