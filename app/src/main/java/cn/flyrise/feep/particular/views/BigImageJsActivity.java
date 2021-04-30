package cn.flyrise.feep.particular.views;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FEStatusBar;
import com.hyphenate.easeui.ui.EaseImagePreviewAdapter;
import com.hyphenate.easeui.ui.EaseImagePreviewAdapter.OnImagePreviewClickListener;
import java.util.List;

public class BigImageJsActivity extends AppCompatActivity implements OnImagePreviewClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_big_image_js);
		FEStatusBar.setupStatusBar(this.getWindow(), Color.TRANSPARENT);
		ViewPager mViewPager = findViewById(R.id.jsViewPager);
//		String imageUrl = getIntent().getExtras().getString("localUrl");
		List<String> imageList = getIntent().getStringArrayListExtra("imageList");
		int position = getIntent().getIntExtra("selectPosition",0);

		EaseImagePreviewAdapter adapter = new EaseImagePreviewAdapter(getSupportFragmentManager());
		adapter.setPreviewImageItems(imageList);
		adapter.setOnImagePreviewClickListener(this);
		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(position);
		mViewPager.setOffscreenPageLimit(2);
	}

	@Override public void onImagePreviewClick() {
		finish();
	}
}
