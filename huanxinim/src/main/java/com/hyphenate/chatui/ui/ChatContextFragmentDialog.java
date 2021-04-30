package com.hyphenate.chatui.ui;

import static com.hyphenate.easeui.EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION;
import static com.hyphenate.easeui.EaseUiK.EmChatContent.MESSAGE_ATTR_IS_REPLY;
import static com.hyphenate.easeui.EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL;
import static com.hyphenate.easeui.EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.controller.EaseUI;
import com.hyphenate.easeui.utils.EaseCommonUtils;

import cn.flyrise.feep.core.CoreZygote;

/**
 * Created by Administrator on 2016-12-9.
 */

public class ChatContextFragmentDialog extends DialogFragment {

    public static final int RESULT_CODE_COPY = 1;
    public static final int RESULT_CODE_DELETE = 2;
    public static final int RESULT_CODE_FORWARD = 3;
    public static final int RESULT_CODE_RECALL = 4;
    public static final int RESULT_CODE_REPLY = 5;
    public static final int RESULT_CODE_REMIND = 6;
    public static final int RESULT_CODE_SPEKER = 7;

    private EMMessage mMessage;

    private TextView mTvCopy;
    private TextView mTvDelete;
    private TextView mTvForward;
    private TextView mTvRecall;
    private TextView mTvReply;
    private TextView mTvRemind;
    private TextView mTvSpeker;

    private OperationListener mOperstionListener;

    private final int MAX_RECALL_TIME = 2 * 60 * 1000;


    public ChatContextFragmentDialog setEMMessage(EMMessage message) {
        this.mMessage = message;
        return this;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return initView(inflater, container);
    }

    private View initView(LayoutInflater inflater, ViewGroup viewGroup) {
        View view = inflater.inflate(R.layout.em_context_operation, null);
        mTvCopy = (TextView) view.findViewById(R.id.tvCopy);
        mTvDelete = (TextView) view.findViewById(R.id.tvDelete);
        mTvForward = (TextView) view.findViewById(R.id.tvForward);
        mTvRecall = (TextView) view.findViewById(R.id.tvRecall);
        mTvReply = (TextView) view.findViewById(R.id.tvReply);
        mTvRemind = (TextView) view.findViewById(R.id.tvRemind);
        mTvSpeker = (TextView) view.findViewById(R.id.tvSpeaker);
        initData();
        setListener();
        return view;
    }

    private void initData() {
        int type = mMessage.getType().ordinal();
        if (type == EMMessage.Type.TXT.ordinal()) {
            if (EaseCommonUtils.isExtendMessage(mMessage)) {
                mTvReply.setVisibility(View.VISIBLE);
                mTvRemind.setVisibility(View.VISIBLE);
            } else if (mMessage.getBooleanAttribute(MESSAGE_ATTR_IS_REPLY, false)) {
                mTvDelete.setVisibility(View.VISIBLE);
                mTvCopy.setVisibility(View.GONE);
                mTvForward.setVisibility(View.GONE);
            } else if (mMessage.getBooleanAttribute(MESSAGE_ATTR_IS_VIDEO_CALL, false)
                    || mMessage.getBooleanAttribute(MESSAGE_ATTR_IS_VOICE_CALL, false)
                    ) {
                mTvDelete.setVisibility(View.VISIBLE);
            } else if (mMessage.getBooleanAttribute(MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                mTvDelete.setVisibility(View.VISIBLE);
                mTvForward.setVisibility(View.VISIBLE);
            } else {
                mTvCopy.setVisibility(View.VISIBLE);
                mTvDelete.setVisibility(View.VISIBLE);
                mTvForward.setVisibility(View.VISIBLE);
                mTvRemind.setVisibility(View.VISIBLE);
            }
        } else if (type == EMMessage.Type.LOCATION.ordinal()) {
            mTvDelete.setVisibility(View.VISIBLE);
        } else if (type == EMMessage.Type.IMAGE.ordinal()) {
            mTvDelete.setVisibility(View.VISIBLE);
            mTvForward.setVisibility(View.VISIBLE);
        } else if (type == EMMessage.Type.VOICE.ordinal()) {
            mTvDelete.setVisibility(View.VISIBLE);
            mTvSpeker.setVisibility(View.VISIBLE);
            Context context = CoreZygote.getContext();
            boolean sperkerOn = EaseUI.getInstance().getSettingsProvider().isSpeakerOpened();
            mTvSpeker.setText(sperkerOn ? context.getString(R.string.turn_off_speaker) : context.getString(R.string.turn_on_speaker));
        } else if (type == EMMessage.Type.VIDEO.ordinal()) {
            mTvDelete.setVisibility(View.VISIBLE);
        } else if (type == EMMessage.Type.FILE.ordinal()) {
            mTvDelete.setVisibility(View.VISIBLE);
        }
        mTvRecall.setVisibility(View.VISIBLE);
    }

    private void setListener() {
        mTvCopy.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_COPY);
            }
            dismiss();
        });
        mTvDelete.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_DELETE);
            }
            dismiss();
        });
        mTvForward.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_FORWARD);
            }
            dismiss();
        });
        mTvRecall.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_RECALL);
            }
            dismiss();
        });
        mTvReply.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_REPLY);
            }
            dismiss();
        });
        mTvRemind.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_REMIND);
            }
            dismiss();
        });
        mTvSpeker.setOnClickListener(v -> {
            if (mOperstionListener != null) {
                mOperstionListener.operation(RESULT_CODE_SPEKER);
            }
            dismiss();
        });
    }


    interface OperationListener {

        void operation(int result);
    }

    public ChatContextFragmentDialog setOperstionListener(OperationListener operstionListener) {
        this.mOperstionListener = operstionListener;
        return this;
    }

}
