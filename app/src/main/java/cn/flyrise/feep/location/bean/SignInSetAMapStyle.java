package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-28-14:35.
 */

public class SignInSetAMapStyle {

	public boolean isAMapSignStyle;//是否为考勤点样式，圆圈加考勤点

	public LatLng latLng;//用户当前坐标

	public LatLng saveLatLng;//考勤点、或自定义位置

	public int signRange;//签到范围

	public boolean isMoveMap;//是否移动到当前位置

	public List<PoiItem> signPoiItems;//用于更新地图上用户位置图标

	public boolean isDottedLine;//是否画虚线，考勤点距离超过50米画
}
