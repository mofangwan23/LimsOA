package cn.flyrise.feep.core.services;

import android.app.Activity;

/**
 * @author ZYP
 * @since 2017-03-01 10:40
 * 管理 Activity 声明周期的服务
 */
public interface IApplicationServices {

    int HOME_PRESS = 1;
    int HOME_PRESS_BUT_LOCKING = 2;
    int HOME_PRESS_AND_UN_LOCKED = 3;

    /**
     * 设置按下 home 键之后的状态
     */
    void setHomeKeyState(int homeKeyState);

    /**
     * 获取按下 home 键之后的状态
     */
    int getHomeKeyState();

    /**
     * 退出应用
     */
    void exitApplication();

    /**
     * 重新登录应用,是否加载注销接口
     */
    void reLoginApplication();

    /**
     * 重新登录应用,是否加载注销接口
     */
    void reLoginApplication(boolean isLoadLogout);

    /**
     * 获取当前可见的 Activity
     */
    Activity getFrontActivity();

    /**
     * 判断某个类型的activity是否在栈中
     *
     */
    boolean activityInStacks(Class<? extends Activity> activity);
}
