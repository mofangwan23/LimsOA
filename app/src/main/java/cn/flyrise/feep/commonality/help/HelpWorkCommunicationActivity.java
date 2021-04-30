package cn.flyrise.feep.commonality.help;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * //欢迎界面工作沟通
 * Created by Administrator on 2016-1-22.
 */
public class HelpWorkCommunicationActivity extends BaseActivity {

    private RelativeLayout theconatct_layout;
    private RelativeLayout ims_layout;
    private RelativeLayout im_layout;
    private RelativeLayout message_layout;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_work_communication);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.work_communication);
        toolbar.setLineVisibility(View.GONE);

    }

    @Override public void bindView() {
        theconatct_layout = (RelativeLayout) this.findViewById(R.id.theconatct_layout);
        ims_layout = (RelativeLayout) this.findViewById(R.id.ims_layout);
        im_layout = (RelativeLayout) this.findViewById(R.id.im_layout);
        message_layout = (RelativeLayout) this.findViewById(R.id.message_layout);
        im_layout.setVisibility(View.GONE);
        ims_layout.setVisibility(View.GONE);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        theconatct_layout.setOnClickListener(onClickListener);
        ims_layout.setOnClickListener(onClickListener);
        message_layout.setOnClickListener(onClickListener);
        im_layout.setOnClickListener(onClickListener);
    }

    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(HelpWorkCommunicationActivity.this, HelpWebViewActivity.class);
            switch (v.getId()) {
                case R.id.theconatct_layout:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 0);
                    break;
                case R.id.ims_layout:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 1);
                    break;
                case R.id.im_layout:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 2);
                    break;
                case R.id.message_layout:
                    intent.putExtra(HelpWebViewActivity.OPEN_URL, 3);
                    break;
            }
            startActivity(intent);
        }
    };
}
