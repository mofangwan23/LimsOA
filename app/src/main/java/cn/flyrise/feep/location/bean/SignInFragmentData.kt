package cn.flyrise.feep.location.bean

import cn.flyrise.feep.location.contract.SignInMainTabContract
import com.amap.api.maps.model.LatLng

/**
 * 新建：陈冕;
 *日期： 2018-8-4-15:33.
 */
class SignInFragmentData {
    var style: Int? = null
    var latLng: LatLng? = null
    var signState: WorkingSignState? = null
    var signData: SignInAttendanceData? = null
    var listener: SignInMainTabContract.SignInTabListener? = null

    var isSearch: Boolean = false
    var saveItem: LocationSaveItem? = null
}