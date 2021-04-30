package cn.flyrise.feep.location.fragment

import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.bean.SignInAttendanceData
import cn.flyrise.feep.location.bean.WorkingSignState
import cn.flyrise.feep.location.contract.SignInMainTabContract
import com.amap.api.maps.model.LatLng


/**
 * 新建：陈冕;
 * 日期： 2018-5-29-15:42.
 * 地点签到父类
 */

open class BaseSignInPlaceFragment : Fragment(), SignInMainTabContract.SignInPlaceListener {

    protected val signButProportion = 195f / 113f
    protected val signButMax = PixelUtil.dipToPx(113f)
    protected val signButMin = PixelUtil.dipToPx(80f)
    protected var mLayout: LinearLayout? = null
    protected var mLayoutSignIns: LinearLayout? = null
    var mRange: Int = 0

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mLayout?.getViewTreeObserver()?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val h = mLayout?.getHeight() ?: 0
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mLayout?.getViewTreeObserver()?.removeOnGlobalLayoutListener(this)
                } else {
                    mLayout?.getViewTreeObserver()?.removeGlobalOnLayoutListener(this)
                }
                var butHeight = (h / signButProportion).toInt()
                if (signButMax < butHeight || butHeight < signButMin) butHeight = signButMax
                FELog.i("-->>>>layout:${h}：：--but：${butHeight}")
                val rl = LinearLayout.LayoutParams(butHeight, butHeight)
                mLayoutSignIns?.layoutParams = rl
            }
        })
    }

    override fun getCurrentSaveItem(): LocationSaveItem? {
        return null
    }

    override fun getExceedDistance(latLng: LatLng): Float {
        return 0f
    }

    override fun notifiCurentLocation(latLng: LatLng) {

    }

    override fun notifiCurentWorkingData(mSignState: WorkingSignState, mSignData: SignInAttendanceData) {

    }

    override fun setSignInSuccessIcon(isShow: Boolean) {

    }

    override fun setSignInButtomEnabled(isEnabled: Boolean?) {

    }
}
