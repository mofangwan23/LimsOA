package cn.flyrise.feep.commonality.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;

public class HelpActivity extends BaseActivity {

    private final String FEEP_UMENG = "HelpActivity";
    private RelativeLayout communicationLayout;
    private RelativeLayout collaborationLayout;
    private RelativeLayout corporateCultureLayout;
    private RelativeLayout knowledgeManagementLayout;
    private ImageView headBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_main);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.reside_menu_item_help);
    }

    @Override
    public void bindView() {
        super.bindView();
        communicationLayout = (RelativeLayout) this.findViewById(R.id.work_communication_layout);
        collaborationLayout = (RelativeLayout) this.findViewById(R.id.collaboration_layout);
        corporateCultureLayout = (RelativeLayout) this.findViewById(R.id.corporate_culture_layout);
        knowledgeManagementLayout = (RelativeLayout) this.findViewById(R.id.knowledge_management_layout);
        headBtn = (ImageView) this.findViewById(R.id.help_head_btn);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        communicationLayout.setOnClickListener(onClickListener);
        collaborationLayout.setOnClickListener(onClickListener);
        corporateCultureLayout.setOnClickListener(onClickListener);
        knowledgeManagementLayout.setOnClickListener(onClickListener);
        headBtn.setOnClickListener(onClickListener);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.work_communication_layout:
                    startActivity(new Intent(HelpActivity.this, HelpWorkCommunicationActivity.class));
                    break;
                case R.id.collaboration_layout:
                    startActivity(new Intent(HelpActivity.this, HelpCollaborationActivity.class));
                    break;
                case R.id.corporate_culture_layout:
                    startActivity(new Intent(HelpActivity.this, HelpCorporateCultureActivity.class));
                    break;
                case R.id.knowledge_management_layout:
                    startActivity(new Intent(HelpActivity.this, HelpKnowledgeManagementActivity.class));
                    break;
                case R.id.help_head_btn:
                    break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        FEUmengCfg.onActivityResumeUMeng(this, FEEP_UMENG);
    }

    @Override
    public void onPause() {
        super.onPause();
        FEUmengCfg.onActivityPauseUMeng(this, FEEP_UMENG);
    }
}
