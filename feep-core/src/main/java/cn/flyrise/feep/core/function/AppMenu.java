package cn.flyrise.feep.core.function;

import android.support.annotation.Keep;
import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * @author ZYP
 */
@Keep
public class AppMenu implements Serializable {

	public static final int ID_EMPTY = 1014;
	public static final int ID_CATEGORY = 1015;
	public static final int ID_EMPTY_SHORT_CUT = 1016;

	public boolean hasNews;         // 是否有未读消息
	public boolean editable;        // 该模块是否可以编辑

	public int menuId;
	@SerializedName("menuName") public String menu;
	@SerializedName("url") public String appURL;
	@SerializedName("icon") public String icon;
	@SerializedName("menuIcon") public int imageRes;

	public AppMenu() { }

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AppMenu appMenu = (AppMenu) o;

		if (menuId != appMenu.menuId) return false;
		return menu.equals(appMenu.menu);
	}

	@Override public int hashCode() {
		int result = menuId;
		result = 31 * result + menu.hashCode();
		return result;
	}

	public static AppMenu fromModule(Module module) {
		AppMenu menu = new AppMenu();
		menu.menuId = module.getModuleId();
		menu.menu = module.name;
		menu.appURL = module.url;
		menu.icon = module.icon;
		return menu;
	}

	public static AppMenu blankMenu() {
		AppMenu menu = new AppMenu();
		menu.menuId = ID_EMPTY;
		menu.menu = "null";
		return menu;
	}

	public static AppMenu categoryMenu(Category c) {
		AppMenu menu = new AppMenu();
		menu.menuId = ID_CATEGORY;
		menu.menu = c.value;
		return menu;
	}

	public static AppMenu fromShortCut(ShortCut pc, PreDefinedShortCut psc) {
		AppMenu menu = new AppMenu();
		menu.menuId = pc.id;
		menu.menu = pc.name;
		menu.icon = pc.icon;
		menu.appURL = pc.url;
		if (psc != null) {
			menu.imageRes = psc.imageRes;
		}

		if (TextUtils.isEmpty(menu.menu) && psc != null) {
			menu.menu = psc.name;
		}

		return menu;
	}

	public static AppMenu emptyShortCutMenu() {
		AppMenu menu = new AppMenu();
		menu.menuId = ID_EMPTY_SHORT_CUT;
		menu.menu = "null";
		return menu;
	}

}
