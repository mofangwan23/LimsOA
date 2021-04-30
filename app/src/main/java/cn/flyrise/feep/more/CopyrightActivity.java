package cn.flyrise.feep.more;

import android.os.Bundle;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.FEToolbar;

/**
 * @author: ZYP
 * @since 2016-08-11 16:33
 */
public class CopyrightActivity extends BaseActivity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.copyright_layout);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(R.string.about_copyright);
    }

}
