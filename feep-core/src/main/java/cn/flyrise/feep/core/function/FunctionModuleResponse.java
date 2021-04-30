package cn.flyrise.feep.core.function;

import android.support.annotation.Keep;
import cn.flyrise.feep.core.network.request.ResponseContent;
import java.util.List;
import java.util.Map;

/**
 * @author 社会主义接班人
 * @since 2018-07-24 11:25
 */
@Keep
public class FunctionModuleResponse extends ResponseContent {

	public List<Module> modules;
	public Map<String, List<SubModule>> moduleChildren;
	public List<Integer> patches;

	// 以下 V7.0 版本新增
	public List<Category> category;
	public List<Integer> customIds;
	public List<Integer> quick;
	public List<ShortCut> quickAll;


}
