package cn.flyrise.feep.core.function;

/**
 * 首页底部菜单
 * @author ZYP
 */
public class AppTopMenu {

	public String menu;
	public String type;
	public int normalIcon;
	public int selectedIcon;

	public AppTopMenu() { }

	public AppTopMenu(String menu, String type, int normalIcon, int selectedIcon) {
		this.menu = menu;
		this.type = type;
		this.normalIcon = normalIcon;
		this.selectedIcon = selectedIcon;
	}

}
