package cn.flyrise.feep.location.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.KeyEvent
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseEditableActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.location.SignInMainTabActivity
import cn.flyrise.feep.location.adapter.LocationCustomSelectedAdapter
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.event.EventTempCustomSignAddress
import cn.flyrise.feep.location.util.LocationCustomSaveUtil
import kotlinx.android.synthetic.main.location_custom_modify_layout.*
import org.greenrobot.eventbus.EventBus
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2017-11-6-15:15.
 * 自定义考勤点临时选中界面
 */

class SignInCustomSelectedActivity : BaseEditableActivity(), LocationCustomSelectedAdapter.OnClickItemListener {

    private var mToolbar: FEToolbar? = null
    private var mAdapter: LocationCustomSelectedAdapter? = null
    private var mSaveLocations: MutableList<LocationSaveItem>? = mutableListOf()

    companion object {
        fun start(context: Context, poiId: String) {
            val intent = Intent(context, SignInCustomSelectedActivity::class.java)
            intent.putExtra("selected_id", poiId)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_custom_selected_layout)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        this.mToolbar = toolbar
        toolbar.title = this.resources.getString(R.string.location_custom_selected_title)
        toolbar.rightText = this.resources.getString(R.string.location_custom_toolbar_right_but)
    }

    override fun bindData() {
        super.bindData()
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
        mSaveLocations = LocationCustomSaveUtil.getSavePoiItems()
        val poiId = intent?.getStringExtra("selected_id") ?: ""
        mAdapter = LocationCustomSelectedAdapter(mSaveLocations, getSelectedItemIndex(poiId), this)
        mRecyclerView!!.adapter = mAdapter
    }

    private fun getSelectedItemIndex(poiId: String): Int {
        return if (TextUtils.isEmpty(poiId)) mSaveLocations?.map { it.isCheck }?.indexOf(true) ?: -1
        else mSaveLocations?.map { it.poiId }?.indexOf(poiId) ?: -1
    }

    override fun bindListener() {
        super.bindListener()
        mToolbar!!.setNavigationOnClickListener { sendTempSignInAddress() }
        mToolbar!!.setRightTextColor(Color.parseColor("#28B9FF"))
        mToolbar!!.setRightTextClickListener {
            startActivityForResult(Intent(this, SignInCustomModifyActivity::class.java), 1033)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1033 && resultCode == Activity.RESULT_OK) {
            mSaveLocations = LocationCustomSaveUtil.getSavePoiItems()
            mAdapter?.setAdapterItem(mSaveLocations, getSelectedItemIndex(""))
        }
    }

    override fun onClickItem(saveItem: LocationSaveItem?) {
        sendTempSignInAddress()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) sendTempSignInAddress()
        return super.onKeyDown(keyCode, event)
    }

    private fun sendTempSignInAddress() {
        finish()
        EventBus.getDefault().post(EventTempCustomSignAddress(mAdapter?.selectedItem))
        val intent = Intent()
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity::class.java)) {
            intent.setClass(this, SignInMainTabActivity::class.java)
        }
        startActivity(intent)
    }
}