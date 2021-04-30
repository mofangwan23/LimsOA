package cn.flyrise.feep.commonality;

/**
 * @author ZYP
 * @since 2017-06-20 11:00
 */
public interface EditableActivity {

    /**
     * 初始化缓存
     */
    int initCache();

    /**
     * 保存缓存
     */
    void saveCache();

}
