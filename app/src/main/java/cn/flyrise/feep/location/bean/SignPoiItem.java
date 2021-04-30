package cn.flyrise.feep.location.bean;

import com.amap.api.services.core.PoiItem;

/**
 * 新建：陈冕;
 * 日期： 2017-11-2-14:38.
 */

public class SignPoiItem {

    public PoiItem poiItem;  //当前坐标的详情

    public LocationSaveItem saveItem;//本地缓存的坐标

    public boolean isUltraRange = false; //当前坐标是否超范围

    public boolean isSignSuccess = false;//是否显示打卡成功标签

}
