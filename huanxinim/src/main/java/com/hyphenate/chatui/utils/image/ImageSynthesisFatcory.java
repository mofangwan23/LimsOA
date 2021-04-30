package com.hyphenate.chatui.utils.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.ImageView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.GlideRoundTransformation;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCursorResult;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.exceptions.HyphenateException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Create by cm132 on 2018/11/28.
 * Describe:群组图像合成
 */
public class ImageSynthesisFatcory {

	private final static int interval = 6;
	private final static int size = 180;
	private int subSize;
	private ImageView mImg;
	private String groupId;//图片保存名称
	private Context mContext;
	private String filePath;
	private String servicePaht;

	private ImageSynthesisFatcory(Builder builder) {
		if (builder.mImg == null || TextUtils.isEmpty(builder.groupId)) {
			return;
		}
		mContext = builder.context;
		mImg = builder.mImg;
		groupId = builder.groupId;
		subSize = size / 2;
		filePath = CoreZygote.getPathServices().getGroupIconPath();
		servicePaht = CoreZygote.getLoginUserServices().getServerAddress();
		setBimtaps();
	}

	private void setBimtaps() {
		Observable
				.unsafeCreate(f -> {
					File imageFile = new File(filePath, groupId + ".jgp");
					if (imageFile.exists()) {
						f.onNext(imageFile.getPath());
						return;
					}

					List<String> memberList = new ArrayList<>();
					EMCursorResult<String> result;
					try {
						EMGroup group = EMClient.getInstance().groupManager().getGroup(groupId);
						memberList.add(group.getOwner());//群主
						result = EMClient.getInstance().groupManager().fetchGroupMembers(groupId, "", 8);
						memberList.addAll(result.getData());//群成员
					} catch (HyphenateException e) {
						e.printStackTrace();
					}
					if (CommonUtil.isEmptyList(memberList)) {
						f.onError(new NullPointerException("group user null"));
						return;
					}
					List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(memberList);
					List<String> hrefs = new ArrayList<>();
					for (AddressBook info : addressBooks) {
						if (!TextUtils.isEmpty(info.imageHref)) hrefs.add(info.imageHref);
					}
					List<String> paths = hrefs;
					if (hrefs.size() >= 4) {
						paths = hrefs.subList(0, 4);
					}
					if (CommonUtil.isEmptyList(paths)) {
						f.onError(new NullPointerException("paht null"));
						return;
					}
//					Bitmap[] bitmaps = new Bitmap[paths.size()];
					List<Bitmap> bitmaps = new ArrayList<>();
					try {
						for (int i = 0; i < paths.size(); i++) {
							getGlideBitmap(bitmaps, servicePaht + paths.get(i));
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					} catch (ExecutionException e) {
						e.printStackTrace();
					}
					if (bitmaps.size() <= 1) {
						f.onError(new NullPointerException("group user bitmap get error"));
						return;
					}
					final Bitmap bitmap = new WechatLayoutManager().combineBitmap(size, getSubSize(bitmaps.size())
							, interval, Color.parseColor("#E8E8E8"), bitmaps);
					File parentFile = new File(filePath);
					if (!parentFile.exists()) parentFile.mkdirs();
					FileOutputStream fileOutputStream;
					try {
						fileOutputStream = new FileOutputStream(imageFile);
						bitmap.compress(CompressFormat.JPEG, 50, fileOutputStream);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						f.onError(new NullPointerException("paht null"));
						return;
					}
					try {
						fileOutputStream.flush();
						fileOutputStream.close();
					} catch (IOException e) {
						e.printStackTrace();
						f.onError(new NullPointerException("paht null"));
						return;
					}
					f.onNext(imageFile.getPath());
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(path -> {
					if (((Activity) mContext).isFinishing()) return;
					Glide.with(mContext).asBitmap()
							.apply(new RequestOptions()
									.dontAnimate()
									.optionalTransform(new GlideRoundTransformation(mContext))
									.diskCacheStrategy(DiskCacheStrategy.DATA)
									.placeholder(R.drawable.em_group_icon)
									.error(R.drawable.em_group_icon)
							)
							.load(path).into(mImg);
				}, error -> {
					if (((Activity) mContext).isFinishing()) return;
					Glide.with(mContext).asBitmap().load(R.drawable.em_group_icon).into(mImg);
				});
	}

	private void getGlideBitmap(final List<Bitmap> bitmaps, String path) throws InterruptedException, ExecutionException {
		if (((Activity) mContext).isFinishing() || path.contains("/UserUploadFile/photo/photo.png")) return;
		Glide.with(mContext)
				.asBitmap()
				.apply(new RequestOptions()
						.override(subSize, subSize)
						.diskCacheStrategy(DiskCacheStrategy.DATA))
				.listener(new RequestListener<Bitmap>() {
					@Override
					public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(final Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource,
							boolean isFirstResource) {
						bitmaps.add(resource);
						return false;
					}
				})
				.load(path)
				.submit().get();
	}

	private int getSubSize(int count) {//计算子bitmap大小
		if (count < 2) return size;
		else if (count < 5) return (size - interval) / 2;
		return 120;
	}

	public static class Builder {

		private ImageView mImg;
		private String groupId;//图片保存名称
		private Context context;

		public Builder(Context context) {
			this.context = context;
		}

		public Builder setGroupId(String id) {
			this.groupId = id;
			return this;
		}

		public Builder setImageView(ImageView imageView) {
			this.mImg = imageView;
			return this;
		}

		public ImageSynthesisFatcory builder() {
			return new ImageSynthesisFatcory(this);
		}
	}
}
