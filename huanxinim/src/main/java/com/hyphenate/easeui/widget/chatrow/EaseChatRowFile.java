package com.hyphenate.easeui.widget.chatrow;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.listener.ChatMessageImageLoadingListener;
import com.hyphenate.easeui.ui.EaseShowNormalFileActivity;
import com.hyphenate.easeui.utils.EaseFileUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.TextFormater;
import java.io.File;

public class EaseChatRowFile extends EaseChatRow {

    protected TextView fileNameView;
    protected TextView fileSizeView;
    protected TextView fileStateView;

    private EMNormalFileMessageBody fileMessageBody;
    protected ChatMessageImageLoadingListener mListener;


    public static EaseChatRowFile create(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_FILE ? R.layout.ease_row_sent_file :
                R.layout.ease_row_received_file, parent, false);
        return new EaseChatRowFile(view);
    }

    public EaseChatRowFile(View view) {
        super(view);
    }

    @Override
    protected void findView() {
        fileNameView = (TextView) itemView.findViewById(R.id.tv_file_name);
        fileSizeView = (TextView) itemView.findViewById(R.id.tv_file_size);
        fileStateView = (TextView) itemView.findViewById(R.id.tv_file_state);
        tvPercent = (TextView) itemView.findViewById(R.id.percentage);
    }

    @Override
    protected void setUpView() {
        fileMessageBody = (EMNormalFileMessageBody) message.getBody();
        String filePath = fileMessageBody.getLocalUrl();
        fileNameView.setText(fileMessageBody.getFileName());
        fileSizeView.setText(TextFormater.getDataSize(fileMessageBody.getFileSize()));
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            File file = new File(filePath);
            if (file.exists()) {
                fileStateView.setText(R.string.Have_downloaded);
            }
            else {
                fileStateView.setText(R.string.Did_not_download);
            }
            return;
        }
        handleSendMessage();
    }

    /**
     * handle sending message
     */
    protected void handleSendMessage() {
        setMsgCallBack();
        switch (message.status()) {
            case SUCCESS:
                progressBar.setVisibility(View.GONE);
                if (tvPercent != null) {
                    tvPercent.setVisibility(View.GONE);
                }
                statusView.setVisibility(View.GONE);
                break;
            case FAIL:
                progressBar.setVisibility(View.GONE);
                if (tvPercent != null) {
                    tvPercent.setVisibility(View.GONE);
                }
                statusView.setVisibility(View.VISIBLE);
                break;
            case INPROGRESS:
                progressBar.setVisibility(View.VISIBLE);
                if (tvPercent != null) {
                    tvPercent.setVisibility(View.VISIBLE);
                    tvPercent.setText(message.progress() + "%");
                }
                statusView.setVisibility(View.GONE);
                break;
            default:
                progressBar.setVisibility(View.GONE);
                if (tvPercent != null) {
                    tvPercent.setVisibility(View.GONE);
                }
                statusView.setVisibility(View.VISIBLE);
                break;
        }
    }




    @Override
    protected void onBubbleClick() {
        String filePath = fileMessageBody.getLocalUrl();
        File file = new File(filePath);
        if (file.exists()) {
            EaseFileUtils.openFile(file, activity);
        }
        else {
            activity.startActivity(new Intent(activity, EaseShowNormalFileActivity.class).putExtra("msg", message));
        }
        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    public void setChatMessageImageLoadingListener(ChatMessageImageLoadingListener listener) {
        this.mListener = listener;
    }
}
