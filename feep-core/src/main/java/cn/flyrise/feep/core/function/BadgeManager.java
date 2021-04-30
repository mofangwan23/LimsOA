package cn.flyrise.feep.core.function;

import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Observable;

/**
 * @author 社会主义接班人
 * @since 2018-07-24 12:00
 */
public class BadgeManager {

	private BadgeManager() { }

	private static final class Singleton {

		private static final BadgeManager sInstance = new BadgeManager();
	}

	public static BadgeManager getInstance() {
		return Singleton.sInstance;
	}

	/**
	 * 获取当前用户的 App 未读消息数量
	 * @return Int > 0 表示存在未读消息数
	 */
	public Observable<Integer> fetchUnreadMessage() {
		return Observable.unsafeCreate(f -> {
			FunctionModuleRequest request = new FunctionModuleRequest();
			FEHttpClient.getInstance().post(request, new ResponseCallback<FunctionModuleResponse>() {
				@Override public void onCompleted(FunctionModuleResponse response) {
					if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
						f.onNext(0);
						f.onCompleted();
						return;
					}

					List<Integer> displayModuleIds = FunctionManager.getInstance().getDisplayModuleIds();
					if (CommonUtil.isEmptyList(displayModuleIds)) {
						f.onNext(0);
						f.onCompleted();
						return;
					}

					List<Module> modules = response.modules;
					int unreadMessageCount = 0;

					for (Module module : modules) {
						if (!displayModuleIds.contains(module.getModuleId())) continue;
						if (module.hasNews) unreadMessageCount++;
					}

					f.onNext(unreadMessageCount);
					f.onCompleted();
				}

				@Override public void onFailure(RepositoryException repository) {
					f.onNext(0);
					f.onCompleted();
				}
			});
		});
	}

	/**
	 * 获取应用中心的未读数据
	 * @return
	 */
	public Observable<Map<Integer, Boolean>> fetchFunctionBadge() {
		return Observable.unsafeCreate(f -> {
			FunctionModuleRequest request = new FunctionModuleRequest();
			FEHttpClient.getInstance().post(request, new ResponseCallback<FunctionModuleResponse>() {
				@Override public void onCompleted(FunctionModuleResponse response) {
					if (response == null || !TextUtils.equals(response.getErrorCode(), "0")) {
						f.onNext(null);
						f.onCompleted();
						return;
					}

					Map<Integer, Boolean> badgeMap = new HashMap<>();
					List<Module> modules = response.modules;
					for (Module module : modules) {
						if (module.hasNews) {
							badgeMap.put(module.getModuleId(), module.hasNews);
						}
					}

					f.onNext(badgeMap);
					f.onCompleted();
				}

				@Override public void onFailure(RepositoryException repository) {
					f.onNext(null);
					f.onCompleted();
				}
			});
		});
	}
}
