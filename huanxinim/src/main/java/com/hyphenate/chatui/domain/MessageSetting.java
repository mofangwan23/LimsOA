package com.hyphenate.chatui.domain;

/**
 * @author ZYP
 * @since 2017-08-13 16:15
 */
public class MessageSetting {

	public String userId;               // 对应的用户 Id
	public boolean receiveMsg;          // 接收消息
	public boolean notify;              // 显示通知
	public boolean sound;               // 声音
	public boolean vibrate;             // 震动
	public boolean silence;             // 勿扰模式
	public String silenceST;            // 勿扰开始时间段
	public String silenceET;            // 勿扰结束时间段
	public boolean deleteMsg;           //退出群聊时是否删除会话消息
	public boolean speakerOn;           //语音消息是否外放播放

	/**
	 * 创建默认的设置：
	 * 接收新消息：开
	 * 显示通知：开
	 * 默认的勿扰时间：晚上 23：00 - 早上 08：00
	 * 默认语音消息外放
	 */
	public static MessageSetting defaultSetting() {
		MessageSetting setting = new MessageSetting();
		setting.receiveMsg = true;
		setting.notify = true;
		setting.sound = false;
		setting.vibrate = false;
		setting.silence = false;
		setting.silenceST = "23:00";
		setting.silenceET = "08:00";
		setting.speakerOn = true;
		setting.deleteMsg = false;
		return setting;
	}
}
