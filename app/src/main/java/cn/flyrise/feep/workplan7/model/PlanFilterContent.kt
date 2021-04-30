package cn.flyrise.feep.workplan7.model

import android.os.Parcel
import android.os.Parcelable
import java.util.*

/**
 * author : klc
 * Msg : 计划的筛选内容详情
 */
class PlanFilterContent() : Parcelable{

    var userIDs: String? = null
    var type: Int? = null
    var startTime: Long? = null
    var endTime: Long? = null


    constructor(parcel: Parcel) : this() {
        userIDs = parcel.readString()
        type = parcel.readValue(Int::class.java.classLoader) as? Int
        startTime = parcel.readValue(Long::class.java.classLoader) as? Long
        endTime = parcel.readValue(Long::class.java.classLoader) as? Long
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userIDs)
        parcel.writeValue(type)
        parcel.writeValue(startTime)
        parcel.writeValue(endTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PlanFilterContent> {
        override fun createFromParcel(parcel: Parcel): PlanFilterContent {
            return PlanFilterContent(parcel)
        }

        override fun newArray(size: Int): Array<PlanFilterContent?> {
            return arrayOfNulls(size)
        }
    }


}