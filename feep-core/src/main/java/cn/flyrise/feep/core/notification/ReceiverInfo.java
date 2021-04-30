package cn.flyrise.feep.core.notification;

/**
 * 接收到的推送的消息
 * Created by luoming on 2018/5/28.
 */

public class ReceiverInfo {

	public int pushTarget; //推送平台
	public String title; //标题
	public String content; //内容
	public String extra; //额外数据
	public Object rawData; //原始数据
	public int infoType = 1;//消息类型：1：通知栏消息，2：自定义消息，3：点击事件

	public ReceiverInfo(Builder builder) {
		this.pushTarget = builder.pushTarget;
		this.title = builder.title;
		this.content = builder.content;
		this.extra = builder.extra;
		this.rawData = builder.rawData;
		this.infoType = builder.infoType;
	}

	public static class Builder {

		private int pushTarget = 100;//极光
		private String title;
		private String content;
		private String extra;
		private Object rawData;
		private int infoType;

		public Builder setPushTarget(int pushTarget) {
			this.pushTarget = pushTarget;
			return this;
		}

		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		public Builder setContent(String content) {
			this.content = content;
			return this;
		}

		public Builder setExtra(String extra) {
			this.extra = extra;
			return this;
		}

		public Builder setRawData(Object rawData) {
			this.rawData = rawData;
			return this;
		}

		public Builder setInfoType(int infoType) {
			this.infoType = infoType;
			return this;
		}

		public ReceiverInfo create() {
			return new ReceiverInfo(this);
		}
	}
}
