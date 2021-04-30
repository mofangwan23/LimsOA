package cn.flyrise.feep.location.fragment

import android.os.Bundle
import cn.flyrise.feep.R
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.location.bean.LocationSignTime
import cn.flyrise.feep.location.contract.SignInMainTabContract
import cn.flyrise.feep.location.util.LocationSignDate

/**
 * 新建：陈冕;
 * 日期： 2018-5-25-15:16.
 * 考勤组-允许超范围签到，当前位置超出范围，列表签到
 */

class SignInAttendanceNearbyFragment : SignInDefaultNearbyFragment() {

    private var isSignMany = false

    fun setSignMany(isSignMany: Boolean) {
        this.isSignMany = isSignMany
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isSignMany) return
        if (mRecylerAdapter != null) mRecylerAdapter!!.setCanReport(false)
    }

    fun setCurrentServiceTime(serviceTime: LocationSignTime, isCanReport: Boolean) {
        if (isSignMany) return
        if (mRecylerAdapter != null) mRecylerAdapter!!.setCanReport(isCanReport)
        if (mRecylerAdapter != null) mRecylerAdapter!!.setWorkingState(LocationSignDate.getInstance().getSignTime(serviceTime))
    }

    override fun onSignWorkingClick() {
        FEToast.showMessage(context!!.resources.getString(R.string.location_time_overs))
    }

    companion object {

        fun getInstance(isSignMany: Boolean, listener: SignInMainTabContract.SignInTabListener): SignInAttendanceNearbyFragment {
            val fragment = SignInAttendanceNearbyFragment()
            fragment.setSignMany(isSignMany)
            fragment.setListener(listener)
            return fragment
        }
    }
}
