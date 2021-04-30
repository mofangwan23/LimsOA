package cn.flyrise.feep.location.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.location.assistant.SignInMonthSelectedActivity
import cn.flyrise.feep.location.bean.SignInCalendarData
import cn.flyrise.feep.location.fragment.SignInCalendarFragment
import cn.flyrise.feep.utils.Patches
import kotlinx.android.synthetic.main.sign_in_main_calendar_layout.*


/**
 * 新建：陈冕;
 *日期： 2018-8-6-17:08.
 *月考勤详情
 * 101;//出勤
 * 103;//迟到
 * 104;//早退
 * 105;//缺卡
 * 107;//外勤
 * 这些能够查看考勤月历
 */
class SignInCalendarActivity : BaseActivity() {

    companion object {

        fun start(context: Context, data: SignInCalendarData) {
            val intent = Intent(context, SignInCalendarActivity::class.java)
            intent.putExtra("data", GsonUtil.getInstance().toJson(data))
            context.startActivity(intent)
        }
    }

    private var mToolbar: FEToolbar? = null
    private var selectedId: Int? = null
    private var fragment: SignInCalendarFragment? = null
    private var data: SignInCalendarData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_in_main_calendar_layout)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        mToolbar = toolbar
        mToolbar?.showNavigationIcon()
    }

    override fun bindData() {
        super.bindData()
        data = GsonUtil.getInstance().fromJson(intent?.getStringExtra("data"), SignInCalendarData::class.java)
        if (data == null) {
            finish()
            return
        }
        fragment = SignInCalendarFragment.getInstacne(data!!)
        fragment?.setLeader(getLeader())
        supportFragmentManager.beginTransaction().apply {
            add(fragmentLayout.id, fragment!!)
            commitAllowingStateLoss()
        }
        initToolbar(data!!)
    }

    private fun getLeader() = FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS)
            && SpUtil.get(PreferencesUtils.HAS_SUB_SUBORDINATES, false)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1022 && resultCode == Activity.RESULT_OK) {
            setSumId(data!!.getIntExtra("sumId", 0))
        }
    }

    private fun initToolbar(data: SignInCalendarData) {
        mToolbar?.getToolbarTitle()?.apply {
            if (!data.isExistMapNull()) {
                val nav_up = resources.getDrawable(R.drawable.icon_arrow_down)
                nav_up.setBounds(0, 0, nav_up.minimumWidth, nav_up.minimumHeight)
                setCompoundDrawables(null, null, nav_up, null)
                setOnClickListener {
                    openSelectedView()
                }
            }
        }
        selectedId = data.selectedSumId ?: 0
        mToolbar?.setTitle(if (data.existMap?.isEmpty() ?: true) getString(R.string.location_month_calendar_title)
        else data.existMap!!.get(selectedId!!))
    }

    private fun openSelectedView() {
        SignInMonthSelectedActivity.start(this, selectedId, data!!.existMap)
    }

    private fun setSumId(id: Int?) {
        selectedId = id
        fragment?.refreshRequestSignHistory(id.toString(), data?.day)
        mToolbar?.getToolbarTitle()?.text = data?.existMap?.get(id)
    }
}