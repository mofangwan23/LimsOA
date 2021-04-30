package cn.flyrise.feep.core.function;

import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * @author 社会主义接班人
 * @since 2018-08-01 17:57
 */
public interface IFunctionProxy {

	List<AppMenu> getAppMenus();

	List<AppMenu> getQuickMenus();

	Map<Category, List<AppMenu>> getCustomCategoryMenus();

	List<AppMenu> getStandardMenus(String category);

	List<AppMenu> getCustomMenus(String category);

	Observable<Integer> saveDisplayOptions(Map<Category, List<AppMenu>> customs);

}
