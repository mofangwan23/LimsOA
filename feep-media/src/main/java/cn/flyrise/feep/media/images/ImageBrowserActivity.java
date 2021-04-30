package cn.flyrise.feep.media.images;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.component.LargeTouchCheckBox;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.adapter.ImagePreviewAdapter;
import cn.flyrise.feep.media.images.adapter.ImagePreviewAdapter.OnImagePreviewClickListener;
import cn.flyrise.feep.media.images.bean.ImageItem;
import cn.flyrise.feep.media.images.repository.ImageSelectionSpec;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static cn.flyrise.feep.media.common.SelectionSpec.EXTRA_SELECTED_FILES;

/**
 * @author ZYP
 * @since 2017-10-20 11:25
 * 图片浏览器，支持大图缩放，左右滑动
 */
@Route("/media/image/browser")
@RequestExtras({
		"extra_selected_files"    // 用户选择的图片，ArrayList<String> 类型
})
public class ImageBrowserActivity extends BaseActivity implements OnImagePreviewClickListener {

	private ViewPager mViewPager;
	private RelativeLayout mRlTitleBar;
	private RelativeLayout mRlBack;
	private RelativeLayout mRlBottom;
	private TextView mTvSummary;
	private LargeTouchCheckBox mCheckBox;
	private boolean isFullScreen;
	private List<ImageItem> mImageItemList;
	private int selectedPosition;


	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ms_activity_image_browser);
		bindView();
	}

	public void bindView() {
		mTvSummary = (TextView) findViewById(R.id.activity_image_browser_tv_Summary);
		mViewPager = (ViewPager) findViewById(R.id.msViewPager);
		mRlTitleBar = (RelativeLayout) findViewById(R.id.activity_image_browser_rl_titlebar);
		mRlBack = (RelativeLayout) findViewById(R.id.activity_image_browser_tv_back);
        mRlBottom = (RelativeLayout) findViewById(R.id.activity_image_browser_rl_bottom);
		mCheckBox = (LargeTouchCheckBox) findViewById(R.id.activity_image_browser_checkbox);
		mImageItemList = (List<ImageItem>) getIntent().getSerializableExtra(EXTRA_SELECTED_FILES);
		ImagePreviewAdapter adapter = new ImagePreviewAdapter(getSupportFragmentManager());
		adapter.setPreviewImageItems(mImageItemList);
		adapter.setOnImagePreviewClickListener(this);
		mTvSummary.setText(String.format("已选中(%d)", getSelectedImageList(mImageItemList).size()));
		mCheckBox.setChecked(mImageItemList.get(0).isHasSelected());
		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(getIntent().getIntExtra(ImageSelectionSpec.EXTRA_POSITION_SELECTED,0));
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override public void onPageSelected(int position) {
				if(mImageItemList.get(position).isHasSelected()){
					mCheckBox.setChecked(true);
				}else {
					mCheckBox.setChecked(false);
				}
				selectedPosition = position;
			}

			@Override public void onPageScrollStateChanged(int state) {
			}
		});

		mRlBack.setOnClickListener(v -> {
			finish();
		});

		mTvSummary.setOnClickListener(v -> {
			Intent intent = new Intent();
			intent.putExtra(EXTRA_SELECTED_FILES, (Serializable) mImageItemList);
			setResult(RESULT_OK,intent);
			finish();
		});

		mCheckBox.setOnClickListener(v -> {
			ImageItem imageItem = mImageItemList.get(selectedPosition);
            if(mCheckBox.isChecked() && getSelectedImageList(mImageItemList).size()==20){
                  mCheckBox.setChecked(false);
				FEToast.showMessage(String.format("最多只能选择 %d 张图片", getSelectedImageList(mImageItemList).size()));
				return;
			}
			imageItem.setHasSelected(mCheckBox.isChecked());
			mImageItemList.set(selectedPosition,imageItem);
			adapter.setPreviewImageItems(mImageItemList);
			mTvSummary.setText(String.format("已选中(%d)", getSelectedImageList(mImageItemList).size()));
			if(getSelectedImageList(mImageItemList).size()>0){
				mTvSummary.setBackgroundColor(Color.parseColor("#26B7FF"));
				mTvSummary.setClickable(true);
			}else {
				mTvSummary.setBackgroundColor(Color.parseColor("#BFEAFF"));
				mTvSummary.setClickable(false);
			}
		});
	}


	@Override public void onImagePreviewClick() {
		if (isFullScreen) {
			mRlTitleBar.setVisibility(View.VISIBLE);
			mRlBottom.setVisibility(View.VISIBLE);

			mRlTitleBar.clearAnimation();
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.ms_top_translate_enter);
			mRlTitleBar.setAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {
				@Override public void onAnimationStart(Animation animation) {
					getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
				}

				@Override public void onAnimationEnd(Animation animation) {
				}

				@Override public void onAnimationRepeat(Animation animation) {
				}
			});
			animation.start();

			Animation a = AnimationUtils.loadAnimation(this, R.anim.ms_bottom_translate_enter);
			mTvSummary.setAnimation(a);
			a.start();
		}
		else {
			mRlTitleBar.clearAnimation();
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			Animation animation = AnimationUtils.loadAnimation(this, R.anim.ms_top_translate_exit);
			mRlTitleBar.setAnimation(animation);
			animation.setAnimationListener(new AnimationListener() {
				@Override public void onAnimationStart(Animation animation) {
				}

				@Override public void onAnimationEnd(Animation animation) {
					mRlTitleBar.setVisibility(View.GONE);
				}

				@Override public void onAnimationRepeat(Animation animation) {
				}
			});
			animation.start();

			Animation a = AnimationUtils.loadAnimation(this, R.anim.ms_bottom_translate_exit);
            mRlBottom.setAnimation(a);
			a.setAnimationListener(new AnimationListener() {
				@Override public void onAnimationStart(Animation animation) {

				}

				@Override public void onAnimationEnd(Animation animation) {
                    mRlBottom.setVisibility(View.GONE);
				}

				@Override public void onAnimationRepeat(Animation animation) {

				}
			});
			a.start();
		}
		isFullScreen = !isFullScreen;
	}

	public List<ImageItem> getSelectedImageList(List<ImageItem> allImageList) {
		List<ImageItem> selectedImageList = new ArrayList<>();
		for (ImageItem item : allImageList) {
			if(item.isHasSelected()){
				selectedImageList.add(item);
			}
		}
		return selectedImageList;
	}

}
