package com.hyphenate.chatui.group.persenter;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMGroupManager;
import com.hyphenate.chat.EMGroupOptions;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.contract.GroupListContract;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.exceptions.HyphenateException;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/3/20.
 */

public class GroupListPresenter implements GroupListContract.IPresenter {

	private GroupListContract.IView mView;

	private List<EMGroup> mGroupList;

	private Context mContext;


	public GroupListPresenter(GroupListContract.IView view, Context context) {
		this.mView = view;
		this.mContext = context;
	}

	@Override
	public void onStart() {
		refreshListData();
	}

	@Override
	public void refreshListData() {
		if (!TextUtils.isEmpty(mView.getSearchKey())) {
			searchGroup(mView.getSearchKey());
			return;
		}
		Observable.create((Observable.OnSubscribe<List<EMGroup>>) subscriber -> {
			try {
				EMClient.getInstance().groupManager().getJoinedGroupsFromServer();// 结果跟本地加载的顺序有很大差别。
				subscriber.onNext(EMClient.getInstance().groupManager().getAllGroups());
			} catch (HyphenateException e) {
				e.printStackTrace();
				subscriber.onError(e);
			}
		}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(f -> {
					mGroupList = f;
					mView.refreshListData(mGroupList);
				}, throwable -> mView.refreshListFail());
	}

	@Override
	public void refreshListData(String searchKey) {

	}

	public void createNewGroup(final List<AddressBook> addressBooks) {
		if (addressBooks.size() == 1) {
			IMHuanXinHelper.getInstance().startChatActivity(mContext, addressBooks.get(0).userId);
			return;
		}
		mView.showLoading();
		Observable
				.create(f -> {
					final String[] userIds = new String[addressBooks.size()];
					//只选择了一个人是单聊：
					for (int i = 0, n = addressBooks.size(); i < n; i++) {
						userIds[i] = IMHuanXinHelper.getInstance().getImUserId(addressBooks.get(i).userId);
					}
					int count = addressBooks.size() >= 3 ? 3 : addressBooks.size();
					StringBuilder groupName = new StringBuilder(CoreZygote.getLoginUserServices().getUserName());
					for (int i = 0; i < count; i++) {
						groupName.append("、").append(addressBooks.get(i).name);
					}
					try {
						EMGroupOptions option = new EMGroupOptions();
						String reason = "邀请加入群";
						reason = EMClient.getInstance().getCurrentUser() + reason + groupName;
						option.maxUsers = 2000;
						option.style = EMGroupManager.EMGroupStyle.EMGroupStylePrivateMemberCanInvite;
						EMGroup group = EMClient.getInstance().groupManager()
								.createGroup(groupName.toString(), "", userIds, reason, option);
						MMPMessageUtil.sendInviteMsg(group.getGroupId(), addressBooks);
						f.onNext(group);
					} catch (Exception e) {
						e.printStackTrace();
						f.onNext(e);
					} finally {
						f.onCompleted();
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(group -> {
					mView.hideLoading();
					if (group == null || !(group instanceof EMGroup)) {
						FEToast.showMessage(R.string.em_txt_create_group_failed);
						return;
					}
					EMGroup emGroup = (EMGroup) group;
					mView.startChatActivity(emGroup);
					EventBus.getDefault().post(200);        // 通知消息列表进行刷新
					refreshListData();
				});
	}

	@Override
	public void searchGroup(String groupName) {
		if (!CommonUtil.isEmptyList(mGroupList)) {
			List<EMGroup> searchResult = new ArrayList<>();
			for (EMGroup emGroup : mGroupList) {
				if (emGroup.getGroupName().contains(groupName)) {
					searchResult.add(emGroup);
				}
			}
			mView.refreshListData(searchResult);
		}
	}

	@Override
	public void loadMoreData() {

	}

	@Override
	public boolean hasMoreData() {
		return false;
	}
}
