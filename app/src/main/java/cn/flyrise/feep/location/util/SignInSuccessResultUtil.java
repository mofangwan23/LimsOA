package cn.flyrise.feep.location.util;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.location.LocationResponse;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.location.event.EventLocationSignSuccess;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-4-19-16:21.
 */

public class SignInSuccessResultUtil {

	public static EventLocationSignSuccess getSignReportSuccess(FEListItem listItem, boolean isTakePhotoError) {
		if (listItem == null || TextUtils.isEmpty(listItem.getDate()) || TextUtils.isEmpty(listItem.getTime())) {
			return null;
		}
		EventLocationSignSuccess signSuccess = new EventLocationSignSuccess();
		signSuccess.isTakePhotoError = isTakePhotoError;
		signSuccess.title = listItem.getName();
		signSuccess.content = TextUtils.isEmpty(listItem.getPdesc()) ? listItem.getAddress() : listItem.getPdesc();
		signSuccess.time = listItem.getTime();
		signSuccess.week = listItem.getWhatDay();
		signSuccess.date = listItem.getDate();
		return signSuccess;
	}

	public static EventLocationSignSuccess getSignReportSuccess(List<LocationResponse.ResultLocationData> data,
			boolean isTakePhotoError) {
		return getSignReportSuccess(getFEListItem(data), isTakePhotoError);
	}

	private static FEListItem getFEListItem(List<LocationResponse.ResultLocationData> dataList) {
		if (CommonUtil.isEmptyList(dataList)) {
			return null;
		}
		final FEListItem item = new FEListItem();
		for (final LocationResponse.ResultLocationData listDataItem : dataList) {
			if ("id".equals(listDataItem.getName())) {
				item.setId(listDataItem.getValue());
			}
			else if ("title".equals(listDataItem.getName())) {
				item.setTitle(listDataItem.getValue());
			}
			else if ("sendTime".equals(listDataItem.getName())) {
				item.setSendTime(listDataItem.getValue());
			}
			else if ("sendUser".equals(listDataItem.getName())) {
				item.setSendUser(listDataItem.getValue());
			}
			else if ("msgId".equals(listDataItem.getName())) {
				item.setMsgId(listDataItem.getValue());
			}
			else if ("msgType".equals(listDataItem.getName())) {
				item.setMsgType(listDataItem.getValue());
			}
			else if ("requestType".equals(listDataItem.getName())) {
				item.setRequestType(listDataItem.getValue());
			}
			else if ("date".equals(listDataItem.getName())) { // 以下为位置上报历史记录新增的2013-10-22
				item.setDate(listDataItem.getValue());
			}
			else if ("whatDay".equals(listDataItem.getName())) {
				item.setWhatDay(listDataItem.getValue());
			}
			else if ("time".equals(listDataItem.getName())) {
				item.setTime(listDataItem.getValue());
			}
			else if ("address".equals(listDataItem.getName())) {
				item.setAddress(listDataItem.getValue());
			}
			else if ("name".equals(listDataItem.getName())) {
				item.setName(listDataItem.getValue());
			}
			else if ("guid".equals(listDataItem.getName())) {// 现场签到的图片2015-02-10,by luozhanjian
				item.setImageHerf(listDataItem.getValue());
				item.setGuid(listDataItem.getValue());
			}
			else if ("pdesc".equals(listDataItem.getName())) {// 图片描述
				item.setPdesc(listDataItem.getValue());
			}
			else if ("sguid".equals(listDataItem.getName())) {// 图片的缩略图
				item.setSguid(listDataItem.getValue());
			}
			else if ("content".equals(listDataItem.getName())) {
				item.setContent(listDataItem.getValue());
			}
			else if ("badge".equals(listDataItem.getName())) {
				item.setBadge(listDataItem.getValue());
			}
			else if ("category".equals(listDataItem.getName())) {
				item.setCategory(listDataItem.getValue());
			}
			else if ("sendUserImg".equals(listDataItem.getName())) {
				item.setSendUserImg(listDataItem.getValue());
			}
		}
		return item;
	}

}
