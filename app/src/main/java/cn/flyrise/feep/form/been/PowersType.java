/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-12 上午11:27:38
 */
package cn.flyrise.feep.form.been;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</       br> 修改时间：2013-3-12</br> 修改备注：</br>
 */
@Keep
public class PowersType implements Parcelable {

	private String filterType;
	private String parentItemType;
	private String dataSourceType;
	private String isMultiple;

	public int getFilterType() {
		return CommonUtil.parseInt(filterType);
	}

	public void setFilterType(int filterType) {
		this.filterType = filterType + "";
	}

	public int getParentItemType() {
		return CommonUtil.parseInt(parentItemType);
	}

	public void setParentItemType(int parentItemType) {
		this.parentItemType = parentItemType + "";
	}

	public int getDataSourceType() {
		return CommonUtil.parseInt(dataSourceType);
	}

	public void setDataSourceType(int dataSourceType) {
		this.dataSourceType = dataSourceType + "";
	}

	public boolean isMultiple() {
		Integer value = 0;
		try {
			value = Integer.valueOf(isMultiple);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return value == 1;
	}

	public void setMultiple(boolean isMultiple) {
		this.isMultiple = isMultiple ? "1" : "0";
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(dataSourceType);
		dest.writeString(filterType);
		dest.writeString(isMultiple);
		dest.writeString(parentItemType);
	}

	public static final Creator<PowersType> CREATOR = new Creator<PowersType>() {
		@Override
		public PowersType createFromParcel(Parcel source) {
			final PowersType types = new PowersType();
			types.dataSourceType = source.readString();
			types.filterType = source.readString();
			types.isMultiple = source.readString();
			types.parentItemType = source.readString();
			return types;

		}

		@Override
		public PowersType[] newArray(int size) {
			return null;
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

}
