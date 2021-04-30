package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.LocusPersonLists;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.util.Date;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-30-17:21.
 */

public interface SignInTrackContract {

	interface View {

		String USER_ID = "user_id";
		String LOCATION_DAY = "location_day";

		void resultData(PolylineOptions polyline);//绘制连线

		void setAMapMoveCamera(CameraUpdate camera);

		void setAMapAddMarker(MarkerOptions markeroptions);

		void resultPerson(List<LocusPersonLists> personLists);
	}

	interface presenter {

		void requestPerson(String day);

		void requesPersonData(String userId, String day);

		String getUserPhoto();

		String getUserName();

		Date textToDate(String yearMonth);

		void onDestroy();
	}

}
