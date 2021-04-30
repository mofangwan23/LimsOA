package cn.flyrise.feep.core.function;

/**
 * 子菜单
 * @author ZYP
 */
public class AppSubMenu {

	public int menuId;
	public String menu;

	public AppSubMenu() { }

	public AppSubMenu(int menuId, String menu) {
		this.menuId = menuId;
		this.menu = menu;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		AppSubMenu that = (AppSubMenu) o;

		if (menuId != that.menuId) return false;
		return menu.equals(that.menu);
	}

	@Override public int hashCode() {
		int result = menuId;
		result = 31 * result + menu.hashCode();
		return result;
	}

}
