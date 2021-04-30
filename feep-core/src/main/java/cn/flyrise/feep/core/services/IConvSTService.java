package cn.flyrise.feep.core.services;

/**
 * @author ZYP
 * @since 2017-08-11 09:30 环信会话设置服务
 */
public interface IConvSTService {

	/**
	 * 会话是否设置了消息免打扰
	 * @param convId 会话 ID
	 */
	boolean isSilence(String convId);

	/**
	 * 设置会话免打扰
	 * @param convId 会话 ID
	 * @param conversation 会话名称
	 */
	void makeConversationSilence(String convId, String conversation);

	/**
	 * 设置会话正常通知
	 * @param convId 会话 ID
	 * @param conversation 会话名称
	 */
	void makeConversationActive(String convId, String conversation);

	/**
	 * 保存群聊，房子退群后显示异常
	 * */
	void makeConversationGroud(String convId, String conversation);

	/**
	 * 获取会话名称
	 * @param convId 会话 ID
	 */
	String getCoversationName(String convId);

	/**
	 * 判断会话是否存在
	 * @param convId 会话 ID
	 */
	boolean coversationExist(String convId);
}
