package com.hyphenate.chatui.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.adapter.PickAtUserAdapter;
import com.hyphenate.exceptions.HyphenateException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FELetterListView;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PickAtUserActivity extends BaseActivity {

    private ListView mListView;
    private PickAtUserAdapter mAdapter;
    private FELetterListView mLetterView;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_at_user);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle("选择回复的人");
    }

    @Override public void bindView() {
        mListView = (ListView) findViewById(R.id.listView);
        mLetterView = (FELetterListView) findViewById(R.id.feLetter);

        mListView.setAdapter(mAdapter = new PickAtUserAdapter());
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            AddressBook addressBook = (AddressBook) mAdapter.getItem(position);
            setResult(RESULT_OK, new Intent().putExtra("username", addressBook.name));
            finish();
        });

        mLetterView.setOnTouchingLetterChangedListener(letter -> {
            int selection = letter.toLowerCase().charAt(0);
            int position = mAdapter.getPositionBySelection(selection);
            if (position != -1) {
                mListView.setSelection(position);
            }
        });
    }

    @Override public void bindData() {
        final FELoadingDialog loadingDialog = new FELoadingDialog.Builder(this)
                .setLoadingLabel(CommonUtil.getString(R.string.core_loading_wait))
                .setCancelable(true)
                .create();
        loadingDialog.show();
        Observable.just(getIntent().getStringExtra("groupId"))
                .map(groupId -> {
                    if (TextUtils.isEmpty(groupId)) return null;

                    EMGroup emGroup = EMClient.getInstance().groupManager().getGroup(groupId);
                    if (emGroup == null) return null;

                    List<String> userIds = new ArrayList<>();
                    userIds.add(emGroup.getOwner());

                    List<String> members = emGroup.getMembers();
                    if (CommonUtil.nonEmptyList(members)) {
                        userIds.addAll(members);
                        String currentUserId = EMClient.getInstance().getCurrentUser();
                        userIds.remove(currentUserId);
                        return userIds;
                    }

                    try {
                        emGroup = EMClient.getInstance().groupManager().getGroupFromServer(groupId, true);
                        if (emGroup != null && CommonUtil.nonEmptyList(emGroup.getMembers())) {
                            userIds.addAll(emGroup.getMembers());
                        }
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }

                    String currentUserId = EMClient.getInstance().getCurrentUser();
                    userIds.remove(currentUserId);  // 不显示自己
                    return userIds;
                })
                .map(userIds -> {
                    if (CommonUtil.isEmptyList(userIds)) return null;
                    List<AddressBook> addressBooks = CoreZygote.getAddressBookServices().queryUserIds(userIds);
                    if (CommonUtil.nonEmptyList(addressBooks) && addressBooks.size() > 10) {
                        Collections.sort(addressBooks, (lhs, rhs) -> lhs.pinyin.toLowerCase().compareTo(rhs.pinyin.toLowerCase()));
                    }
                    return addressBooks;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(addressBooks -> {
                    loadingDialog.hide();
                    onAddressBookLoadSuccess(addressBooks);
                }, exception -> {
                    loadingDialog.hide();
                    exception.printStackTrace();
                });
    }

    private void onAddressBookLoadSuccess(List<AddressBook> addressBooks) {
        if (addressBooks == null) return;
        mLetterView.setVisibility(addressBooks.size() > 10 ? View.VISIBLE : View.GONE);
        mAdapter.setAddressBook(addressBooks);
        mLetterView.setShowLetters(mAdapter.getLetterList());
    }
}
