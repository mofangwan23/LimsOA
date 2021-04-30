package com.hyphenate.easeui.widget.chatrow;

import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMMessage.Direct;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.model.Message;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import com.hyphenate.exceptions.HyphenateException;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * author : klc
 * 2017-11-02 18:00
 */
public class EaseChatRowReply extends EaseChatRow {

    private TextView tvUserTime;
    private TextView tvOriginalContent;
    private TextView contentView;

    private String mModuleId;
    private Message feMessage;


    public static EaseChatRowReply create(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_REPLY ? R.layout.ease_row_sent_reply :
                R.layout.ease_row_received_reply, parent, false);
        return new EaseChatRowReply(view);
    }

    public EaseChatRowReply(View view) {
        super(view);
    }

    @Override
    protected void findView() {
        contentView = (TextView) itemView.findViewById(R.id.tv_chatcontent);
        tvUserTime = (TextView) itemView.findViewById(R.id.tvUserTime);
        tvOriginalContent = (TextView) itemView.findViewById(R.id.tvOriginalContent);
    }

    @Override
    protected void setUpView() {
        EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
        Spannable span = EaseSmileUtils.getSmiledText(activity, txtBody.getMessage());
        contentView.setText(span, BufferType.SPANNABLE);

        feMessage = new Message();
        mModuleId = message.getStringAttribute(EmChatContent.MESSAGE_SUPER_MOUDULE, "");
        feMessage.setBusinessID(message.getStringAttribute("id", ""));
        feMessage.setMessageID(message.getStringAttribute("msgId", ""));
        feMessage.setUrl(message.getStringAttribute("url", ""));
//        AddressBook addressBook;
        CoreZygote.getAddressBookServices().queryUserDetail(message.direct() == Direct.RECEIVE?message.getFrom():message.getTo())
                .subscribe(addressBook -> {
                    long time = message.getLongAttribute(EmChatContent.MESSAGE_ATTR_TIME, System.currentTimeMillis());
                    String sendTime = (DateUtil.formatTimeForDetail(time));
                    tvUserTime.setText(addressBook == null ? "" : addressBook.name + " " + sendTime);
                }, error -> {

                });
//        if (message.direct() == Direct.RECEIVE) {
//            addressBook = CoreZygote.getAddressBookServices().queryUserInfo(message.getFrom());
//        } else {
//            addressBook = CoreZygote.getAddressBookServices().queryUserInfo(message.getTo());
//        }

        String action = message.getStringAttribute("action", "");
        String title = message.getStringAttribute("title", "");
        String content = message.getStringAttribute("content", "");
        String originalContent = action.equals(title) ? content : title;
        tvOriginalContent.setText(String.format("《%s》", originalContent));

        bubbleLayout.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onExtendMessageClick(mModuleId, feMessage);
            }
        });
        handleTextMessage();
    }

    protected void handleTextMessage() {
        if (message.direct() == Direct.SEND) {
            setMsgCallBack();
            switch (message.status()) {
                case CREATE:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case SUCCESS:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.GONE);
                    break;
                case FAIL:
                    progressBar.setVisibility(View.GONE);
                    statusView.setVisibility(View.VISIBLE);
                    break;
                case INPROGRESS:
                    progressBar.setVisibility(View.VISIBLE);
                    statusView.setVisibility(View.GONE);
                    break;
                default:
                    break;
            }
        } else {
            if (!message.isAcked() && message.getChatType() == ChatType.Chat) {
                try {
                    EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                } catch (HyphenateException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onBubbleClick() {

    }

}
