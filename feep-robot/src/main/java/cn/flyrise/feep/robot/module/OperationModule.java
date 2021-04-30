package cn.flyrise.feep.robot.module;

import cn.flyrise.feep.robot.manager.FeepOperationManager.OnMessageGrammarResultListener;

/**
 * Created by Administrator on 2017-6-29.
 * 具体操作跳转的模块
 */

public class OperationModule {

    public final String operationType;
    public final String messageType;
    public final String username;
    public final String dateTime;
    public final String wildcard;//通配符回传的数据
    public final OnMessageGrammarResultListener grammarResultListener;

    private OperationModule(Builder builder) {
        this.operationType = builder.operationType;
        this.messageType = builder.messageType;
        this.username = builder.username;
        this.dateTime = builder.dateTime;
        this.wildcard = builder.wildcard;
        this.grammarResultListener = builder.grammarResultListener;
    }

    public int getMessageId() {
        int messageId = -1;
        try {
            messageId = Integer.valueOf(messageType);
        } catch (Exception exp) {
            messageId = -1;
        }
        return messageId;
    }


    public static class Builder {

        private String operationType;
        private String messageType;
        private String username;
        private String dateTime;
        private String wildcard;
        private OnMessageGrammarResultListener grammarResultListener;

        public Builder operationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public Builder messageType(String messageType) {
            this.messageType = messageType;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder dateTime(String dateTime) {
            this.dateTime = dateTime;
            return this;
        }

        public Builder wildcard(String wildcard) {
            this.wildcard = wildcard;
            return this;
        }

        public Builder grammarResultListener(OnMessageGrammarResultListener grammarResultListener) {
            this.grammarResultListener = grammarResultListener;
            return this;
        }

        public OperationModule build() {
            return new OperationModule(this);
        }
    }


}
