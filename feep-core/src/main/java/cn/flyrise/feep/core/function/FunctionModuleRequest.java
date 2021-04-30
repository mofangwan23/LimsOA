package cn.flyrise.feep.core.function;

import android.support.annotation.Keep;
import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author 社会主义接班人
 * @since 2018-07-24 11:25
 */
@Keep
public class FunctionModuleRequest extends RequestContent {

	public static final String NAMESPACE = "FunctionModuleRequest";

	@Override
	public String getNameSpace() {
		return NAMESPACE;
	}

}