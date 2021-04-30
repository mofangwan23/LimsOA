package cn.flyrise.feep.core.function;

import java.util.Map;

/**
 * @author 社会主义接班人
 * @since 2018-07-24 14:00
 */
public final class FunctionDataSet {

	public static final int CODE_NO_FUNCTION = 2;           // 当前用户没有任何模块
	public static final int CODE_FETCH_FAILURE = 1;         // 获取应用模块异常
	public static final int CODE_FETCH_SUCCESS = 3;         // 成功

	public int resultCode;                      // 请求结果集
	public boolean hasUnreadMessage;            // 是否存在未读消息
	public Map<Integer, Boolean> appBadgeMap;   // 存在未读消息的应用

	public static FunctionDataSet errorDataSet() {
		FunctionDataSet ds = new FunctionDataSet();
		ds.resultCode = CODE_FETCH_FAILURE;
		return ds;
	}

	public static FunctionDataSet emptyDataSet() {
		FunctionDataSet ds = new FunctionDataSet();
		ds.resultCode = CODE_NO_FUNCTION;
		return ds;
	}

}
