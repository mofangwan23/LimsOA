package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;

/**
 * 新建：陈冕;
 * 日期： 2017-11-16-18:58.
 * 拍照位置上报
 */

public class LocationPhotoItem {

	public boolean takePhoto; // 强制拍照

	public String sendType = "2"; //上传的类型

	public String longitude = "0.0"; //经度

	public String latitude = "0.0";//纬度

	public String time; //时间

	public String title; //单位名称

	public String address; //地址

	public String poiId; //地址的唯一值

	public int type; //签到类型（k.location.sign）

	public int takePhotoType;//拍照签到类型

	public LatLng workingLatLng;//考勤组坐标，校验用户是否离开考勤范围

	public int workingRange;//考勤范围

	public String endWorkingSignTime;//结束考勤时间，发送拍照数据使用

	public LatLng signLatLng;//签到地点的坐标

	public LatLng stringToLatLng() {
		try {
			return signLatLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return new LatLng(0.0, 0.0);
	}
}
