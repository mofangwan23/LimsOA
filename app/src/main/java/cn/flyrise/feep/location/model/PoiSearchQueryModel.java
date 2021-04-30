package cn.flyrise.feep.location.model;

import android.content.Context;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.contract.LocationQueryPoiContract;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-18-9:31.
 */

public class PoiSearchQueryModel {

	private PoiSearch.Query poiSearchQuery;
	private PoiSearch poiSearch;

	private int currentPageNum;

	private PoiSearchQueryModel(Builder builder) {
		if (builder.mLocationType == K.location.LOCATION_SEARCH || builder.mLocationType == K.location.LOCATION_CUSTOM_SEARCH) {
			poiSearchQuery = new PoiSearch.Query(builder.searchKay, "", "");
		}
		else {
			poiSearchQuery = new PoiSearch.Query("", builder.queryRangeKey, "");
		}
		currentPageNum = builder.currentPageNum;
		poiSearchQuery.setPageSize(builder.pageSize); // 每页数量
		poiSearchQuery.setPageNum(builder.currentPageNum); // 页码
		poiSearch = new PoiSearch(builder.mContext, poiSearchQuery);
		setPoiSearchBound(builder);
		poiSearch.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
			@Override
			public void onPoiSearched(PoiResult result, int code) {
				if (result == null || !isPoiSearchQueryDataEffective(result, code) || isServicePoiItem(builder.searchKay,
						builder.mLocationType)) {
					builder.listener.onQueryFailure();
					return;
				}
				builder.listener.onQuerySuccess(result.getPageCount(), result.getPois(), currentPageNum);
			}

			@Override
			public void onPoiItemSearched(PoiItem arg0, int arg1) {
				builder.listener.onQueryFailure();
			}
		});
	}

	private boolean isServicePoiItem(String searchKey, int locationType) {//带参数搜索搜索失败
		return (locationType == K.location.LOCATION_SEARCH || locationType == K.location.LOCATION_CUSTOM_SEARCH)
				&& TextUtils.isEmpty(searchKey);
	}

	private void setPoiSearchBound(Builder builder) {
		if (builder.currentLatLonPoint == null) {
			return;
		}
		int currentSearchRange = 0;
		if (builder.mLocationType == K.location.LOCATION_SEARCH) { //搜索签到
//			currentSearchRange = isWorkingRangeSearch(builder.signStyle, builder.workingRange) ? builder.workingRange : builder.search;
			currentSearchRange = builder.workingRange;
			poiSearch.setBound(new PoiSearch.SearchBound(builder.currentLatLonPoint, currentSearchRange, true));
			return;
		}

		if (builder.mLocationType == K.location.LOCATION_SIGN) {
			currentSearchRange = builder.workingRange;
			poiSearch.setBound(new PoiSearch.SearchBound(builder.currentLatLonPoint, currentSearchRange, true));
			return;
		}

		if (builder.mLocationType != K.location.LOCATION_CUSTOM_SEARCH) {
			currentSearchRange = builder.search;
			poiSearch.setBound(new PoiSearch.SearchBound(builder.currentLatLonPoint, currentSearchRange, true));
			return;
		}
		if (builder.customSearchAddressType == LocationQueryPoiContract.TYPE_CUSTOM_COUNTRY) {//不设置Bound和城市就是全国搜索
			return;
		}
		currentSearchRange = builder.customSearch;
		poiSearch.setBound(new PoiSearch.SearchBound(builder.currentLatLonPoint, currentSearchRange, true));
	}

//	private boolean isWorkingRangeSearch(int signStyle, int workingRange) {//是否使用服务端传递的考勤位置
//		return workingRange > 0 && (signStyle == K.sign.STYLE_ATT || signStyle == K.sign.STYLE_LIST_ATT);
//	}

	private boolean isPoiSearchQueryDataEffective(PoiResult result, int code) {
		return code == AMapException.CODE_AMAP_SUCCESS && isCurrentQuery(result.getQuery());
	}

	private boolean isCurrentQuery(PoiSearch.Query query) {
		return query != null && query.equals(poiSearchQuery);
	}

	boolean isPoiSerchQueryNull() {
		return poiSearchQuery == null || poiSearch == null;
	}

	void setPageNum(int currentPageNum) {
		this.currentPageNum = currentPageNum;
		if (isPoiSerchQueryNull()) {
			return;
		}
		poiSearchQuery.setPageNum(currentPageNum);// 设置查后一页
	}

	void startQuery() {
		if (isPoiSerchQueryNull()) {
			return;
		}
		poiSearch.searchPOIAsyn();
	}

	public static class Builder {

		private int mLocationType;
		private String searchKay;
		private String queryRangeKey;
		private int pageSize;
		private int currentPageNum;
		private LatLonPoint currentLatLonPoint;
		private int search;
		private int customSearchAddressType;
		private int customSearch;
		private int signStyle;
		private int workingRange;

		private Context mContext;
		private OnSerqQueryListener listener;

		public Builder(Context context, OnSerqQueryListener listener) {
			this.mContext = context;
			this.listener = listener;
		}

		Builder setLocationType(int mLocationType) {
			this.mLocationType = mLocationType;
			return this;
		}

		Builder setSearchKay(String searchKay) {
			this.searchKay = searchKay;
			return this;
		}

		Builder setQueryRangeKey(String queryRangeKey) {
			this.queryRangeKey = queryRangeKey;
			return this;
		}

		public Builder setPageSize(int pageSize) {
			this.pageSize = pageSize;
			return this;
		}

		Builder setCurrentPageNum(int currentPageNum) {
			this.currentPageNum = currentPageNum;
			return this;
		}

		Builder setCurrentLatLonPoint(LatLonPoint currentLatLonPoint) {
			this.currentLatLonPoint = currentLatLonPoint;
			return this;
		}

		public Builder setSearch(int search) {
			this.search = search;
			return this;
		}

		Builder setCustomSearchAddressType(int customSearchAddressType) {
			this.customSearchAddressType = customSearchAddressType;
			return this;
		}

		Builder setCustomSearch(int customSearch) {
			this.customSearch = customSearch;
			return this;
		}

		Builder setSignStyle(int isSignStyle) {
			this.signStyle = isSignStyle;
			return this;
		}

		public Builder setWorkingRange(int workingRange) {
			this.workingRange = workingRange;
			return this;
		}

		public PoiSearchQueryModel build() {
			return new PoiSearchQueryModel(this);
		}
	}

	public interface OnSerqQueryListener {

		void onQuerySuccess(int pageCount, List<PoiItem> items, int currentPageNum);

		void onQueryFailure();
	}

}
