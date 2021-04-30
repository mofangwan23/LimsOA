package cn.flyrise.feep.commonality.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * 欢迎界面知识管理
 * Created by Administrator on 2016-1-22.
 */
public class HelpKnowledgeManagementActivity extends BaseActivity {

    private RelativeLayout help_km_1;
    private RelativeLayout help_km_2;
    private RelativeLayout help_km_3;
    private RelativeLayout help_km_4;
    private RelativeLayout help_km_5;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_knowledge_management);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.knowledge_management);
    }

    @Override public void bindView() {
        help_km_1 = (RelativeLayout) this.findViewById(R.id.help_km_1);
        help_km_2 = (RelativeLayout) this.findViewById(R.id.help_km_2);
        help_km_3 = (RelativeLayout) this.findViewById(R.id.help_km_3);
        help_km_4 = (RelativeLayout) this.findViewById(R.id.help_km_4);
        help_km_5 = (RelativeLayout) this.findViewById(R.id.help_km_5);
        help_km_3.setVisibility(View.GONE);
        help_km_5.setVisibility(View.GONE);
    }

    @Override
    public void bindListener() {
        help_km_1.setOnClickListener(onClickListener);
        help_km_2.setOnClickListener(onClickListener);
        help_km_3.setOnClickListener(onClickListener);
        help_km_4.setOnClickListener(onClickListener);
        help_km_5.setOnClickListener(onClickListener);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(HelpKnowledgeManagementActivity.this,HelpWebViewActivity.class);
            switch (v.getId()) {
                case R.id.help_km_1:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 15);
                    break;
                case R.id.help_km_2:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 16);
                    break;
                case R.id.help_km_3:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 17);
                    break;
                case R.id.help_km_4:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 18);
                    break;
                case R.id.help_km_5:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 19);
                    break;
            }
            startActivity(intent);
        }
    };
}
