package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by yj on 2016/7/19.
 */
public class PromptRequest extends RequestContent {
    public static final String NAMESPACE = "PromptRequest";

    public static final String METHOD_PROMPT = "提醒周期";
    public static final String METHOD_REPEAT = "事件周期";

    public String prompt;
    public String key;

    public PromptRequest() { }

    public PromptRequest(String prompt, String key) {
        this.prompt = prompt;
        this.key = key;
    }

    public String getNameSpace() {
        return NAMESPACE;
    }
}
