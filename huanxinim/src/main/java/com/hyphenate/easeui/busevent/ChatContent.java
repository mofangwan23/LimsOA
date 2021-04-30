package com.hyphenate.easeui.busevent;

/**
 * Created by klc on 2017/3/20.
 */

public class ChatContent {

    /**
     * 消息搜索后跳转到某一条具体消息的事件
     */
    public static class SeekToMsgEvent {
        public  String msgID;
        public SeekToMsgEvent(String msgID) {
            this.msgID = msgID;
        }
    }

}
