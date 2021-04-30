package com.hyphenate.chatui.retrieval;

import android.text.TextUtils;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chatui.R;

/**
 * @author ZYP
 * @since 2018-05-08 18:14
 */
@Route("/im/chat/search/group")
public class GroupSearchActivity extends FESearchListActivity<GroupInfo> {

	private String mKeyword;
	private GroupSearchAdapter mAdapter;
	private GroupSearchPresenter mPresenter;

	@Override
	public void bindView() {
		super.bindView();
		this.listView.setCanRefresh(false);
	}

	@Override
	public void bindData() {
		et_Search.setHint(R.string.input_msg_key);
		mKeyword = getIntent().getStringExtra("keyword");

		mAdapter = new GroupSearchAdapter(this);
		mPresenter = new GroupSearchPresenter(this);
		setAdapter(mAdapter);
		setPresenter(mPresenter);

		if (!TextUtils.isEmpty(mKeyword)) {
			et_Search.setText(mKeyword);
			et_Search.setSelection(mKeyword.length());
			searchKey = mKeyword;
			myHandler.post(searchRunnable);
		}
		else {
			myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 500);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			GroupInfo groupInfo = (GroupInfo) object;
			FRouter.build(GroupSearchActivity.this, "/im/chat")
					.withString("Extra_chatID", groupInfo.conversationId)
					.withInt("Extra_chatType", 0X104)
					.go();
		});
	}

}
