package cn.flyrise.feep.location;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.bean.LocationDetailItem;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.softfarique.photoviewlibrary.PhotoView;
import java.io.File;

/**
 * cm
 * 2017-11-22
 * 查看拍照签到详情
 */
public class ShowNetPhotoActivity extends BaseActivity {

	private PhotoView photoView;
	private ProgressBar bar;
	private TextView mTvTitle;
	private TextView mTvAddress;
	private TextView mTvDescribe;
	private TextView mTvDate;

	private LocationDetailItem detailItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.shownetphoto);
	}

	@Override
	protected boolean optionStatusBar() {
		return FEStatusBar.setDarkStatusBar(this);
	}

	@Override
	public void bindView() {
		super.bindView();
		photoView = (PhotoView) this.findViewById(R.id.net_photoview);
		bar = (ProgressBar) this.findViewById(R.id.net_photo_bar);
		mTvTitle = (TextView) this.findViewById(R.id.tv_title);
		mTvAddress = (TextView) this.findViewById(R.id.tv_address);
		mTvDescribe = (TextView) this.findViewById(R.id.tv_describe);
		mTvDate = (TextView) this.findViewById(R.id.tv_date);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(getResources().getString(R.string.location_take_photo_detail));
	}

	@Override
	public void bindData() {
		if (getIntent() != null) {
			final String data = getIntent().getStringExtra("location_detail_data");
			if (TextUtils.isEmpty(data)) {
				return;
			}
			detailItem = GsonUtil.getInstance().fromJson(data, LocationDetailItem.class);
		}
		if (detailItem == null) {
			return;
		}
		mTvTitle.setText(detailItem.title);
		mTvAddress.setText(detailItem.address);
		mTvDate.setText(detailItem.date);
		mTvDescribe.setText(detailItem.describe);
		if (TextUtils.isEmpty(detailItem.iconUrl)) {
			finish();
			return;
		}

		loadImage(detailItem.iconUrl);
	}

	private void loadImage(final String url) {
		Glide.with(this)
				.load(url)
				.apply(new RequestOptions()
						.fitCenter()
						.skipMemoryCache(true) // 不使用内存缓存
						.diskCacheStrategy(DiskCacheStrategy.NONE) // 不使用磁盘缓存
						.signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
				)
				.listener(new RequestListener<Drawable>() {

					@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
							boolean isFirstResource) {
						bar.setVisibility(View.GONE);
						FEToast.showMessage(getResources().getString(R.string.lbl_text_logo_download_error));
						return false;
					}

					@Override
					public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource,
							boolean isFirstResource) {
						bar.setVisibility(View.GONE);
						File file = new File(url);
						if (file.exists()) {
							file.delete();
						}
						return false;
					}
				})
				.into(photoView);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
