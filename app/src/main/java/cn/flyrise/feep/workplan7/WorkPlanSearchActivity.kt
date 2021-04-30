package cn.flyrise.feep.workplan7

import android.os.Bundle
import android.text.TextUtils
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.core.base.component.FESearchListActivity
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.notification.NotificationController
import cn.flyrise.feep.particular.ParticularActivity
import cn.flyrise.feep.particular.ParticularIntent
import cn.flyrise.feep.particular.presenter.ParticularPresenter
import cn.flyrise.feep.utils.Patches
import cn.flyrise.feep.workplan7.adapter.WorkPlanSearchAdapter
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean
import cn.flyrise.feep.workplan7.presenter.WorkPlanSearchListPresenter
import cn.squirtlez.frouter.FRouter
import cn.squirtlez.frouter.annotations.Route
import com.dk.view.badge.BadgeUtil

/**
 * author : klc
 * data on 2018/5/14 16:06
 * Msg : 计划搜索
 */
@Route("/plan/search")
class WorkPlanSearchActivity : FESearchListActivity<WorkPlanListItemBean>() {

    private var mAdapter: WorkPlanSearchAdapter? = null
    private var mPresenter: WorkPlanSearchListPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun bindData() {
        et_Search.hint = "搜索计划..."
        mAdapter = WorkPlanSearchAdapter(this)
        setAdapter(mAdapter)
        mPresenter = WorkPlanSearchListPresenter(this)
        setPresenter(mPresenter)

        searchKey = intent.getStringExtra("keyword")
        if (!TextUtils.isEmpty(searchKey)) {
            et_Search.setText(searchKey)
            et_Search.setSelection(searchKey.length)
            myHandler.post(searchRunnable)
        } else {
            myHandler.postDelayed({ DevicesUtil.showKeyboard(et_Search) }, 500);
        }
    }

    override fun bindListener() {
        super.bindListener()
        mAdapter?.setOnItemClickListener { _, item ->
            val clickItem: WorkPlanListItemBean = item as WorkPlanListItemBean
            if (clickItem.isNews) {
                NotificationController.messageReaded(this, clickItem.id)
                val feApplication = this.applicationContext as FEApplication
                val num = feApplication.cornerNum - 1
                BadgeUtil.setBadgeCount(this, num)//角标
                feApplication.cornerNum = num
            }
//            ParticularIntent.Builder(this)
//                    .setParticularType(ParticularPresenter.PARTICULAR_WORK_PLAN)
//                    .setTargetClass(ParticularActivity::class.java)
//                    .setBusinessId(clickItem.id)
//                    .create()
//                    .start()

            if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
                FRouter.build(this, "/plan/detail")
                        .withString("EXTRA_BUSINESSID", clickItem.id)
                        .go()
            } else {
                FRouter.build(this, "/particular/detail")
                        .withInt("extra_particular_type", 5)
                        .withString("extra_business_id", clickItem.id)
                        .go()
            }
        }
    }

    override fun searchData(searchKey: String?) {
        mPresenter?.searchData(searchKey)
    }


}