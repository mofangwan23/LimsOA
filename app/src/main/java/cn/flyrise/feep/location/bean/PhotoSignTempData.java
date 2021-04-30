package cn.flyrise.feep.location.bean;

import com.amap.api.maps.model.LatLng;

import cn.flyrise.feep.location.contract.LocationWorkingContract;

/**
 * 新建：陈冕;
 * 日期： 2017-12-19-16:34.
 * 传递给拍照页面的数据
 */

public class PhotoSignTempData {

    public LocationWorkingContract mWorking; //考勤组详情
    public LocationSaveItem signInItem; //当前选中的地点详情
    public int locationType; //签到类型
    public String serviceTime; //当前服务端时间
    public LatLng currentLocation; //用户当前位置
    public int currentRange;//当前搜索周边成功，所用的半径
//    public String signAttdPname;//考勤点当前地点
//    public String signAttdPaddress;//考勤点当前地址
//    public LatLng signAttdLatLng;//考勤点当前坐标
//    public int signAttdRange;//考勤点当前允许的范围

    PhotoSignTempData(Bulider bulider) {
        mWorking = bulider.mWorking;
        signInItem = bulider.choiceItem;
        locationType = bulider.locationType;
        serviceTime = bulider.serviceTime;
        currentLocation = bulider.currentLocation;
        currentRange = bulider.currentRange;
//        signAttdPname = bulider.signAttdPname;
//        signAttdPaddress = bulider.signAttdPaddress;
//        signAttdLatLng = bulider.signAttdLatLng;
//        signAttdRange = bulider.signAttdRange;
    }

    public static class Bulider {

        private LocationWorkingContract mWorking; //考勤组详情
        private LocationSaveItem choiceItem; //当前选中的地点详情
        private int locationType; //签到类型
        private String serviceTime; //当前服务端时间
        private LatLng currentLocation; //用户当前位置
        private int currentRange;//当前搜索周边成功，所用的半径

//        private String signAttdPname;//考勤点当前地点
//        private String signAttdPaddress;//考勤点当前地址
//        private LatLng signAttdLatLng;//考勤点当前坐标
//        private int signAttdRange;//考勤点当前允许的范围

        public Bulider setWorking(LocationWorkingContract mWorking) {
            this.mWorking = mWorking;
            return this;
        }

        public Bulider setChoiceItem(LocationSaveItem choiceItem) {
            this.choiceItem = choiceItem;
            return this;
        }

        public Bulider setLocationType(int type) {
            this.locationType = type;
            return this;
        }

        public Bulider setServiceTime(String serviceTime) {
            this.serviceTime = serviceTime;
            return this;
        }

        public Bulider setCurrentLocation(LatLng location) {
            this.currentLocation = location;
            return this;
        }

        public Bulider setCurrentRange(int currentRange) {
            this.currentRange = currentRange;
            return this;
        }

//        public Bulider setSignAttdPname(String pname) { //考勤点名称
//            this.signAttdPname = pname;
//            return this;
//        }
//
//        public Bulider setSignAttdPaddress(String paddress) { //考勤点详细地址
//            this.signAttdPaddress = paddress;
//            return this;
//        }

//        public Bulider setSignAttdRange(int range) {
//            this.signAttdRange = range;
//            return this;
//        }

//        public Bulider setSignAttdSignLatLng(LatLng latLng) {
//            this.signAttdLatLng = latLng;
//            return this;
//        }

        public PhotoSignTempData bulider() {
            return new PhotoSignTempData(this);
        }

    }


}
