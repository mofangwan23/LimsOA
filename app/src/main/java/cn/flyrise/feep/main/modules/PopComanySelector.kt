package cn.flyrise.feep.main.modules

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.CompanyAdapter
import cn.flyrise.feep.addressbook.model.Department

class PopComanySelector(val context: Context) : PopupWindow(context) {

    private var company: Department? = null
    private var companies: List<Department>? = null
    private var companySelectListener: ((Department) -> Unit)? = null

    fun setCompany(c: Department) {
        this.company = c
    }

    fun setCompanies(cs: List<Department>) {
        this.companies = cs
    }

    fun setOnCompanySelectListener(onCompanySelect: ((Department) -> Unit)? = null) {
        this.companySelectListener = onCompanySelect
    }

    fun show(anchorView: View){
        val contentView = LayoutInflater.from(context).inflate(R.layout.fragment_company_selector, null, false)
        this.width = ViewGroup.LayoutParams.MATCH_PARENT
        this.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this.isOutsideTouchable = true
        this.isFocusable = true
        this.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        contentView.measure(makeDropDownMeasureSpec(this.width), makeDropDownMeasureSpec(this.height))
        this.contentView = contentView

        val cAdapter = CompanyAdapter().apply {
            setDefault(company)
            setData(companies)
        }

        (contentView.findViewById(cn.flyrise.feep.R.id.listView) as ListView).apply {
            adapter = cAdapter
            setOnItemClickListener { _, _, position, _ ->
                val selectedCompany = cAdapter?.getItem(position) as Department
                if (TextUtils.equals(selectedCompany.deptId, company?.deptId)) {
                    dismiss()
                    return@setOnItemClickListener
                }

                companySelectListener?.invoke(selectedCompany)
                dismiss()
            }
        }

        contentView.findViewById<View>(cn.flyrise.feep.R.id.viewTransparent).setOnClickListener { dismiss() }

        showAsDropDown(anchorView, 0, 0)
    }

    private fun makeDropDownMeasureSpec(measureSpec: Int): Int {
        val mode = if (measureSpec == ViewGroup.LayoutParams.WRAP_CONTENT) {
            View.MeasureSpec.UNSPECIFIED
        } else {
            View.MeasureSpec.EXACTLY
        }
        return View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(measureSpec), mode)
    }

    override fun showAsDropDown(anchor: View?, xoff: Int, yoff: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            var visibleFrame = Rect()
            anchor!!.getGlobalVisibleRect(visibleFrame)
            var height = anchor.getResources().getDisplayMetrics().heightPixels - visibleFrame.bottom
            setHeight(height)
        }
        super.showAsDropDown(anchor, xoff, yoff)

    }

}