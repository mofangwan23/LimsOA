package cn.flyrise.feep.location.bean;

import android.text.TextUtils;
import com.amap.api.maps.model.LatLng;

/**
 * 新建：陈冕;
 * 日期： 2018-5-28-19:20.
 */

public class SignInAttendanceData {

	public String times;          // 打卡时间段
	public String pname;        //考勤名称
	public String paddress;    //考勤地址
	public LatLng latLng;      //考勤点坐标
	public String range;     //考勤范围

	private SignInAttendanceData(Builder builder) {
		this.times = builder.times;
		this.pname = builder.pname;
		this.paddress = builder.paddress;
		this.range = builder.range;
		this.latLng = builder.latLng;
		if (latLng == null) setLatLng(builder.latitude, builder.longitude);
	}

	private void setLatLng(String latitude, String longitude) {
		if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) return;
		try {
			latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	public static class Builder {

		private String times;          // 打卡时间段
		private String pname;        //考勤名称
		private String paddress;    //考勤地址
		private String range;     //考勤范围
		public LatLng latLng;      //考勤点坐标
		private String latitude;
		private String longitude;

		public Builder setTimes(String times) {
			this.times = times;
			return this;
		}

		public Builder setPname(String pname) {
			this.pname = pname;
			return this;
		}

		public Builder setPaddress(String paddress) {
			this.paddress = paddress;
			return this;
		}

		public Builder setLatLng(LatLng latLng) {
			this.latLng = latLng;
			return this;
		}

		public Builder setLatitude(String latitude) {
			this.latitude = latitude;
			return this;
		}

		public Builder setLongitude(String longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder setRange(String range) {
			this.range = range;
			return this;
		}

		public SignInAttendanceData builder() {
			return new SignInAttendanceData(this);
		}
	}
}
