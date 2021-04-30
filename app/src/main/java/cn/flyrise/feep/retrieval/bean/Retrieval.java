package cn.flyrise.feep.retrieval.bean;

/**
 * @author ZYP
 * @since 2018-04-28 14:54
 * 检索的数据实体
 */
public class Retrieval {

	public static final int VIEW_TYPE_HEADER = 1;        // Header
	public static final int VIEW_TYPE_FOOTER = 2;        // Footer
	public static final int VIEW_TYPE_CONTENT = 3;       // Content

	public int viewType;        // 视图类型：Header、Footer、Content
	public int retrievalType;   // 数据类型：联系人、新闻、文件、聊天记录

	public String content;      // 内容
	public String extra;        // 存放点简单点的数据

	// 联系人：userId、deptId、imageHref
	// 新闻：messageId、businessId，ListRequestType(5)
	// 文件：url、taskId、filename
	// 聊天：isGroup, messageId, conversationId



}
