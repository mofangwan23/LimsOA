package cn.flyrise.feep.robot.adapter.holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.adapter.RobotUnderstanderAdapter;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * 新建：陈冕;
 * 日期： 2017-8-7-11:10.
 */

public class EmailViewHodler extends RobotViewHodler {

	private TextView mTvUserName;
	private ListView mListView;

	private LinearLayout mLayout;

	private Context mContext;

	public EmailViewHodler(View itemView, Context context) {
		super(itemView);
		mContext = context;
		mTvUserName = itemView.findViewById(R.id.tvUserName);
		mListView = itemView.findViewById(R.id.listView);
		mLayout = itemView.findViewById(R.id.listview_layout);
	}

	public void setEmailViewHodler() {
		String userName = CoreZygote.getLoginUserServices().getUserName();
		mTvUserName.setText(userName);
		List<String> mMailLists = item.textList;
		mTvUserName.setOnClickListener(v ->
				updateAndDismiss(mTvUserName.getText().toString(), item.title));
		if (mMailLists.size() <= 1) {
			mLayout.setVisibility(View.GONE);
			return;
		}
		mLayout.setVisibility(View.VISIBLE);
		LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				PixelUtil.dipToPx(34) * mMailLists.size());
		mListView.setLayoutParams(ll);
		final List<String> strings = mMailLists.subList(1, mMailLists.size());
		mListView.setAdapter(new ArrayAdapter(mContext, R.layout.robot_email_list_item, R.id.text1, strings));
		mListView.setOnItemClickListener((parent, view, positions, id) -> {
			String account = strings.get(positions);
			updateAndDismiss(account, item.title);
		});
	}

	private void updateAndDismiss(String account, String userName) {
		RobotUnderstanderAdapter.OnMailAccountChangeEvent event = new RobotUnderstanderAdapter.OnMailAccountChangeEvent();
		event.setNewAccount(account);
		event.setUserName(userName);
		EventBus.getDefault().post(event);
	}

	@Override
	public void onDestroy() {

	}
}
