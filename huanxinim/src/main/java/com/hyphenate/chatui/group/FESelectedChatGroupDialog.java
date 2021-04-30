package com.hyphenate.chatui.group;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.group.adapter.FESelectedChatGroupAdapter;

import java.util.List;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 新建：陈冕;
 * 日期： 2018-3-9-13:41.
 * 群聊解散后，选择记录写入的群聊
 */

public class FESelectedChatGroupDialog extends DialogFragment implements FESelectedChatGroupAdapter.OnSelectedItemListener {

    private List<EMGroup> mGroupList;
    private FESelectedChatGroupAdapter.OnSelectedItemListener mListener;

    public FESelectedChatGroupDialog setChatGroupList(List<EMGroup> groupList) {
        this.mGroupList = groupList;
        return this;
    }

    public FESelectedChatGroupDialog setListener(FESelectedChatGroupAdapter.OnSelectedItemListener itemListener) {
        this.mListener = itemListener;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.em_selected_chat_group_dialog, container, false);
        initView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.85), (int) (dm.heightPixels * 0.55));
        }
    }

    private void initView(View view) {
        if (CommonUtil.isEmptyList(mGroupList)) {
            dismiss();
            return;
        }
        RecyclerView recyclerView = view.findViewById(R.id.group_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new FESelectedChatGroupAdapter(mGroupList, this));
    }

    @Override
    public void selectedItem(String groudId) {
        dismiss();
        if (mListener == null) {
            return;
        }
        mListener.selectedItem(groudId);
    }
}
