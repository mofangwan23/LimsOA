package cn.flyrise.feep.location.contract;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import java.util.List;

/**
 * cm 2017-4-19.
 */

public interface LocationQueryPoiContract {

	String poiSearchKey = "餐饮服务|购物服务|生活服务|体育休闲服务|医疗保健服务|住宿服务|风景名胜|商务住宅|政府机构及社会团体|科教文化服务|交通设施服务|公司企业|道路附属设施|地名地址信息|公共设施";

	int search = 500; //默认考勤范围查询

	int pageSize = 15;//设置搜索周边每一页的数量

	int spaceTime = 8 * 1000;//定时刷新位置

	int TYPE_CUSTOM_CITY = 1; //市内搜索

	int TYPE_CUSTOM_COUNTRY = 2;//全国搜索

	int custom_search = 1000; //默认自定义查询范围

	int search_char_error = 2103;//搜索字符异常，提示切换字符，自定义考勤用到

	void getGPSLocation(int locationType); //定位

	void getRapidlyLocation(int locationType);//快捷定位

	void SearchKey(int locationType, String key);

	void loadMorePoiSearch(); // 上拉加载更多数据

	void stopLocationGps();//停止定位

	void destroyLocationGps();//销毁

	void requestLoactionPoiItem(int signStyle, int signRange); //搜索周边

	void requestLoactionPoiItem(LatLonPoint latLonPoint);//发送位置，滑动地图搜索周边

	void requestLoactionPoiItem();//当前位置搜索周边

	boolean hasMoreData();//是否能够加载更多

	interface OnQueryPoiItemListener {

		void gpsLocationSuccess(LatLng currentLatlng);//gps定位成功，判断是否获取周边和移动界面

		void showSwipeRefresh(boolean isShow);

		void setEmptyingAdapter();

		void refreshListData(List<PoiItem> items);

		void loadMoreListData(List<PoiItem> items);

		void loadMoreListFail();

		void loadMoreState(int state);
	}
}
