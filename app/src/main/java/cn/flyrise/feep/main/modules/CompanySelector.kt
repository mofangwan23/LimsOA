package cn.flyrise.feep.main.modules

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.TextUtils
import android.view.*
import android.widget.ListView
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.CompanyAdapter
import cn.flyrise.feep.addressbook.model.Department

/**
 * @author 社会主义接班人
 * @since 2018-07-27 14:28
 */
class CompanySelector : DialogFragment() {

    private var company: Department? = null
    private var companies: List<Department>? = null
    private var companySelectListener: ((Department) -> Unit)? = null
    private var marginTop: Int = 0

    public fun setMarginTop(marginTop: Int) {
        this.marginTop = marginTop
    }

    fun setCompany(c: Department) {
        this.company = c
    }

    fun setCompanies(cs: List<Department>) {
        this.companies = cs
    }

    fun setOnCompanySelectListener(onCompanySelect: ((Department) -> Unit)? = null) {
        this.companySelectListener = onCompanySelect
    }

    override fun onStart() {
        super.onStart()
        val attributes = dialog.window!!.attributes
        attributes.gravity = Gravity.TOP
        attributes.width = resources.displayMetrics.widthPixels
        attributes.height = resources.displayMetrics.heightPixels - marginTop
        attributes.dimAmount = 0.0f
        attributes.y = marginTop
        dialog.window!!.attributes = attributes
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val view = inflater.inflate(R.layout.fragment_company_selector, container, false)
        val cAdapter = CompanyAdapter().apply {
            setDefault(company)
            setData(companies)
        }

        (view.findViewById(R.id.listView) as ListView).apply {
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

        view.findViewById<View>(R.id.viewTransparent).setOnClickListener { dismiss() }
        return view
    }

}