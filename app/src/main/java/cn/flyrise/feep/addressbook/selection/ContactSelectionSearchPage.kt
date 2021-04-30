package cn.flyrise.feep.addressbook.selection

import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.ContactSearchAdapter
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.core.watermark.WMAddressDecoration
import cn.flyrise.feep.core.watermark.WMStamp
import kotlinx.android.synthetic.main.fragment_contact_search.*


/**
 * @author ZYP
 * @since 2018-06-07 13:48
 */
class ContactSelectionSearchPage : Fragment(), ContactSelectionView {

    lateinit var presenter: SelectionPresenter
    private lateinit var adapter: ContactSearchAdapter

    private val handler = Handler()
    private val textChangeRunnable = { presenter.search(etSearch.text.toString()) }

    override fun onResume() {
        super.onResume()
        presenter.selectionView = ContactSelectionView@ this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindView()
        handler.postDelayed({
            etSearch.isFocusable = true
            etSearch.isFocusableInTouchMode = true
            etSearch.requestFocus()
            DevicesUtil.showKeyboard(etSearch)
        }, 500)
    }

    fun bindView() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                ivClearInput.visibility = if (s?.length!! > 0) View.VISIBLE else View.GONE
                if (s?.length == 0) {
                    adapter.clearContacts()
                    return
                }

                handler.postDelayed(textChangeRunnable, 300)
            }
        })

        ivClearInput.setOnClickListener {
            etSearch.setText("")
            adapter.clearContacts()
            ivClearInput.visibility = View.GONE
        }

        tvSearchCancel.setOnClickListener {
            (activity as ContactSelectionActivity).onBackPressed()
            clearSearchResult()
        }

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.itemAnimator = DefaultItemAnimator()

        val watermark = WMStamp.getInstance().waterMarkText
        recyclerView.addItemDecoration(WMAddressDecoration(watermark))

        adapter = ContactSearchAdapter(activity)
        adapter.setEmptyView(ivEmptyView)
        adapter.setOnContactItemClickListener { addressBook, position ->
            (activity as ContactSelectionActivity).confirmSearchResult(addressBook)
            clearSearchResult()
        }

        recyclerView.adapter = adapter
    }

    override fun showContacts(addressBooks: List<AddressBook?>?, deptUser: List<AddressBook?>?) {
        adapter.contacts = addressBooks
    }

    override fun showLoading() {}

    override fun hideLoading() {}

    private fun clearSearchResult() {
        etSearch.setText("")
        ivClearInput.visibility = View.GONE
        adapter.clearContacts()
        DevicesUtil.hideKeyboard(etSearch)
    }

}