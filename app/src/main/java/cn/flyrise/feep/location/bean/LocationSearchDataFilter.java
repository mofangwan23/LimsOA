package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.K;
import cn.flyrise.feep.location.util.SignInUtil;

/**
 * 新建：陈冕;
 * 日期： 2018-3-23-17:09.
 */

public class LocationSearchDataFilter {

    private int type;
    private boolean isNotAllowSuperRange = false;
    private List<PoiItem> items;
    private int search;
    private LatLng signLatLng;
    private int signRange;

    public LocationSearchDataFilter(Builder builder) {
        this.type = builder.type;
        this.isNotAllowSuperRange = builder.isNotAllowSuperRange;
        this.items = builder.items;
        this.search = builder.search;
        this.signLatLng = builder.signLatLng;
        this.signRange = builder.signRange;
    }

    /**
     * isNotAllowSuperRange
     * false:服务端允许超范围考勤，周边数据不用筛选
     * true:服务端不允许考勤组超范围考勤，周边数据需要筛选
     */
    public List<SignPoiItem> getData() {
        if (isNotAllowSuperRange) {
            return getSignPoiItem(items, signLatLng, signRange);
        } else {
            removeExceedDistance(items, type, search);
            return SignInUtil.poiItemsToSignPoiItem(items);
        }
    }

    //删除超出范围的地址
    private void removeExceedDistance(List<PoiItem> poiItems, int locationType, int signRange) {
        if (locationType == K.location.LOCATION_CUSTOM_SEARCH) {
            return;
        }
        List<PoiItem> errorPoi = new ArrayList<>();
        for (PoiItem item : poiItems) {
            if (item == null) {
                continue;
            }
            if (item.getDistance() > signRange) {
                errorPoi.add(item);
            }
        }
        poiItems.removeAll(errorPoi);
    }

    //转换成适配器使用的类型，并去除超出范围的数据
    private List<SignPoiItem> getSignPoiItem(List<PoiItem> poiItems, LatLng signLatLng, int signRange) {
        List<SignPoiItem> arrayList = new ArrayList<>();
        SignPoiItem signPoiItem;
        for (PoiItem item : poiItems) {
            if (item == null) {
                continue;
            }
            signPoiItem = new SignPoiItem();
            signPoiItem.poiItem = item;
            signPoiItem.isUltraRange = isCurrentLocationWorkingExceed(item.getLatLonPoint(), signLatLng, signRange);
            arrayList.add(signPoiItem);
        }
        return arrayList;
    }

    private static boolean isCurrentLocationWorkingExceed(LatLonPoint latLng, LatLng signLatLng, int signRange) {
        return SignInUtil.getExceedDistance(new LatLng(latLng.getLatitude()
                , latLng.getLongitude()), signLatLng, signRange) <= 0;
    }

    public static class Builder {
        private int type;
        private boolean isNotAllowSuperRange = false;
        private List<PoiItem> items;
        private int search;
        private LatLng signLatLng;
        private int signRange;

        //当前考勤类型
        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        //服务端不允许超范围考勤
        public Builder setNotAllowSuperRange(boolean isNotAllowSuperRange) {
            this.isNotAllowSuperRange = isNotAllowSuperRange;
            return this;
        }

        //搜索周边的数据集合
        public Builder setItems(List<PoiItem> items) {
            this.items = items;
            return this;
        }

        //非考勤组默认考勤范围，一般为500
        public Builder setSearch(int search) {
            this.search = search;
            return this;
        }

        //考勤点签到地址，服务端设置
        public Builder setSignLatLng(LatLng signLatLng) {
            this.signLatLng = signLatLng;
            return this;
        }

        //考勤点签到范围，服务端设置
        public Builder setSignRange(int signRange) {
            this.signRange = signRange;
            return this;
        }

        public LocationSearchDataFilter builder() {
            return new LocationSearchDataFilter(this);
        }
    }
}
