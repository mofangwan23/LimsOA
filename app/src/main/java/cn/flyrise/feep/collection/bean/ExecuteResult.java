package cn.flyrise.feep.collection.bean;

/**
 * @author ZYP
 * @since 2018-05-18 10:43
 */
public class ExecuteResult {

	public int errorCode;
	public String errorMessage;

	public static ExecuteResult successResult() {
		return new ExecuteResult();
	}

	public static ExecuteResult errorResult(int errorCode, String errorMessage) {
		ExecuteResult result = new ExecuteResult();
		result.errorCode = errorCode;
		result.errorMessage = errorMessage;
		return result;
	}

}
