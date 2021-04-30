package cn.flyrise.feep.workplan7

import android.content.Intent
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.utils.Patches

/**
 * 计划首页，根据补丁列表具体跳转
 */
class PlanMainActivity : BaseActivity() {

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
			startActivity(Intent(this, Plan7MainActivity::class.java))
		}
		else {
			startActivity(Intent(this, Plan6MainActivity::class.java))
		}
		finish()
	}

}