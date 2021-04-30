package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by klc
 */

public class PublishAndReceiveListRequest extends KnowledgeBaseRequest {

    private static String GETRECEIVERLIST = "getpublishList";
    private static String GETPUBLISHEDLIST = "getpublishedList";
    private static String GETDOWNLIST = "getMydownloadList";

    private String param1;
    private String param2;

    public PublishAndReceiveListRequest(int listType, int currentPage, int pageSize) {
        if (listType == KnowKeyValue.PUBLISTTYPE)
            this.method = GETPUBLISHEDLIST;
        else {
            this.method = GETRECEIVERLIST;
        }
        this.count = "2";
        this.obj = "knowledgeService";
        this.param1 = String.valueOf(currentPage);
        this.param2 = String.valueOf(pageSize);
    }
}
