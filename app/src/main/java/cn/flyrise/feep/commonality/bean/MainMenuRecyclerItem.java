package cn.flyrise.feep.commonality.bean;


/**
 * Created by Administrator on 2016-7-12.
 */
public class MainMenuRecyclerItem {
    public int menuId;
    public String menuName;
    public Integer menuIcon;
    public String userId;


    public void setRecyclerItem(int menuId, String name, Integer icon) {
        this.menuId = menuId;
        this.menuName = name;
        this.menuIcon = icon;
    }

    public void setRecyclerItem(int menuId, String name, Integer icon, String userId) {
        this.menuId = menuId;
        this.menuName = name;
        this.menuIcon = icon;
        this.userId = userId;
    }
}
