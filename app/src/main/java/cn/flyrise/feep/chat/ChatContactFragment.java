package cn.flyrise.feep.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.OrganizationStructureActivity;
import cn.flyrise.feep.addressbook.utils.ContactsIntent;
import cn.flyrise.feep.commonality.TheContactPersonSearchActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.main.MainContactFragment;
import cn.flyrise.feep.main.adapter.MainContactAdapter;
import cn.flyrise.feep.main.adapter.MainContactModel;
import com.hyphenate.chatui.group.GroupListActivity;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/11/17.
 * 聊天转发选择联系人Fragment
 */

public class ChatContactFragment extends MainContactFragment {

	private String msgID;

	public static ChatContactFragment getInstance(String msgID) {
		ChatContactFragment fragment = new ChatContactFragment();
		Bundle bundle = new Bundle();
		bundle.putString(ChatContanct.EXTRA_FORWARD_MSG_ID, msgID);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		msgID = bundle.getString(ChatContanct.EXTRA_FORWARD_MSG_ID);
	}


	@Override
	public void bindView(View view) {
		super.bindView(view);
		mToolbar.setNavigationOnClickListener(v -> getActivity().finish());
		mToolbar.setNavigationVisibility(View.VISIBLE);
		mToolbar.setTitle("转发至");
		view.findViewById(R.id.layoutContactSearch).setOnClickListener(v -> {
			Intent intent = new Intent(getActivity(), TheContactPersonSearchActivity.class);
			intent.putExtra(ChatContanct.EXTRA_FORWARD_MSG_ID, msgID);
			startActivity(intent);
		});
		view.findViewById(R.id.layoutContactSearch).setBackgroundColor(Color.WHITE);
	}

	@Override
	public void refreshModels(List<MainContactModel> models) {
		models.remove(new MainContactModel.Builder().setType(MainContactAdapter.TYPE_ALL).build());
		models.remove(new MainContactModel.Builder().setType(MainContactAdapter.TYPE_ATTENTION).build());
		models.remove(new MainContactModel.Builder().setType(MainContactAdapter.TYPE_CUSTOM_CONTACT).build());
		super.refreshModels(models);
	}

	@Override
	public void onItemClick(MainContactModel model) {
		if (model.type == MainContactAdapter.TYPE_COMPANY) {
			Intent intent = new Intent(getActivity(), OrganizationStructureActivity.class);
			intent.putExtra(ChatContanct.EXTRA_FORWARD_MSG_ID, msgID);
			getActivity().startActivity(intent);
		}
		else if (model.type == MainContactAdapter.TYPE_DEPARTMENT) {
			if (TextUtils.isEmpty(model.deptId)) {
				new ContactsIntent(getActivity()).title(CommonUtil.getString(R.string.organizational_structure))
						.forwardMsgId(msgID).startChat().open();
			}
			else {
				new ContactsIntent(getActivity()).title(CommonUtil.getString(R.string.organizational_part_time_department))
						.defaultDepartmentId(model.deptId).forwardMsgId(msgID).startChat().open();
			}
		}
		else if (model.type == MainContactAdapter.TYPE_GROUP_CHAT) {                                     // 新建群聊
			Intent intent = new Intent(getActivity(), GroupListActivity.class);
			intent.putExtra(ChatContanct.EXTRA_FORWARD_MSG_ID, msgID);
			startActivity(intent);
		}
		else if (model.type == MainContactAdapter.TYPE_COMMON_USE) {
			CoreZygote.getAddressBookServices().queryUserDetail(model.userId)
					.subscribe(addressBook -> {
						IMHuanXinHelper.getInstance().forwardMsg2User(getActivity(), addressBook.userId, addressBook.name, msgID);
					}, error -> {

					});
		}
	}
}
