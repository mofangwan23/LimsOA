package cn.flyrise.feep.collection;

import static cn.flyrise.feep.collection.CollectionFolderFragment.MODE_DISPLAY;

import android.os.Bundle;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;

public class MineCollectionActivity extends BaseActivity {

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mine_collection);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle("我的收藏");
	}

	@Override public void bindView() {
		CollectionFolderFragment fragment = CollectionFolderFragment.newInstance(MODE_DISPLAY);
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.layoutFragmentContainer, fragment)
				.show(fragment)
				.commit();
	}
}
