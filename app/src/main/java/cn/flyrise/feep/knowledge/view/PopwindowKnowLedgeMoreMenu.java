package cn.flyrise.feep.knowledge.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.knowledge.view.BasePopwindow.PopwindowMenuClickLister;

@SuppressLint("ValidFragment")
public class PopwindowKnowLedgeMoreMenu extends DialogFragment {

    private TextView mTvMove;
    private TextView mTvRename;
    private TextView mTvCancel;

    private boolean canRename;
    private boolean canMove;
    private PopwindowMenuClickLister mListener;

    public PopwindowKnowLedgeMoreMenu setCanRename(boolean canRename) {
        this.canRename = canRename;
        return this;
    }

    public PopwindowKnowLedgeMoreMenu setCanMove(boolean canMove) {
        this.canMove = canMove;
        return this;
    }

    public PopwindowKnowLedgeMoreMenu setListener(PopwindowMenuClickLister mListener) {
        this.mListener = mListener;
        return this;
    }

	@NonNull
	@Override public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
    	Dialog dialog=new Dialog(getActivity(),R.style.BottomDialog2);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();
		Window window = getDialog().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.gravity = Gravity.BOTTOM; // 紧贴底部
		lp.width = WindowManager.LayoutParams.MATCH_PARENT; // 宽度持平
		window.setAttributes(lp);
	}

	@Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
       View view = inflater.inflate(R.layout.layout_knowledge_moremenu, container,false);
        initView(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTvRename.setVisibility(canRename?View.VISIBLE:View.GONE);
        mTvMove.setVisibility(canMove?View.VISIBLE:View.GONE);
    }

    private void initView(View view) {
        mTvMove = view.findViewById(R.id.layout_knowledge_moremenu_tv_move);
        mTvRename = view.findViewById(R.id.layout_knowledge_moremenu_tv_rename);
        mTvCancel = view.findViewById(R.id.layout_knowledge_moremenu_tv_cancel);
	    initListener();
    }

    private void initListener() {

        mTvMove.setOnClickListener(v -> {
	        mListener.setPopWindowClicklister(mTvMove);
        });

        mTvRename.setOnClickListener(v -> {
	        mListener.setPopWindowClicklister(mTvRename);
        });

        mTvCancel.setOnClickListener(v -> {
            dismiss();
        });
    }

}
