package cn.flyrise.feep.x5;

import java.util.List;

/**
 * @author 社会主义接班人
 * @since 2018-09-17 16:25
 */
public class Request {

	public final int pageId;
	public final int moduleId;
	public final String extra;
	public final String messageId;
	public final String businessId;
	public final String appointURL;
	public final List<String> userIds;

	private Request(Builder builder) {
		this.pageId = builder.pageId;
		this.moduleId = builder.moduleId;
		this.extra = builder.extra;
		this.userIds = builder.userIds;
		this.messageId = builder.messageId;
		this.businessId = builder.businessId;
		this.appointURL = builder.appointURL;
	}

	public static class Builder {

		private int moduleId;
		private int pageId = -1;

		private String extra;
		private String messageId;
		private String businessId;
		private String appointURL;
		private List<String> userIds;

		public Builder businessId(String businessId) {
			this.businessId = businessId;
			return this;
		}

		public Builder messageId(String messageId) {
			this.messageId = messageId;
			return this;
		}

		public Builder moduleId(int moduleId) {
			this.moduleId = moduleId;
			return this;
		}

		public Builder pageId(int pageId) {
			this.pageId = pageId;
			return this;
		}

		public Builder extra(String extra) {
			this.extra = extra;
			return this;
		}

		public Builder userIds(List<String> userIds) {
			this.userIds = userIds;
			return this;
		}

		public Builder appointURL(String appointURL) {
			this.appointURL = appointURL;
			return this;
		}

		public Request create() {
			return new Request(this);
		}
	}

}
