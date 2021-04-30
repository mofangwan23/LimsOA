package com.hyphenate.easeui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.ViewGroup;

import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMNormalFileMessageBody;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseChatMessageList.MessageListItemClickListener;
import com.hyphenate.easeui.widget.chatrow.ChatRowVoiceCall;
import com.hyphenate.easeui.widget.chatrow.EaseChatRow;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowBigExpression;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowExtend;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowFile;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowGif;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowImage;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowLocation;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowReply;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowSystem;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowText;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVideo;
import com.hyphenate.easeui.widget.chatrow.EaseChatRowVoice;

import java.util.List;

import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.BitmapUtil;

public class EaseMessageAdapter extends BaseRecyclerAdapter {

    private static final int MESSAGE_TYPE_SENT_TXT = 3;
    public static final int MESSAGE_TYPE_REC_TXT = 4;
    public static final int MESSAGE_TYPE_SENT_IMAGE = 5;
    private static final int MESSAGE_TYPE_REC_IMAGE = 6;
    public static final int MESSAGE_TYPE_SENT_LOCATION = 7;
    private static final int MESSAGE_TYPE_REC_LOCATION = 8;
    public static final int MESSAGE_TYPE_SENT_VOICE = 9;
    private static final int MESSAGE_TYPE_REC_VOICE = 10;
    public static final int MESSAGE_TYPE_SENT_VIDEO = 11;
    private static final int MESSAGE_TYPE_REC_VIDEO = 12;
    public static final int MESSAGE_TYPE_SENT_FILE = 13;
    private static final int MESSAGE_TYPE_REC_FILE = 14;
    public static final int MESSAGE_TYPE_SENT_EXPRESSION = 15;
    private static final int MESSAGE_TYPE_REC_EXPRESSION = 16;
    public static final int MESSAGE_TYPE_SENT_VOICE_CALL = 17;
    public static final int MESSAGE_TYPE_REC_VOICE_CALL = 18;
    public static final int MESSAGE_TYPE_SENT_VIDEO_CALL = 19;
    public static final int MESSAGE_TYPE_REC_VIDEO_CALL = 20;
    public static final int MESSAGE_TYPE_SENT_GIF = 21;
    private static final int MESSAGE_TYPE_REC_GIF = 22;
    private static final int MESSAGE_TYPE_EXTENDS_TXT = 23;
    private static final int MESSAGE_TYPE_SYSTEM = 24;
    public static final int MESSAGE_TYPE_SENT_REPLY = 25;
    private static final int MESSAGE_TYPE_REC_REPLY = 26;


    private List<EMMessage> messages = null;
    private MessageListItemClickListener itemClickListener;
    private ImageLoadListener imageLoadListener;
    private Context context;

    public EaseMessageAdapter(Context context) {
        this.context = context;
    }

    public void refresh(List<EMMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }


    @Override
    public int getDataSourceCount() {
        return messages == null ? 0 : messages.size();
    }

    @Override
    public void onChildBindViewHolder(ViewHolder holder, int position) {
        EaseChatRow chatRow = (EaseChatRow) holder;
        chatRow.setView(context, messages.get(position), position == 0 ? null : messages.get(position - 1), itemClickListener);
        if (holder instanceof EaseChatRowFile) {
            if (imageLoadListener != null) {
                ((EaseChatRowFile) chatRow).setChatMessageImageLoadingListener(() -> imageLoadListener.loadComplete());
            }
        }
    }

    @Override
    public ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        EaseChatRow chatRow;
        switch (viewType) {
            case MESSAGE_TYPE_SENT_TXT:
            case MESSAGE_TYPE_REC_TXT:
                chatRow = EaseChatRowText.create(parent, viewType);
                break;
            case MESSAGE_TYPE_SENT_IMAGE:
            case MESSAGE_TYPE_REC_IMAGE:
                chatRow = EaseChatRowImage.create(parent, viewType);
                break;
            case MESSAGE_TYPE_REC_LOCATION:
            case MESSAGE_TYPE_SENT_LOCATION:
                chatRow = EaseChatRowLocation.create(parent, viewType);
                break;
            case MESSAGE_TYPE_REC_VOICE:
            case MESSAGE_TYPE_SENT_VOICE:
                chatRow = EaseChatRowVoice.create(parent, viewType);
                break;
            case MESSAGE_TYPE_SENT_VIDEO:
            case MESSAGE_TYPE_REC_VIDEO:
                chatRow = EaseChatRowVideo.create(parent, viewType);
                break;
            case MESSAGE_TYPE_SENT_FILE:
            case MESSAGE_TYPE_REC_FILE:
                chatRow = EaseChatRowFile.create(parent, viewType);
                break;
            case MESSAGE_TYPE_SENT_EXPRESSION:
            case MESSAGE_TYPE_REC_EXPRESSION:
                chatRow = EaseChatRowBigExpression.create(parent, viewType);
                break;
            case MESSAGE_TYPE_REC_VIDEO_CALL:
            case MESSAGE_TYPE_SENT_VIDEO_CALL:
            case MESSAGE_TYPE_SENT_VOICE_CALL:
            case MESSAGE_TYPE_REC_VOICE_CALL:
                chatRow = ChatRowVoiceCall.create(parent, viewType);
                break;
            case MESSAGE_TYPE_SENT_GIF:
            case MESSAGE_TYPE_REC_GIF:
                chatRow = EaseChatRowGif.create(parent, viewType);
                break;
            case MESSAGE_TYPE_EXTENDS_TXT:
                chatRow = EaseChatRowExtend.create(parent);
                break;
            case MESSAGE_TYPE_SYSTEM:
                chatRow = EaseChatRowSystem.create(parent);
                break;
            case MESSAGE_TYPE_SENT_REPLY:
            case MESSAGE_TYPE_REC_REPLY:
                chatRow = EaseChatRowReply.create(parent, viewType);
                break;
            default:
                chatRow = EaseChatRowText.create(parent, viewType);
                break;
        }
        return chatRow;
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 0 && hasHeaderView()) {
            return TYPE_HEADER;
        } else {
            if (hasHeaderView()) {
                position = position - 1;
            }
            EMMessage message = messages.get(position);
            if (message.getType() == EMMessage.Type.TXT) {
                if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_BIG_EXPRESSION, false)) {
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_EXPRESSION : MESSAGE_TYPE_SENT_EXPRESSION;
                }
                if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, false)) {
                    return MESSAGE_TYPE_SYSTEM;
                }
                if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL, false)) {
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_VOICE_CALL : MESSAGE_TYPE_SENT_VOICE_CALL;
                } else if (message.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, false)) {
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_VIDEO_CALL : MESSAGE_TYPE_SENT_VIDEO_CALL;
                } else if (message.getBooleanAttribute(EmChatContent.MESSAGE_ATTR_IS_REPLY, false)) { // 回复他人内容
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_REPLY : MESSAGE_TYPE_SENT_REPLY;
                } else if (EaseCommonUtils.isExtendMessage(message)) {
                    return MESSAGE_TYPE_EXTENDS_TXT;
                }
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_TXT : MESSAGE_TYPE_SENT_TXT;
            }
            if (message.getType() == EMMessage.Type.IMAGE) {
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_IMAGE : MESSAGE_TYPE_SENT_IMAGE;
            }
            if (message.getType() == EMMessage.Type.LOCATION) {
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_LOCATION : MESSAGE_TYPE_SENT_LOCATION;
            }
            if (message.getType() == EMMessage.Type.VOICE) {
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_VOICE : MESSAGE_TYPE_SENT_VOICE;
            }
            if (message.getType() == EMMessage.Type.VIDEO) {
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_VIDEO : MESSAGE_TYPE_SENT_VIDEO;
            }
            if (message.getType() == EMMessage.Type.FILE) {
                EMNormalFileMessageBody body = (EMNormalFileMessageBody) message.getBody();
                if (body != null && BitmapUtil.isPictureGif(body.getFileName())) {
                    return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_GIF : MESSAGE_TYPE_SENT_GIF;
                }
                return message.direct() == EMMessage.Direct.RECEIVE ? MESSAGE_TYPE_REC_FILE : MESSAGE_TYPE_SENT_FILE;
            }
            return -1;// invalid
        }
    }

    @Override public long getItemId(int position) {
        return position;
    }

    public void setItemClickListener(MessageListItemClickListener listener) {
        itemClickListener = listener;
    }

    public void setImageLoadListener(ImageLoadListener imageLoadListener) {
        this.imageLoadListener = imageLoadListener;
    }

    public interface ImageLoadListener {

        void loadComplete();
    }
}
