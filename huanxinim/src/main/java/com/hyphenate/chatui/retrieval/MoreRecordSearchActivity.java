package com.hyphenate.chatui.retrieval;

import android.text.TextUtils;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.RouteCreator;
import cn.squirtlez.frouter.annotations.Route;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.busevent.ChatContent;
import java.util.concurrent.TimeUnit;
import org.greenrobot.eventbus.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-04 19:51
 */
@Route("/im/chat/search/more")
public class MoreRecordSearchActivity extends FESearchListActivity<ChatMessage> {

	private String mKeyword;
	private MoreRecordSearchAdapter mAdapter;
	private MoreRecordSearchPresenter mPresenter;

	@Override
	public void bindView() {
		super.bindView();
		this.listView.setCanRefresh(false);
	}

	@Override
	public void bindData() {
		et_Search.setHint("搜索聊天记录");
		mKeyword = getIntent().getStringExtra("keyword");

		mAdapter = new MoreRecordSearchAdapter(this);
		mPresenter = new MoreRecordSearchPresenter(this);
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
			mKeyword = et_Search.getText().toString();
			ChatMessage chatMessage = (ChatMessage) object;

			if (TextUtils.isEmpty(chatMessage.messageId)) {
				FRouter.build(MoreRecordSearchActivity.this, "/im/chat/search")   // 这里他妈的必须需要一个 keyword 啊
						.withString("conversationId", chatMessage.conversationId)
						.withString("keyword", mKeyword)
						.go();
				return;
			}

			RouteCreator routeCreator = FRouter
					.build(MoreRecordSearchActivity.this, "/im/chat")
					.withString("Extra_chatID", chatMessage.conversationId);

			if (chatMessage.isGroup) {
				routeCreator.withInt("Extra_chatType", 0X104);
			}

			routeCreator.go();

			Observable.timer(500, TimeUnit.MILLISECONDS)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(time -> {
						EventBus.getDefault().post(new ChatContent.SeekToMsgEvent(chatMessage.messageId));
					});
		});
	}
}
