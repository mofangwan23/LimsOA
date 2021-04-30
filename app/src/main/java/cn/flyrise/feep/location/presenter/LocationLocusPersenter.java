package cn.flyrise.feep.location.presenter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.LocationLocusResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.location.bean.LocationLists;
import cn.flyrise.feep.location.bean.LocusDataProvider;
import cn.flyrise.feep.location.bean.LocusDates;
import cn.flyrise.feep.location.bean.LocusPersonLists;
import cn.flyrise.feep.location.contract.LocationLocusContract;
import cn.flyrise.feep.location.util.CharacterParser;
import cn.flyrise.feep.location.util.GpsHelper;
import cn.flyrise.feep.location.util.PinyinComparator;
import com.amap.api.location.AMapLocation;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-1-30-17:22.
 */

public class LocationLocusPersenter implements LocationLocusContract.presenter {

    private List<LatLng> latlngs;                                                       // 坐标集合
    private List<String> listTime;                                                      // 时间集合
    private List<String> listAddress;                                                   // 地址集合
    private String userName;
    private String userPhoto;
    private int currentIndex = 0;                                 // 记录绘制到第几个标记
    private int clickItmeDate = 0;                                 // 记录时日期列表点击的列
    private int clickItmeperson = 0;                                 // 记录下属列表点击的列
    private boolean isDateButSelected = false;
    private boolean isPersonButSelected = false;
    private Marker selectedMarker;                                                           // 绘制地图的标记
    private LocusDataProvider locusData;
    private LocationLocusResponse locusResponse;                                                 // 请求服务器返回的数据(人员列表、轨迹列表、日期)
    private LocationLocusResponse locusResponseperson;                                           // 请求服务器返回的数据(轨迹列表)

    //记录用户当前位置的坐标，用于当考勤轨迹为空的时候定位到用户当前位置,默认是珠海飞企地址
    private LatLng userLocationAddressLatlng = new LatLng(22.371993, 113.574035);
    private GpsHelper gpsHelper;
    private String applicationUserName;                                           // 获取到全局中的用户名
    private List<LocusPersonLists> personlist;

    private LocationLocusContract.View mView;
    private Context mContext;

    public LocationLocusPersenter(Context context) {
        this.mContext = context;
        mView = (LocationLocusContract.View) context;
        applicationUserName = CoreZygote.getLoginUserServices().getUserName();
        reGetLocation();
        planning();
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getUserPhoto() {
        return userPhoto;
    }

    @Override
    public void setSelectedMarker(Marker marker) {
        this.selectedMarker = marker;
    }

    @Override
    public void hideSelectedMarker() {
        if (selectedMarker.isInfoWindowShown()) {
            selectedMarker.hideInfoWindow();
        }

    }

    //进入考勤轨迹获取数据
    private void planning() {
        locusData = new LocusDataProvider(mContext);
        locusData.requestLocus(null, null);
        locusData.setResponseListener(new LocusDataProvider.OnLocationResponseListener() {

            @Override
            public void onSuccess(LocationLocusResponse responses, String locationType) {
                // 进入考勤轨迹界面请求返回用户自己的轨迹（请求类型为零、请求id为空、请求天数为空）
                if ("0".equals(responses.getRequestType())) {
                    locusResponse = responses;
                }
                if (locusResponse == null) {//当数据为空的时候影藏所有按钮
                    mView.setDateButVisibility(false);
                    mView.setPersonButVisibility(false);
                    latLngsEmpty();
                    return;
                }
                parsingData();
                drawRoute();// 绘制路线
                // 判断是否有时间集合，如没有将影藏
                if (locusResponse.getDateList() != null && locusResponse.getDateList().size() != 0) {
                    mView.setDateButVisibility(true);
                    mView.setDateButText(locusResponse.getDateList().get(0).getName());
                } else {
                    mView.setDateButVisibility(false);
                }
                // 判断是否有人员集合，如没有将影藏
                if (locusResponse.getPersonList() != null && locusResponse.getPersonList().size() != 0) {
                    mView.setPersonButText(locusResponse.getUserName());
                }
            }

            @Override
            public void onFailed(Throwable error, String content) {
                mView.setSelectButtonLayout(personlist);
            }
        });
        moveCurrentPosition();
    }

    //存储进入考勤轨迹界面获取到的数据
    private void parsingData() {
        parsingLocationListData();// 存储经纬度、地址、日期、时间、星期
        userName = locusResponse.getUserName();// 当前选中人员姓名
        userPhoto = locusResponse.getPhone();// 当前选中人员电话
        sortingPerson(); // 下属人员
        parsingListDate(); // 多天日期(算出星期)
    }

    //人员排序
    private void sortingPerson() {
        personlist = locusResponse.getPersonList();
        if (personlist == null) {
            return;
        }
        // 当人员为两人以上或者人员为一但不等于当前用户时，显示人员姓名
        mView.setPersonButVisibility(personlist.size() > 1 || !personlist.get(0).getUserName().equals(applicationUserName));
        //将汉字转换为拼音类
        final CharacterParser characterParser = CharacterParser.getInstance();
        //根据拼音来排列ListView里面的数据类
        final PinyinComparator pinyinComparator = new PinyinComparator();
        for (final LocusPersonLists person : personlist) {
            final String chars = characterParser.convert(person.getUserName().substring(0, 1));
            final String sortString = chars.substring(0, 1).toUpperCase();
            person.setIsChar(sortString);
        }
        // 根据a-z进行排序源数据
        Collections.sort(personlist, pinyinComparator);
        mView.initPersonSelectWindow(personlist, locusResponse.getUserId());
    }

    private void parsingListDate() {

        if (locusResponse == null || locusResponse.getDateList() == null) {
            return;
        }
        final List<LocusDates> dates = locusResponse.getDateList();
        if (dates.size() < 7) {
            mView.setDateButVisibility(false);
        } else if (dates.size() == 7) {
            mView.setDateButVisibility(true);
        }
        mView.initDateSelectWindow(dates);
    }

    @Override
    public void clickeDateButton() {
        if (isDateButSelected) {
            clickButtonHideListLayout();
        } else {
            clickButtonShowList(false);
        }
        mView.setRecyclerVisibility(true);
    }

    @Override
    public void clickPersonButton() {
        if (isPersonButSelected) {
            clickButtonHideListLayout();
        } else {
            clickButtonShowList(true);
        }
        mView.setRecyclerVisibility(false);
    }

    private void clickButtonShowList(boolean isShowPersonList) {
        if (isShowPersonList) {
            isPersonButSelected = true;
            isDateButSelected = false;
            mView.setSelectDateBut(false);
            mView.setSelectPersonBut(true);
        } else {
            isPersonButSelected = false;
            isDateButSelected = true;
            mView.setSelectDateBut(true);
            mView.setSelectPersonBut(false);
        }
        mView.setSelectListVisibility(true);
    }

    private void clickButtonHideListLayout() {
        mView.setSelectDateBut(false);
        mView.setSelectPersonBut(false);
        isPersonButSelected = false;
        isDateButSelected = false;
        mView.setSelectListVisibility(false);
    }

    private void parsingLocationListData() {
        latlngs = new ArrayList<>();
        listAddress = new ArrayList<>();
        listTime = new ArrayList<>();
        if (locusResponse.getLocationList() == null) {
            return;
        }
        double latitude;
        double longitude;
        String address;
        String time;
        for (LocationLists item : locusResponse.getLocationList()) {
            if (item == null) {
                continue;
            }
            latitude = Double.valueOf(item.getLatitude());
            longitude = Double.valueOf(item.getLongitude());
            address = item.getAddress();
            time = item.getTime();
            listTime.add(time);
            listAddress.add(address);
            latlngs.add(new LatLng(latitude, longitude));
        }
        latLngsEmpty();
    }

    @Override
    public void latLngsEmpty() {
        if (latlngs == null || latlngs.isEmpty()) {
            moveCurrentPosition();
            FEToast.showMessage(mContext.getResources().getString(R.string.location_null));
        }
    }

    private void parsingLocationListToData() {
        latlngs = new ArrayList<>();
        listAddress = new ArrayList<>();
        listTime = new ArrayList<>();
        if (locusResponseperson.getLocationList() == null) {
            return;
        }
        double latitude;
        double longitude;
        String address;
        String time;
        for (LocationLists item : locusResponseperson.getLocationList()) {
            if (item == null) {
                continue;
            }
            latitude = Double.valueOf(item.getLatitude());
            longitude = Double.valueOf(item.getLongitude());
            address = item.getAddress();
            time = item.getTime();
            listTime.add(time);
            listAddress.add(address);
            latlngs.add(new LatLng(latitude, longitude));
        }
        userName = locusResponseperson.getUserName();
        userPhoto = locusResponseperson.getPhone();
        if (latlngs.isEmpty()) {
            moveCurrentPosition();
            latLngsEmpty();
        }
    }

    //绘制轨迹路线
    private void drawRoute() {
        if (latlngs == null) {
            latLngsEmpty();
            return;
        }
        for (LatLng latLng : latlngs) {
            mapMarkers(latLng);
            currentIndex++;
        }
        currentIndex = 0;
        routeMap(latlngs);
    }

    // 多坐标调用（多位置需要绘制路线）
    private void mapMarkers(LatLng latlng) {
        // 用于改变起始标记
        final MarkerOptions markeroptions = new MarkerOptions();
        markeroptions.position(latlng);
        markeroptions.setFlat(true);
        // 标记贴在地上
        if (listAddress != null && listAddress.size() > currentIndex) {
            markeroptions.title(listAddress.get(currentIndex));
        }
        if (listTime != null && listTime.size() > currentIndex) {
            markeroptions.snippet(listTime.get(currentIndex));
        }
        if (currentIndex == 0) {
            mView.setAMapMoveCamera(CameraUpdateFactory.changeLatLng(latlng));
            markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            // 非起始位置的标记
            markeroptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }
        mView.setAMapAddMarker(markeroptions);
        if (selectedMarker != null) {
            selectedMarker.showInfoWindow();// 每次只显示一个提示语
        }
    }

    // 通过坐标完成路线绘制
    private void routeMap(List<LatLng> latlngs) {
        final PolylineOptions polyline = new PolylineOptions();
        polyline.addAll(latlngs);// 需要绘制路线的坐标
        polyline.width(8);// 设置绘制线的宽度
        polyline.color(Color.parseColor("#3f51b5"));// 设置绘制线的颜色
        mView.setAMapAddPolyline(polyline);
    }

    private void reGetLocation() {//定位数据
        if (gpsHelper == null) {
            gpsHelper = new GpsHelper(mContext.getApplicationContext());
        }
        gpsHelper.getSingleLocation(new GpsHelper.LocationCallBack() {
            @Override
            public void success(AMapLocation location) {
                userLocationAddressLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                moveCurrentPosition();
            }

            @Override
            public void error() {
                userLocationAddressLatlng = new LatLng(22.371993, 113.574035);
            }
        });
    }

    private void moveCurrentPosition() {
        mView.setAMapMoveCamera(CameraUpdateFactory.changeLatLng(userLocationAddressLatlng));
    }

    @Override
    public void onPersonClickeItem(int position) {
        resetSelectedLayout();
        clickItmeperson = position;// 获得点击的位置，并记录下来
        mView.setAMapClear();
        if (locusResponse != null) {
            mView.setPersonButText(locusResponse.getPersonList().get(clickItmeperson).getUserName());
            mView.setDateButText(locusResponse.getDateList().get(clickItmeDate).getName());
        }
        // 当用户点击下属人员是，将默认返回下属今天的轨迹
        requesPersonData();
    }

    @Override
    public void resetSelectedLayout() {
        isDateButSelected = false;
        isPersonButSelected = false;
        mView.setSelectDateBut(false);
        mView.setSelectPersonBut(false);
        mView.setSelectListVisibility(false);
    }

    @Override
    public void onDateClickeItem(int position) {
        if (locusResponse == null || locusResponse.getPersonList() == null) {
            return;
        }
        resetSelectedLayout();
        clickItmeDate = position;// 记录点击的日期项
        final String currentSelectDate = locusResponse.getDateList().get(position).getName();
        mView.setDateButText(currentSelectDate != null && !"".equals(currentSelectDate) ?
                locusResponse.getDateList().get(position).getName() : mContext.getResources().getString(R.string.location_date));
        mView.setAMapClear();
        requesPersonData();  // 当用户点击下属一周内任意一天日期时（除休息外），返回轨迹路线
    }

    //开始请求人员数据（加入Id、Date）请求类型还是0
    private void requesPersonData() {
        locusData = new LocusDataProvider(mContext);
        locusData.requestLocus(getSelectedUserId(), getSeledtedDate());
        locusData.setResponseListener(new LocusDataProvider.OnLocationResponseListener() {

            @Override
            public void onSuccess(LocationLocusResponse responses, String locationType) {
                if ("0".equals(responses.getRequestType())) {
                    locusResponseperson = responses;
                }
                if (locusResponseperson != null) {
                    parsingLocationListToData();// 将获取的的数据存储到为全局变量
                    drawRoute();// 绘制路线
                } else {
                    latLngsEmpty();
                }
            }

            @Override
            public void onFailed(Throwable error, String content) {
                latLngsEmpty();
            }
        });
    }

    private String getSelectedUserId() {
        if (locusResponse == null || locusResponse.getPersonList() == null) {
            return "";
        }
        if (locusResponse.getPersonList().get(clickItmeperson) == null) {
            return "";
        }
        return locusResponse.getPersonList().get(clickItmeperson).getUserId();
    }

    private String getSeledtedDate() {
        if (locusResponse == null || locusResponse.getDateList() == null) {
            return "";
        }
        if (locusResponse.getDateList().get(clickItmeDate) == null) {
            return "";
        }
        return locusResponse.getDateList().get(clickItmeDate).getDate();
    }

    @Override
    public void onDestroy() {
        clickItmeDate = 0;
        clickItmeperson = 0;
        if (gpsHelper != null) {
            gpsHelper.stopSingleLocationRequest();
        }
    }
}
