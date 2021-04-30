package cn.flyrise.feep.core.function;

import java.util.List;

/**
 * @author ZYP
 */
public interface IFunctionRepository {

	// 模块注册
	void save(FunctionModuleResponse result);

	// 是否是原生模块
	boolean isNative(int moduleId);

	// 是否存在这个模块
	boolean hasModule(int moduleId);

	// 获取指定模块
	Module getModule(int moduleId);

	// 获取服务器标准展示的模块
	List<Module> getModules();

	// 获取首页菜单
	List<AppTopMenu> getTopMenu();

	// 获取模块列表
	List<AppMenu> getAppMenus();

	// 获取审批子菜单子菜单
	List<AppSubMenu> getSubMenus(int moduleId);

	// 是否存在这个类型
	boolean hasCategory(Category category);

	// 获取指定类型
	Category getCategory(String categoryId);

	// 获取类型列表
	List<Category> getCategories();

	// 是否存在这个补丁
	boolean hasPatch(int patchCode);

	// 获取自定义排序规则
	List<Integer> getAppSortRules();

	// 获取指定快捷方式
	ShortCut getQuick(int quickId);

	// 获取自定义快捷方式
	List<Integer> getAppShortCuts();

	// 获取全部的自定义快捷方式
	List<ShortCut> getAllAppShortCuts();

	void initRepository();

	//清空缓存的数据
	void emptyData();
}
