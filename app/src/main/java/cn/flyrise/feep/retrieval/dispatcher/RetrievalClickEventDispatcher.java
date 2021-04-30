package cn.flyrise.feep.retrieval.dispatcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.SparseArray;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * @author ZYP
 * @since 2018-05-03 11:30
 */
public class RetrievalClickEventDispatcher {

	private final SparseArray<RetrievalDispatcher> mDispatcherCaches;
	private final Context mContext;
//	private final List<RetrievalType> mRetrievalType;   // 部分不在这里面的界面就没必要创建对象了

	public RetrievalClickEventDispatcher(Context context, List<RetrievalType> retrievalTypes) {
		mContext = context;
//		mRetrievalType = retrievalTypes;
		mDispatcherCaches = new SparseArray<>(retrievalTypes.size());
	}

	/**
	 * 处理查看更多事件
	 * @param retrievalType 类型：联系人、新闻、文件、聊天记录等
	 * @param keyword 搜索的关键字
	 */
	public void dispatcherMoreEvent(int retrievalType, String keyword) {
		RetrievalDispatcher dispatcher = getDispatcher(retrievalType);
		if (dispatcher != null) {
			dispatcher.jumpToSearchPage(mContext, keyword);
		}
	}

	/**
	 * 处理内容点击事件
	 * @param retrieval 具体点击的内容
	 */
	public void dispatcherClickEvent(Retrieval retrieval) {
		RetrievalDispatcher dispatcher = getDispatcher(retrieval.retrievalType);
		if (dispatcher != null) {
			dispatcher.jumpToDetailPage(mContext, retrieval);
		}
	}

	private RetrievalDispatcher getDispatcher(int retrievalType) {
		RetrievalDispatcher dispatcher = mDispatcherCaches.get(retrievalType);
		if (dispatcher == null) {
			dispatcher = this.lazyLoadDispatcher(retrievalType);
		}
		return dispatcher;
	}

	private RetrievalDispatcher lazyLoadDispatcher(int targetRetrievalType) {
		RetrievalDispatcher targetDispatcher = null;
		try {
			Properties properties = new Properties();
			AssetManager assetManager = mContext.getAssets();
			InputStream inputStream = assetManager.open("dispatcher.properties");
			properties.load(inputStream);

			Enumeration<?> enumeration = properties.propertyNames();
			while (enumeration.hasMoreElements()) {
				String key = (String) enumeration.nextElement();
				int retrievalType = CommonUtil.parseInt(key);
//				if (!necessaryDispatcher(retrievalType)) continue;

				String property = properties.getProperty(key);
				Class<? extends RetrievalDispatcher> dispatcherClass = (Class<? extends RetrievalDispatcher>) Class.forName(property);
				RetrievalDispatcher dispatcher = dispatcherClass.newInstance();

				if (dispatcher != null) mDispatcherCaches.put(retrievalType, dispatcher);
				if (targetRetrievalType == retrievalType) targetDispatcher = dispatcher;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return targetDispatcher;
	}

//	private boolean necessaryDispatcher(int dispatcherKey) {
//		for (RetrievalType retrievalType : mRetrievalType) {
//			int type = retrievalType.getRetrievalType();
//			if (type == dispatcherKey) return true;
//		}
//		return false;
//	}

}
