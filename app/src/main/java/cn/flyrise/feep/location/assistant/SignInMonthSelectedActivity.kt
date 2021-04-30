package cn.flyrise.feep.location.assistant

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.location.adapter.MonthSummarySelectedAdapter
import cn.flyrise.feep.location.bean.SignInLeaderMonthItem
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.location_month_summary_selected_dialog_layout.*


/**
 * 新建：陈冕;
 * 日期： 2018-5-24-9:56.
 * 签到月汇总详情选择
 */

class SignInMonthSelectedActivity : BaseActivity() {

    private var selectedId: Int? = null
    private var toolBar: FEToolbar? = null
    private var existMap: Map<Int, String>? = null

    companion object {
        fun start(context: Activity, selectedId: Int?, existMap: Map<Int, String>?) {
            val intent = Intent(context, SignInMonthSelectedActivity::class.java)
            intent.putExtra("id", selectedId)
            intent.putExtra("map", GsonUtil.getInstance().toJson(existMap))
            context.startActivityForResult(intent, 1022)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_month_summary_selected_dialog_layout)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolBar = toolbar
    }

    override fun bindData() {
        super.bindData()
        selectedId = intent?.getIntExtra("id", 0)
        existMap = GsonUtil.getInstance().fromJson(intent.getStringExtra("map"), object : TypeToken<Map<Int, String>>() {}.type)
        if (existMap == null) {
            finish()
            return
        }
        toolBar?.getToolbarTitle()?.apply {
            val nav_up = resources.getDrawable(R.drawable.icon_arrow_down)
            nav_up.setBounds(0, 0, nav_up.minimumWidth, nav_up.minimumHeight)
            setCompoundDrawables(null, null, nav_up, null)
        }
        toolBar?.setTitle(existMap!!.get(selectedId!!))
        val mLayout = LinearLayoutManager(this)
        mLayout.isAutoMeasureEnabled = true

        val mAdapter = MonthSummarySelectedAdapter(this, existMap?.map {
            SignInLeaderMonthItem().apply {
                sumId = it.key
                sumTitle = it.value
            }
        })

        mAdapter.apply {
            setListener { id, position ->
                val intent = Intent()
                intent.putExtra("sumId", id.toInt())
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            setCurrentPosition(selectedId.toString())
        }

        recyclerview.apply {
            setLayoutManager(mLayout)
            setAdapter(mAdapter)
        }
        layout.setOnClickListener { finish() }
        toolBar?.setOnClickListener { finish() }
    }
}