package cn.flyrise.feep.core.function;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.X.MainMenu;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * @author ZYP
 */
public class FunctionManager {

	private static final class Singleton {

		private static FunctionManager sInstance = new FunctionManager();
	}

	public static FunctionManager getInstance() {
		return Singleton.sInstance;
	}

	private IPreDefinedModuleRepository mDefinedModuleRepository;
	private IFunctionRepository mModuleRepository;
	private Context mContext;

	public void init(Context context, IPreDefinedModuleRepository definedModuleRepository) {
		this.mContext = context;
		this.mDefinedModuleRepository = definedModuleRepository;
	}

	public static IFunctionRepository getModuleRepository() {
		return getInstance().mModuleRepository;
	}

	public static IPreDefinedModuleRepository getDefinedModuleRepository() {
		return getInstance().mDefinedModuleRepository;
	}

	public static List<AppMenu> getAppMenu() {
		IFunctionRepository repository = getModuleRepository();
		return repository == null ? null : repository.getAppMenus();
	}

	public static List<AppTopMenu> getAppTopMenu() {
		IFunctionRepository repository = getModuleRepository();
		return repository == null ? null : repository.getTopMenu();
	}

	public static List<AppSubMenu> getAppSubMenu(int moduleId) {
		IFunctionRepository repository = getModuleRepository();
		return repository == null ? null : repository.getSubMenus(moduleId);
	}

	public static List<AppMenu> getStandardMenus(String key) {
		IFunctionProxy proxy = findApplicationProxy();
		return proxy == null ? null : proxy.getStandardMenus(key);
	}

	public static List<AppMenu> getQuickMenus() {
		IFunctionProxy proxy = findApplicationProxy();
		return proxy == null ? null : proxy.getQuickMenus();
	}

	public static List<Category> getCategories() {
		IFunctionRepository repository = getModuleRepository();
		return repository == null ? null : repository.getCategories();
	}

	public static void emptyData() {
		IFunctionRepository repository = getModuleRepository();
		if (repository != null) repository.emptyData();
	}

	public static Map<Category, List<AppMenu>> getCustomCategoryMenus() {
		IFunctionProxy proxy = findApplicationProxy();
		return proxy == null ? null : proxy.getCustomCategoryMenus();
	}

	public static Observable<Integer> saveDisplayOptions(Map<Category, List<AppMenu>> editMenus) {
		IFunctionProxy proxy = findApplicationProxy();
		return proxy == null ? Observable.just(200) : proxy.saveDisplayOptions(editMenus);
	}

	public static boolean hasPatch(int patch) {
		IFunctionRepository repository = getModuleRepository();
		return repository != null && repository.hasPatch(patch);
	}

	public static boolean hasModule(int moduleId) {
		IFunctionRepository repository = getModuleRepository();
		return repository != null && repository.hasModule(moduleId);
	}

	public static boolean isNative(int moduleId) {
		IFunctionRepository repository = getModuleRepository();
		return repository != null && repository.isNative(moduleId);
	}

	public static Module findModule(int moduleId) {
		IFunctionRepository repository = getModuleRepository();
		return repository == null ? null : repository.getModule(moduleId);
	}

	public static Class findClass(int moduleId) {
		IPreDefinedModuleRepository repository = getDefinedModuleRepository();
		return repository == null ? null : repository.getModuleClass(moduleId);
	}

	public static boolean isAssociateExist() {
		List<AppTopMenu> topMenu = getAppTopMenu();
		if (topMenu != null && topMenu.size() > 0) {
			for (AppTopMenu menu : topMenu) {
				if (TextUtils.equals(menu.type, MainMenu.Associate)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<Integer> getDisplayModuleIds() {
		if (mModuleRepository == null) return null;
		List<AppMenu> appMenus = mModuleRepository.getAppMenus();
		List<Integer> menuIds = new ArrayList<>(appMenus.size());
		for (AppMenu menu : appMenus) {
			menuIds.add(menu.menuId);
		}
		return menuIds;
	}

	/**
	 * 请求应用中心数据，并对数据进行处理，一步到位
	 */
	public Observable<FunctionDataSet> fetchFunctions() {
		return Observable.unsafeCreate(f -> {
			FunctionModuleRequest request = new FunctionModuleRequest();
			FEHttpClient.getInstance().post(request, new ResponseCallback<FunctionModuleResponse>() {
				@Override public void onCompleted(FunctionModuleResponse response) {
					if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
						// 1. 数据获取失败
						f.onNext(FunctionDataSet.errorDataSet());
						f.onCompleted();
						return;
					}

					if (CommonUtil.isEmptyList(response.modules)) {
						// 2. 空数据
						f.onNext(FunctionDataSet.emptyDataSet());
						f.onCompleted();
						return;
					}

					// 3. 根据补丁列表，创建合适的模块仓库对象
					String repositoryClass = getRepository(response.patches);
					try {
						Class repo = Class.forName(repositoryClass);
						Constructor constructor = repo.getConstructor(Context.class, IPreDefinedModuleRepository.class);
						mModuleRepository = (IFunctionRepository) constructor.newInstance(mContext, mDefinedModuleRepository);
					} catch (Exception exp) {
						exp.printStackTrace();
					}

					FunctionDataSet ds = new FunctionDataSet();
					ds.resultCode = FunctionDataSet.CODE_FETCH_SUCCESS;

					// 4. 保存数据
					if (mModuleRepository != null) {
						mModuleRepository.save(response);
						//5.初始化数据仓库
						mModuleRepository.initRepository();
						List<Integer> displayModuleds = getDisplayModuleIds();
						ds.appBadgeMap = new HashMap<>();

						// 6. 查找含有未读消息的应用
						for (Module module : response.modules) {
							if (!displayModuleds.contains(module.getModuleId())) continue;
							if (module.hasNews) {
								ds.appBadgeMap.put(module.getModuleId(), module.hasNews);
							}
						}

						// 7. 是否含有未读消息
						ds.hasUnreadMessage = !ds.appBadgeMap.isEmpty();
					}

					f.onNext(ds);
					f.onCompleted();
				}

				@Override public void onFailure(RepositoryException repository) {
					if (repository != null && repository.exception() != null) repository.exception().printStackTrace();
					f.onNext(FunctionDataSet.errorDataSet());
					f.onCompleted();
				}
			});
		});
	}

	private String getRepository(List<Integer> patches) {
		if (CommonUtil.isEmptyList(patches)) return RepositoryV6;
		boolean isV7Application = false;
		for (int patch : patches) {
			if (patch == 30) {
				isV7Application = true;
				break;
			}
		}

		if (isV7Application) return RepositoryV7;
		return RepositoryV6;
	}

	private static final String RepositoryV6 = "cn.flyrise.feep.main.modules.FunctionRepositoryV6";
	private static final String RepositoryV7 = "cn.flyrise.feep.main.modules.FunctionRepositoryV7";

	private static IFunctionProxy findApplicationProxy() {
		IFunctionProxy proxy = null;
		try {
			proxy = (IFunctionProxy) getModuleRepository();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return proxy;
	}
}
