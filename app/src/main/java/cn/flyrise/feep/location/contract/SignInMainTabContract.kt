package cn.flyrise.feep.location.contract

import cn.flyrise.feep.location.bean.*
import com.amap.api.maps.model.LatLng

/**
 * 新建：陈冕;
 * 日期： 2018-5-29-13:32.
 */

interface SignInMainTabContract {

    interface SignInMainListener {

        fun notifiCurentLocation(latLng: LatLng) //更新用户当前位置

        fun notifiCurentWorkingData(signState: WorkingSignState, signData: SignInAttendanceData) //考勤组数据更新

        fun refreshListData(items: List<SignPoiItem>?) //更新搜索周边列表

        fun loadMoreListData(items: List<SignPoiItem>?) //加载更多

        fun signSuccessShowIcon() //签到成功显示签章

        fun restartWorkingTime() //重新请求考勤组

        fun setRefreshing(isRefresh: Boolean) //是否关闭刷新

        fun setSwipeRefreshEnabled(isEnabled: Boolean) //是否禁止刷新

        fun setSignInButtomEnabled(isEnabled: Boolean) //是否禁止点击签到

        fun getCurrentSignInItem(): LocationSaveItem?//获取当前考勤点信息

        fun loadMoreState(state: Int)//加载更多状态

        fun setSignRange(signRange: Int)//签到范围
    }

    interface SignInTabListener {//签到列表

        fun onRestartLocation() //重新定位刷新列表

        fun onLoadMoreData() //加载更多

        fun onSignInItem(saveItem: LocationSaveItem) //签到

        fun onSignInErrorItem() //异常拍照

        fun onSetAMapStyle(style: SignInSetAMapStyle) //设置地图样式

        fun onFrontViewClick() //划开签到列表，延迟8秒刷新界面
    }

    interface SignInPlaceListener {//签到地点

        fun getCurrentSaveItem(): LocationSaveItem?//获取当前签到坐标

        fun getExceedDistance(latLng: LatLng): Float //获取当前签到地点

        fun notifiCurentLocation(latLng: LatLng) //当前坐标更新

        fun notifiCurentWorkingData(mSignState: WorkingSignState, mSignData: SignInAttendanceData) //考勤组数据更新

        fun setSignInSuccessIcon(isShow: Boolean) //徽章是否显示

        fun setSignInButtomEnabled(isEnabled: Boolean?) //签到按钮是否能点击
    }
}
