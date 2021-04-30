package com.hyphenate.easeui.widget.chatrow;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.squirtlez.frouter.FRouter;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.exceptions.HyphenateException;

;

public class EaseChatRowLocation extends EaseChatRow {

    private TextView locationView;
    private EMLocationMessageBody locBody;
    private String userName;

    public static EaseChatRowLocation create(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_LOCATION ? R.layout.ease_row_sent_location :
                R.layout.ease_row_received_location, parent, false);
        return new EaseChatRowLocation(view);
    }

    public EaseChatRowLocation(View view) {
        super(view);
    }



    @Override
    protected void findView() {
            locationView = (TextView) itemView.findViewById(R.id.tv_location);
    }

    @Override
    protected void setUpView() {
        locBody = (EMLocationMessageBody) message.getBody();
        String address=message.getStringAttribute("locationAddress", "")+locBody.getAddress();
        locationView.setText(address);
        // handle sending message
        if (message.direct() == EMMessage.Direct.SEND) {
            String userId = EMClient.getInstance().getCurrentUser();
            if (!TextUtils.isEmpty(userId)) {
                userName = EaseUserUtils.getUserNick(userId);
            }
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
            if (!TextUtils.isEmpty(message.getFrom())) {
                userName = EaseUserUtils.getUserNick(message.getFrom());
            }
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
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude", locBody.getLatitude());
        bundle.putDouble("longitude", locBody.getLongitude());
        bundle.putString("title", locBody.getAddress());
        bundle.putString("address", message.getStringAttribute("locationAddress", ""));
        bundle.putString("name", userName);
        bundle.putInt("isSendLocation", 601);
        FRouter.build(activity, "/location/detail")
                .withBundle(bundle)
                .go();
    }

}
