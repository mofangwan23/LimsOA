package cn.flyrise.feep.location.views

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.KeyEvent
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.location.SignInMainTabActivity
import cn.flyrise.feep.location.bean.LocationSaveItem
import cn.flyrise.feep.location.fragment.SignInMainSearchFragment
import kotlinx.android.synthetic.main.location_main_search_layout.*

/**
 * 新建：陈冕;
 *日期： 2018-8-3-17:24.
 * 搜索后签到的主界面
 */
class SignInMainSearchActivity : BaseActivity() {

    private var mainFragment: SignInMainSearchFragment? = null
    private var selectedItem: LocationSaveItem? = null
    private var title: String? = null

    companion object {
        fun start(context: Activity, item: LocationSaveItem, requestCode: Int) {
            val intent = Intent(context, SignInMainSearchActivity::class.java)
            intent.putExtra("address_data", GsonUtil.getInstance().toJson(item))
            context.startActivityForResult(intent, requestCode)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_main_search_layout)
    }

    override fun bindData() {
        super.bindData()
        selectedItem = GsonUtil.getInstance().fromJson(intent?.getStringExtra("address_data"), LocationSaveItem::class.java)
        if (selectedItem == null) return
        title = selectedItem?.title
        etSearch.setText(title)
        val ft = supportFragmentManager.beginTransaction()
        mainFragment = SignInMainSearchFragment()
        mainFragment?.setLeaderListner {}
        mainFragment?.setLocationSaveItem(selectedItem!!)
        ft.add(fragmeLayout.id, mainFragment!!)
        ft.commitAllowingStateLoss()
        val navigationB = resources.getDrawable(cn.flyrise.feep.core.R.mipmap.core_icon_back)
        navigationB.setColorFilter(Color.parseColor("#484848"), PorterDuff.Mode.SRC_ATOP)
        navigation.setImageDrawable(navigationB)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mainFragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun bindListener() {
        super.bindListener()
        btnSearchCancle.setOnClickListener {
            if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity::class.java)) {
                val intent = Intent(this, SignInMainTabActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            } else {
                finish()
            }
        }
        etSearch.setOnClickListener {
            clickeBack(title)
        }
        navigation.setOnClickListener {
            clickeBack("")
        }
    }

    private fun clickeBack(text: String?) {
        val intent = Intent()
        intent.putExtra("title", text)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clickeBack("")
        }
        return super.onKeyDown(keyCode, event)
    }
}