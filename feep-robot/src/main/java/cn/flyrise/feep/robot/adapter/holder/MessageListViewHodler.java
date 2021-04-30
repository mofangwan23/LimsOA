package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.Robot;
import cn.flyrise.feep.robot.adapter.RobotUnderstanderAdapter;
import cn.flyrise.feep.robot.bean.FeSearchMessageItem;
import cn.flyrise.feep.robot.module.RobotModuleItem;

/**
 * Created by Administrator on 2017-7-24.
 * 内容列表显示的layout(协同搜索、文档搜索)
 */

public class MessageListViewHodler extends RobotViewHodler {

	private RelativeLayout listLayout;
	private ImageView listIcon;
	private TextView listTitle;
	private TextView listContent;

	private Context mContext;

	private RobotUnderstanderAdapter.OnRobotClickeItemListener mListener;

	public MessageListViewHodler(View itemView, Context context, RobotUnderstanderAdapter.OnRobotClickeItemListener listener) {
		super(itemView);
		this.mContext = context;
		mListener = listener;
		listLayout = itemView.findViewById(R.id.list_content_layout);
		listIcon = itemView.findViewById(R.id.list_icon);
		listTitle = itemView.findViewById(R.id.list_title_tv);
		listContent = itemView.findViewById(R.id.list_content_tv);
	}

	public void setListViewHodler() {
		listLayout.setOnClickListener(v -> {
			if (mListener != null) {
				mListener.onItem(item);
			}
		});
		if (item.addressBook != null) {
			setAddressBook(item.addressBook);
			return;
		}
		if (item.feListItem != null) {
			setListItemData(item, item.feListItem);
			return;
		}

		if (TextUtils.isEmpty(item.title)) {
			listTitle.setVisibility(View.GONE);
		}
		else {
			listTitle.setText(item.title);
		}

		if (TextUtils.isEmpty(item.content)) {
			listContent.setVisibility(View.GONE);
		}
		else {
			listContent.setText(item.content);
		}
		FEImageLoader.load(mContext, listIcon, item.icon, R.drawable.record_on_fe);
	}

	//搜索消息列表
	private void setListItemData(RobotModuleItem item, FeSearchMessageItem listDataItem) {
		if (item.process == Robot.search_message.content_start) {
			listLayout.setBackgroundResource(R.drawable.robot_adapter_list_start_selector);
		}
		else if (item.process == Robot.search_message.content_end) {
			listLayout.setBackgroundResource(R.drawable.robot_adapter_list_end_selector);
		}
		else {
			listLayout.setBackgroundResource(R.drawable.robot_adapter_list_selector);
		}
		String sendUserImg = listDataItem.sendUserImg;
		listTitle.setText(getTitle(listDataItem));
		listContent.setText(getContext(listDataItem));
		if (TextUtils.isEmpty(listDataItem.sendUserId)) {
			listIcon.setVisibility(View.GONE);
		}
		else {
			listIcon.setVisibility(View.VISIBLE);
			if (TextUtils.isEmpty(sendUserImg)) {
				FEImageLoader.load(mContext, listIcon, getModuleIcon(item.moduleParentType));
				return;
			}
			FEImageLoader.load(mContext, listIcon, CoreZygote.getLoginUserServices()
					.getServerAddress() + sendUserImg, listDataItem.sendUserId, listDataItem.sendUser);
		}
	}

	private String getTitle(FeSearchMessageItem item) {
		if (TextUtils.isEmpty(item.title)) {
			return "";
		}
		if (isTextBook(item.title)) {
			return subTextString(item.title);
		}
		return item.title;
	}

	private Spanned getContext(FeSearchMessageItem item) {
		return Html.fromHtml(item.sendUser
				+ "<b>" + mContext.getResources().getString(R.string.robot_message_list_send)
				+ "</b>" + item.sendTime);
	}

	private int getModuleIcon(int moduleId) {
		if (moduleId == 6 || moduleId == 5) {
			return R.drawable.robot_more_say_12;
		}
		return R.drawable.robot_understander_icon;
	}

	//联系人列表
	private void setAddressBook(AddressBook addressBook) {
		if (TextUtils.isEmpty(addressBook.name)) {
			listTitle.setVisibility(View.GONE);
		}
		else {
			listTitle.setText(addressBook.name);
		}
		if (TextUtils.isEmpty(addressBook.deptName)) {
			listContent.setVisibility(View.GONE);
		}
		else {
			listContent.setText(addressBook.deptName);
		}

		FEImageLoader.load(mContext, listIcon
				, CoreZygote.getLoginUserServices().getServerAddress() + getImageHref(addressBook.imageHref, addressBook.userId)
				, addressBook.userId, addressBook.name);
	}

	private String getImageHref(String image, String userId) {
		if (!TextUtils.isEmpty(image) || TextUtils.isEmpty(userId)) {
			return image;
		}
		AddressBook userInfo = CoreZygote.getAddressBookServices().queryUserInfo(userId);
		if (userInfo == null) {
			return image;
		}
		return userInfo.imageHref;
	}

	@Override
	public void onDestroy() {

	}

	public static boolean isTextBook(String title) {
		if (TextUtils.isEmpty(title)) {
			return false;
		}
		StringBuilder buffer = new StringBuilder(title);
		boolean isBook = false;
		int start = buffer.indexOf("《");
		int end = buffer.indexOf("》");
		isBook = start == 0 && end == (buffer.length() - 1);
		return isBook;
	}

	/**
	 * 截取出存在于书名号中的标题
	 */
	public static String subTextString(String title) {
		StringBuilder buffer = new StringBuilder(title);
		String titles = null;
		int start = buffer.indexOf("《");
		int end = buffer.indexOf("》");
		if (start > -1 && end > -1) {
			titles = buffer.substring(start + 1, end);
		}
		else {
			titles = title;
		}
		return titles;
	}
}