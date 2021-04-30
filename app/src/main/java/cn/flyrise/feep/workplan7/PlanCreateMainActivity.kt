package cn.flyrise.feep.workplan7

import android.app.Activity
import android.content.Intent
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.utils.Patches
import cn.squirtlez.frouter.annotations.RequestExtras
import cn.squirtlez.frouter.annotations.Route

/**
 * 计划新建首页，根据补丁列表具体跳转
 */
@Route("/plan/create")
@RequestExtras("userIds")
class PlanCreateMainActivity : BaseActivity() {

    companion object {
        fun start(activity: Activity?) {
            activity!!.startActivity(Intent(activity, PlanCreateMainActivity::class.java))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
            Plan7MainActivity.start(this, Plan7MainActivity.NEW, intent?.getStringArrayListExtra("userIds"))
        } else {
            startActivity(Intent(this, Plan6CreateActivity::class.java))
        }
        finish()
    }

}