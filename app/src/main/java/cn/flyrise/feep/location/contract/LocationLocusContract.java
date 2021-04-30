package cn.flyrise.feep.location.contract;

import cn.flyrise.feep.location.bean.LocusDates;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-30-17:21.
 */

public interface LocationLocusContract {

	interface View {

		String USER_ID = "user_id";
		String LOCATION_DAY = "location_day";

		void setRecyclerVisibility(boolean isDateVisibility);

		boolean isSelectListVisible();

		void setSelectListVisibility(boolean isVisibility);

		void setDateButText(String text);

		void setPersonButText(String text);

		void setSelectDateBut(boolean isSelected);

		void setSelectPersonBut(boolean isSelected);

		void setDateButVisibility(boolean isVisibility);

		void setPersonButVisibility(boolean isVisibility);

		void setSelectButtonLayout(List<LocusPersonLists> personlist);

		void setAMapMoveCamera(CameraUpdate update);

		void setAMapAddMarker(MarkerOptions markeroptions);

		void setAMapAddPolyline(PolylineOptions polyline);

		void setAMapClear();

		void initPersonSelectWindow(List<LocusPersonLists> personLists, String userId);

		void initDateSelectWindow(List<LocusDates> dates);
	}

	interface presenter {

		String getUserName();

		String getUserPhoto();

		void latLngsEmpty();

		void setSelectedMarker(Marker marker);

		void hideSelectedMarker();

		void clickeDateButton();

		void clickPersonButton();

		void onPersonClickeItem(int position);

		void onDateClickeItem(int position);

		void resetSelectedLayout();

		void onDestroy();
	}

}
