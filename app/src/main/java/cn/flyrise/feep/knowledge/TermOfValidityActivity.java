package cn.flyrise.feep.knowledge;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.MainMenuRecyclerViewActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;

public class TermOfValidityActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_term_of_validity);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        toolbar.setLineVisibility(View.VISIBLE);
        toolbar.showNavigationIcon();
        toolbar.setTitle(R.string.term_of_validity);
    }
}
