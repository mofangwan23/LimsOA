package cn.flyrise.feep.workplan7

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.utils.ContactsIntent
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.DataKeeper
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.dialog.FEMaterialDialog
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.workplan7.adapter.PlanUserLayoutAdapter
import cn.flyrise.feep.workplan7.model.PlanFilterContent
import cn.flyrise.feep.workplan7.view.PlanItemLayout
import com.borax12.materialdaterangepicker.DateTimePickerDialog
import kotlinx.android.synthetic.main.plan_activity_filter.*
import java.util.*

class PlanFilterActivity : BaseActivity() {

    private lateinit var types: Array<String>
    private val SELECT_PERSON_CODE = 102
    lateinit var filterContent: PlanFilterContent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plan_activity_filter)
    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        toolbar.title = getString(R.string.plan_filter_title)
    }

    override fun bindView() {
        super.bindView()
        userListView.layoutManager = GridLayoutManager(this, 6)
    }

    override fun bindData() {
        super.bindData()
        types = resources.getStringArray(R.array.plan_types)
        val filterContent = intent.getParcelableExtra<PlanFilterContent>("filter")
        if (filterContent == null) {
            this.filterContent = PlanFilterContent()
        } else {
            this.filterContent = filterContent
            showUserLayout(toPersonList(this.filterContent.userIDs))
            if (filterContent.startTime != null) {
                lyStatTime.setContent(DateUtil.formatTime(filterContent.startTime!!, "yyyy年MM月dd日"))
            }
            if (filterContent.endTime != null) {
                lyEndTime.setContent(DateUtil.formatTime(filterContent.endTime!!, "yyyy年MM月dd日"))
            }
            if (filterContent.type != null) lyType.setContent(types[(filterContent.type ?: 1) - 1])
        }
    }

    override fun bindListener() {
        super.bindListener()
        lyUserTitle.setOnClickListener {
            //            val intent = Intent(this, ContactSelectionActivity::class.java)
//            intent.putExtra(TITLE, getString(R.string.plan_filter_users))
//            intent.putExtra(cn.flyrise.feep.addressbook.selection.DATASOURCE, cn.flyrise.feep.addressbook.selection.DATASOURCE_WORK_PLAN_RELATED)
//            intent.putExtra(cn.flyrise.feep.addressbook.selection.SELECTION_MODE, cn.flyrise.feep.addressbook.selection.SELECTION_MULTI)
//            if (!filterContent.userIDs.isNullOrEmpty()) {
//                intent.putStringArrayListExtra(cn.flyrise.feep.addressbook.selection.CONTACT_DEFAULT, ArrayList(filterContent.userIDs!!.split(",")))
//            }
//            startActivityForResult(intent, SELECT_PERSON_CODE)

            if (!filterContent.userIDs.isNullOrEmpty()) {
                DataKeeper.getInstance().keepDatas(SELECT_PERSON_CODE, toPersonList(filterContent.userIDs))
            }
            ContactsIntent(this@PlanFilterActivity)
                    .targetHashCode(SELECT_PERSON_CODE)
                    .requestCode(SELECT_PERSON_CODE)
                    .userCompanyOnly()
                    .title(getString(R.string.plan_filter_users))
                    .withSelect()
                    .open()
        }
        lyType.setOnClickListener {
            FEMaterialDialog.Builder(this).setWithoutTitle(true).setItems(types) { dialog, _, position ->
                lyType.setContent(types[position])
                filterContent.type = position + 1
                dialog.dismiss()
            }.build().show()
        }
        lyStatTime.setOnClickListener {
            openDateTimeDialog(lyStatTime, filterContent.startTime)
        }
        lyEndTime.setOnClickListener {
            openDateTimeDialog(lyEndTime, filterContent.endTime)
        }
        btReset.setOnClickListener {
            filterContent = PlanFilterContent()
            lyType.setContent("")
            lyStatTime.setContent("")
            lyEndTime.setContent("")
            lyUserTitle.setTitle(getString(R.string.plan_filter_users))
            userListView.adapter = PlanUserLayoutAdapter(this, null)
        }
        btConfirm.setOnClickListener {
            if (filterContent.startTime != null && filterContent.endTime == null) {
                FEToast.showMessage(getString(R.string.plan_create_end_time_none))
                return@setOnClickListener
            }

            if (filterContent.startTime == null && filterContent.endTime != null) {
                FEToast.showMessage(getString(R.string.plan_create_start_time_none))
                return@setOnClickListener
            }

            if (filterContent.startTime ?: 0 > filterContent.endTime ?: 0) {
                FEToast.showMessage(getString(R.string.plan_filter_end_no_min_start))
                return@setOnClickListener
            }
            val intent = Intent()
            intent.putExtra("filter", filterContent)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun openDateTimeDialog(lyTime: PlanItemLayout, time: Long?) {
        DevicesUtil.tryCloseKeyboard(this)
        val dateTimePickerDialog = DateTimePickerDialog()
        val calendar = Calendar.getInstance()
        if (time != null) {
            calendar.time = Date(time)
        } else if (lyTime == lyStatTime) {
            calendar.add(Calendar.MONTH, -1) //开始时间默认为一个月前
        }
//        dateTimePickerDialog.setMaxCalendar(Calendar.getInstance())//刷选日期不能超过当天
        dateTimePickerDialog.setDateTime(calendar)
        dateTimePickerDialog.setButtonCallBack(object : DateTimePickerDialog.ButtonCallBack {
            override fun onClearClick() {}

            override fun onOkClick(result: Calendar, dialog: DateTimePickerDialog) {
                if (lyTime == lyStatTime) {
                    filterContent.startTime = result.timeInMillis
                } else {
                    filterContent.endTime = result.timeInMillis
                }
                lyTime.setContent(DateUtil.formatTime(result.timeInMillis, "yyyy年MM月dd日"))
                dateTimePickerDialog.dismiss()
            }
        })
        dateTimePickerDialog.setTimeLevel(3)
        dateTimePickerDialog.show(fragmentManager, "dateTimePickerDialog")
    }

    private fun toPersonList(ids: String?): List<AddressBook>? {
        if (ids.isNullOrEmpty()) return null
        val idList = ids!!.split(",")
        return CoreZygote.getAddressBookServices().queryUserIds(idList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PERSON_CODE && DataKeeper.getInstance().getKeepDatas(SELECT_PERSON_CODE) != null) {
            val userIDs = DataKeeper.getInstance().getKeepDatas(SELECT_PERSON_CODE) as List<AddressBook>
            showUserLayout(userIDs)
            val sb = StringBuilder()
            userIDs.forEachIndexed { index, addressBook ->
                sb.append(addressBook.userId)
                if (userIDs.size - 1 != index) {
                    sb.append(",")
                }
            }
            filterContent.userIDs = sb.toString()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        DataKeeper.getInstance().removeKeepData(SELECT_PERSON_CODE)
    }


    private fun showUserLayout(userIDs: List<AddressBook>?) {
        userListView.adapter = PlanUserLayoutAdapter(this, userIDs)
        if (userIDs.isNullOrEmpty()) {
            lyUserTitle.setTitle(getString(R.string.plan_filter_users))
        } else {
            lyUserTitle.setTitle("${getString(R.string.plan_filter_users)}（${userIDs!!.size}）")
        }
    }
}