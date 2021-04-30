package cn.flyrise.feep.collection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.bean.FavoriteFolder;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;

/**
 * @author ZYP
 * @since 2018-05-23 11:52
 *
 * 收藏夹选择界面
 */
public class CollectionFolderActivity extends BaseActivity {

	private CollectionFolderFragment mFolderFragment;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collection_select);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle("我的收藏");
		int mode = getIntent().getIntExtra("mode", CollectionFolderFragment.MODE_DISPLAY);
		if (mode == CollectionFolderFragment.MODE_SELECT) {
			toolbar.setRightText("完成");
			toolbar.getRightTextView().setTextColor(Color.parseColor("#28B9FF"));
			toolbar.setRightTextClickListener(view -> {
				final FavoriteFolder folder = mFolderFragment.getSelectedFolder();
				if (folder == null) {
					FEToast.showMessage("请选择一个收藏夹");
					return;
				}

				Intent data = new Intent();
				data.putExtra("favoriteId", folder.favoriteId);
				data.putExtra("favoriteName", folder.favoriteName);
				setResult(Activity.RESULT_OK, data);
				finish();
			});
		}
	}

	@Override public void bindView() {
		int mode = getIntent().getIntExtra("mode", CollectionFolderFragment.MODE_DISPLAY);
		mFolderFragment = CollectionFolderFragment.newInstance(mode);
		getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.layoutContent, mFolderFragment)
				.commitAllowingStateLoss();
	}
}
