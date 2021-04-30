package cn.flyrise.feep.addressbook.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.flyrise.android.protocol.model.CommonGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.CommonGroupAdapter;
import cn.flyrise.feep.addressbook.model.CommonGroupEvent;
import cn.flyrise.feep.addressbook.model.DismissEvent;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * @author ZYP
 * @since 2018-03-23 16:16
 */
public class CommonGroupFilterFragment extends BaseFilterFragment {

	private ListView mListView;
	private CommonGroup mCommonGroup;
	private List<CommonGroup> mCommonGroups;
	private CommonGroupAdapter mCommonGroupAdapter;

	public static CommonGroupFilterFragment newInstance(List<CommonGroup> commonGroups) {
		CommonGroupFilterFragment fragment = new CommonGroupFilterFragment();
		fragment.mCommonGroups = commonGroups;
		return fragment;
	}

	public void setDefaultCommonGroup(CommonGroup commonGroup) {
		this.mCommonGroup = commonGroup;
		if (mCommonGroupAdapter != null) {
			mCommonGroupAdapter.setDefault(commonGroup);
			mCommonGroupAdapter.notifyDataSetChanged();
		}
	}

	@Override public View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_ab_filter_base, container, false);
		mListView = (ListView) view.findViewById(R.id.listView);
		resetContentHeight(mListView = (ListView) view.findViewById(R.id.listView));
		this.initialize();
		return view;
	}

	private void initialize() {
		if (mCommonGroupAdapter == null) {
			mCommonGroupAdapter = new CommonGroupAdapter();
		}

		mCommonGroupAdapter.setDefault(mCommonGroup);
		mCommonGroupAdapter.setData(mCommonGroups);
		mListView.setAdapter(mCommonGroupAdapter);

		mListView.setOnItemClickListener((parent, itemView, position, id) -> {
			CommonGroup selectedGroup = (CommonGroup) mCommonGroupAdapter.getItem(position);
			boolean hasChange = mCommonGroup == null || !TextUtils.equals(mCommonGroup.groupId, selectedGroup.groupId);
			EventBus.getDefault().post(new CommonGroupEvent(mCommonGroup = selectedGroup, hasChange));
			EventBus.getDefault().post(new DismissEvent());
		});
	}
}
