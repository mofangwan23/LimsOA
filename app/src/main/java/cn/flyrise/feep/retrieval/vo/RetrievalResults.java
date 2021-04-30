package cn.flyrise.feep.retrieval.vo;

import android.support.annotation.Keep;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-05 10:39
 * 封装检索结果
 */
@Keep
public final class RetrievalResults {

	/**
	 * 检索结果数量：0 表示没数据
	 */
	public final int resultCode;

	/**
	 * 检索类型：联系人、新闻、文件、聊天记录等...
	 */
	public final int retrievalType;

	/**
	 * 检索结果：联系人、新闻、文件等...
	 */
	public final List<? extends Retrieval> retrievals;

	// 不允许创建对象，并且，属性也他妈的全部不允许赋值
	private RetrievalResults(Builder builder) {
		this.resultCode = builder.resultCode;
		this.retrievalType = builder.retrievalType;
		this.retrievals = builder.retrievals;
	}

	public static class Builder {

		private int resultCode;
		private int retrievalType;
		private List<? extends Retrieval> retrievals;

		public Builder retrievalType(int retrievalType) {
			this.retrievalType = retrievalType;
			return this;
		}

		public Builder retrievals(List<? extends Retrieval> retrievals) {
			this.retrievals = retrievals;
			return this;
		}

		public RetrievalResults create() {
			this.resultCode = CommonUtil.isEmptyList(retrievals) ? 0 : retrievals.size();
			return new RetrievalResults(this);
		}
	}
}
