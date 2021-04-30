/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-6-11
 */
package cn.flyrise.feep.collaboration.view;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * <b>类功能描述：</b><div style="margin-left:40px;margin-top:-10px"> 头像类控件显示 </div>
 * @author <a href="mailto:184618345@qq.com">017</a>
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class Avatar extends RelativeLayout {

	/**
	 * 名字位于头像右侧
	 */
	public static final int NAMERIGHT = 1;
	/**
	 * 名字位于头像下方
	 */
	private static final int NAMEBELOW = 2;
	/**
	 * 无头像
	 */
	private static final int NOAVATAR = 3;
	/**
	 * 头像,文字不经过截取处理
	 */
	public static final int NAMERIGHT_RLONG_TEXT = 4;

	public static final int DEPARTMENT_SEARCH_TYPE = 5;

	public static final int BUBBLE_NAME_BOTTOM = 6;
	private ImageView head_Iv;
	private TextView name_Tv;
	private int type = NAMEBELOW;
	private String name = "";
	private ImageView tick_Iv;
	private boolean isNeedInterceptName;

	private String mHost;

	public Avatar(Context context) {
		this(context, null);
	}

	public Avatar(int type, Context context) {
		this(type, context, null);
		this.type = type;
	}

	public Avatar(Context context, AttributeSet attrs) {
		this(NAMEBELOW, context, attrs);
	}

	private Avatar(int type, Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutParams lp;
		View mLayout;
		switch (type) {
			case DEPARTMENT_SEARCH_TYPE:
			case NAMERIGHT_RLONG_TEXT:
			case NAMERIGHT:
				lp = new LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				mLayout = LayoutInflater.from(context).inflate(R.layout.avatar_name_right_layout, null);
				head_Iv = (ImageView) mLayout.findViewById(R.id.avatar_icon);
				name_Tv = (TextView) mLayout.findViewById(R.id.avatar_name);
				addView(mLayout, lp);
				break;
			case NAMEBELOW:
				lp = new LayoutParams(PixelUtil.dipToPx(54), ViewGroup.LayoutParams.WRAP_CONTENT);
				mLayout = LayoutInflater.from(context).inflate(R.layout.avatar_name_bottom_layout, null);
				head_Iv = (ImageView) mLayout.findViewById(R.id.avatar_icon);
				name_Tv = (TextView) mLayout.findViewById(R.id.avatar_name);
				tick_Iv = (ImageView) mLayout.findViewById(R.id.avatar_tick);
				lp.addRule(RelativeLayout.CENTER_HORIZONTAL);
				addView(mLayout, lp);
				break;
			case BUBBLE_NAME_BOTTOM:
				lp = new LayoutParams(PixelUtil.dipToPx(52), ViewGroup.LayoutParams.WRAP_CONTENT);
				setLayoutParams(new LayoutParams(PixelUtil.dipToPx(52),
						ViewGroup.LayoutParams.WRAP_CONTENT));
				mLayout = LayoutInflater.from(context)
						.inflate(R.layout.avatar_bubble_name_bottom_layout, null);
				head_Iv = (ImageView) mLayout.findViewById(R.id.avatar_icon);
				name_Tv = (TextView) mLayout.findViewById(R.id.avatar_name);
				tick_Iv = (ImageView) mLayout.findViewById(R.id.avatar_tick);
				addView(mLayout, lp);
				break;
			case NOAVATAR:
				break;
			default:
				break;
		}
		mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setName(CharSequence s) {
		if (s == null) {
			return;
		}
		name = s.toString();
		if (isNeedInterceptName) {
			name = interceptName(name);
		}
		if (name.length() > 5 && name.length() < 10 && type == NAMERIGHT) {
			name_Tv.setText(name.subSequence(0, 5) + "\n" + name.subSequence(5, name.length()));
		}
		else if (name.length() > 10 && type == NAMERIGHT) {
			name_Tv.setText(name.subSequence(0, 5) + "\n" + name.subSequence(5, 9) + "..");
		}
		else if (name.length() == 10 && type == NAMERIGHT) {
			name_Tv.setText(name.subSequence(0, 5) + "\n" + name.subSequence(5, 10));
		}
		else if (name.length() > 4 && type == NAMEBELOW) {
			name_Tv.setText(name.subSequence(0, 4) + "..");
		}
		else {
			name_Tv.setText(name);
		}
	}

	/**
	 * 设置是否需要截取名字
	 */
	public void setNeedInterceptName() {
		this.isNeedInterceptName = true;
	}

	/**
	 * 判断名字中是否有"/",如果有，截取 "/"后面的文本
	 */
	private static String interceptName(String s) {
		if (s != null) {
			final int index = s.lastIndexOf("/");
			if (index != -1) {
				return s.substring(index + 1, s.length());
			}
		}
		return s;
	}


	public void setAvatarFace(int type, String userId, String name, String imageHref) {
		if (type == AddressBookType.Staff || type == AddressBookType.Group) {
			CoreZygote.getAddressBookServices().queryUserDetail(userId)
					.subscribe(f -> {
						FEImageLoader.load(getContext(), head_Iv, getUserImageHref(imageHref,f.imageHref), userId, name);
					}, error -> {
						head_Iv.setImageResource(R.drawable.administrator_icon);
					});
		}
		else if (type == AddressBookType.Position) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_post_fe));
		}
		else if (type == AddressBookType.Company) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_corporation_fe));
		}
		else if (type == AddressBookType.Department) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_department_fe));
		}
	}

	public void setAvatarFace(int type, AddressBookItem item) {
		if (type == AddressBookType.Staff || type == AddressBookType.Group) {
			if (item != null) {
				String userId = item.getId();
				CoreZygote.getAddressBookServices().queryUserDetail(userId)
						.subscribe(f -> {
							FEImageLoader.load(getContext(), head_Iv, getUserImageHref(item.getImageHref(),f.imageHref), userId, item.getName());
						}, error -> {
							head_Iv.setImageResource(R.drawable.administrator_icon);
						});
			}
			else {
				head_Iv.setImageResource(R.drawable.administrator_icon);
			}
		}
		else if (type == AddressBookType.Position) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_post_fe));
		}
		else if (type == AddressBookType.Company) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_corporation_fe));
		}
		else if (type == AddressBookType.Department) {
			head_Iv.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.head_department_fe));
		}
	}

	private String getUserImageHref(String imageHref,String imagePath) {
		if (!TextUtils.isEmpty(imageHref) && !imageHref.contains("/UserUploadFile/photo/photo.png")) {
			return mHost + imageHref;
		}

//		AddressBook addressBook = CoreZygote.getAddressBookServices().queryUserInfo(userId);
//		if (addressBook != null) {
			return mHost + imagePath;
//		}
//		return null;
	}


	public CharSequence getName() {
		return name;
	}

	/**
	 * 设置头像是否已办，打勾勾
	 */
	public void setReaded(boolean isReaded) {
		if (type == NAMEBELOW) {
			if (isReaded) {
				tick_Iv.setVisibility(View.VISIBLE);
			}
			else {
				tick_Iv.setVisibility(View.INVISIBLE);
			}
		}
	}

	/**
	 * 获取组合控件中的图片控件
	 */
	public ImageView getImageView() {
		return head_Iv;
	}

	/**
	 * 获取组合控件中的文字控件
	 */
	public TextView getTextView() {
		return name_Tv;
	}
}
