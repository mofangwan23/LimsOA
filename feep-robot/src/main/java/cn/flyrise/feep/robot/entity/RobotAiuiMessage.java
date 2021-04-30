package cn.flyrise.feep.robot.entity;

import com.iflytek.aiui.AIUIMessage;

/**
 * 新建：陈冕;
 * 日期： 2017-11-24-17:49.
 * 语音上传参数封装
 */

public class RobotAiuiMessage {

    private AIUIMessage aiuiMessage;

    public RobotAiuiMessage(final Builder builder) {
        aiuiMessage = new AIUIMessage(builder.messageType
                , builder.arg1
                , builder.arg2
                , builder.params
                , builder.data);
    }

    public AIUIMessage create() {
        return aiuiMessage;
    }

   public static class Builder {

        private int messageType;

        private int arg1;

        private int arg2;

        private String params;

        private byte[] data;

        public Builder setMessageType(int type) {
            this.messageType = type;
            return this;
        }

        public Builder setArg1(int arg1) {
            this.arg1 = arg1;
            return this;
        }

        public Builder setArg2(int arg2) {
            this.arg2 = arg2;
            return this;
        }

        public Builder setParams(String params) {
            this.params = params;
            return this;
        }

        public Builder setData(byte[] data) {
            this.data = data;
            return this;
        }

        public RobotAiuiMessage build() {
            return new RobotAiuiMessage(this);
        }
    }
}
