package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.PhotoSignTempData;
import cn.flyrise.feep.location.event.EventLocationSignSuccess;

/**
 * 新建：陈冕;
 * 日期： 2017-12-19-16:16.
 * 数据上报
 */

public interface LocationReportSignContract {

	int POST_PHOTO_SIGN_DATA = 473; //传给拍照签到的请求

	String LOCATION_PHOTO_ITEM = "location_photo_item";//封装的签到数据

	void reportDataRequest(PhotoSignTempData tempData); //位置数据上报

	void photoSignError(PhotoSignTempData tempData);

	void requestHistory(boolean isTakePhotoError);//历史记录请求,老版本服务端不会回传签到成功数据，需要自己获取

	interface ReportSignListener {

		void onReportSetCheckedItem();//开始上传数据，停止所有的签到操作

		void onReportFailure(String errorText, int errorType);//签到异常

		void onReportHistorySuccess(EventLocationSignSuccess signSuccess);//对比历史记录正确，签到成功

		void onReportPhotoDismiss(boolean isSurePhoto);//取消拍照签到输入框
	}

}
