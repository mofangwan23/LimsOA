package cn.flyrise.feep.location.views

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseEditableActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.location.adapter.LocationCustomModifyAdapter
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.event.EventCustomCreateAddress
import cn.flyrise.feep.location.event.EventTempCustomSignAddress
import cn.flyrise.feep.location.util.LocationCustomSaveUtil
import kotlinx.android.synthetic.main.location_custom_modify_layout.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2017-11-6-15:15.
 * 自定义考勤点编辑界面
 */

class SignInCustomModifyActivity : BaseEditableActivity() {

    private var mToolbar: FEToolbar? = null
    private var mAdapter: LocationCustomModifyAdapter? = null
    private var mSaveLocations: MutableList<LocationSaveItem>? = null
    private var mOriginalData: MutableList<LocationSaveItem> = ArrayList()//原始数据，判断是否有修改

    private fun getSavePoiIds() = mSaveLocations?.map { it.poiId }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_custom_modify_layout)
        EventBus.getDefault().register(this)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        this.mToolbar = toolbar
        toolbar.title = this.resources.getString(R.string.location_custom_list_title)
        toolbar.rightText = this.resources.getString(R.string.location_custom_success)
    }

    override fun bindData() {
        super.bindData()
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
        mSaveLocations = LocationCustomSaveUtil.getSavePoiItems()
        if (mSaveLocations != null) mOriginalData = LocationCustomSaveUtil.getSavePoiItems()
        setAddCustomVisibility(mSaveLocations)
        mAdapter = LocationCustomModifyAdapter(mSaveLocations)
        mRecyclerView!!.adapter = mAdapter
    }

    override fun bindListener() {
        super.bindListener()
        mToolbar!!.setNavigationOnClickListener { if (isHoshWork()) showExitDialog() else finish() }
        mToolbar!!.setRightTextColor(Color.parseColor("#28B9FF"))
        mToolbar!!.setRightTextClickListener { saveLocation() }
        customAdd!!.setOnClickListener { LocationSendActivity.start(this, K.location.LOCATION_CUSTOM_SETTING, getSavePoiIds()) }
        mAdapter!!.setOnLocationCustomListener(object : LocationCustomModifyAdapter.OnLocationCustomListener {
            override fun customModify(position: Int, isCheck: Boolean) {
            }

            override fun customDelete(position: Int, isCheck: Boolean) {
                mSaveLocations = mAdapter!!.poiItems
                mSaveLocations!!.removeAt(position)
                if (isCheck && mSaveLocations != null && mSaveLocations!!.size >= 1) mSaveLocations!![0].isCheck = true
                notifyCustomItems(mSaveLocations)
                setAddCustomVisibility(mSaveLocations)
            }
        })
    }

    private fun notifyCustomItems(saveLocations: List<LocationSaveItem>?) {
        if (mAdapter != null) mAdapter!!.setAdapterItem(saveLocations)
    }

    private fun setAddCustomVisibility(saveLocations: List<LocationSaveItem>?) {//数量大于等于5个时，隐藏添加按钮
        if (CommonUtil.isEmptyList(saveLocations)) return
        customAddLayout.visibility = (if ((saveLocations?.size ?: 0) >= 5) View.GONE else View.VISIBLE)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventSignInAutoModify(address: EventCustomCreateAddress) {
        if (address.saveItem == null) return
        address.saveItem!!.isCheck = true
        modifyCustiom(address.saveItem)
    }

    private fun modifyCustiom(saveItem: LocationSaveItem?) {
        mSaveLocations = mAdapter!!.poiItems
        mSaveLocations?.forEach { it.isCheck=false }
        mSaveLocations?.add(0, saveItem!!)
        notifyCustomItems(mSaveLocations)
        setAddCustomVisibility(mSaveLocations)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && isHoshWork()) {
            showExitDialog()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun saveLocation() {
        if (mAdapter != null) LocationCustomSaveUtil.setSavePoiItems(mAdapter!!.poiItems)
        if (CoreZygote.getApplicationServices().activityInStacks(SignInCustomSelectedActivity::class.java)) {
            setResult(Activity.RESULT_OK)
        } else {
            val saveItems = mAdapter!!.poiItems.filter { it.isCheck }
            EventBus.getDefault().post(EventTempCustomSignAddress(if (CommonUtil.isEmptyList(saveItems)) null else saveItems[0]))
        }
        finish()
    }

    private fun isHoshWork() = !mOriginalData.containsAll(mAdapter!!.poiItems)
            || !mAdapter!!.poiItems.containsAll(mOriginalData)
}