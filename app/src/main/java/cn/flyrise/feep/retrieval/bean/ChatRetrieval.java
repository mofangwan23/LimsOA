package cn.flyrise.feep.retrieval.bean;

/**
 * @author ZYP
 * @since 2018-05-04 15:19
 */
public class ChatRetrieval extends Retrieval {

	public boolean isGroup;
	public String messageId;        // 如果只有一条记录的话，这个就是 messageId
	public String conversationId;   // 会话Id
	public int imageRes;
	public String keyword;          // 关键字

}
