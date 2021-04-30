package cn.flyrise.feep.location.dialog

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import cn.flyrise.feep.R
import cn.flyrise.feep.location.adapter.BaseSelectedAdapter
import kotlinx.android.synthetic.main.location_selected_dialog_layout.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-24-9:56.
 * 签到选择的弹出框
 */

class SignInSelectedDialog : DialogFragment() {

    private var mListener: BaseSelectedAdapter.OnSelectedClickeItemListener? = null
    private var id: String? = null
    private var mAdapter: BaseSelectedAdapter? = null

    fun setSelectedId(id: String?): SignInSelectedDialog {
        this.id = id
        return this
    }

    fun setAdapter(adapter: BaseSelectedAdapter): SignInSelectedDialog {
        this.mAdapter = adapter
        return this
    }

    fun setListener(listener: BaseSelectedAdapter.OnSelectedClickeItemListener): SignInSelectedDialog {
        this.mListener = listener
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getDialog().getWindow()!!.requestFeature(Window.FEATURE_NO_TITLE)
        return inflater.inflate(R.layout.location_selected_dialog_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mLayout = LinearLayoutManager(getContext())
        mLayout.isAutoMeasureEnabled = true
        recyclerview.setLayoutManager(mLayout)
        mAdapter?.setListener { id, position ->
            mListener?.onSelectedClickeItem(id, position)
            dismiss()
        }
        recyclerview.setAdapter(mAdapter)
        mAdapter?.setCurrentPosition(id)
    }

    override fun onStart() {
        super.onStart()
        getDialog().apply {
            val dm = DisplayMetrics()
            getActivity()!!.getWindowManager().getDefaultDisplay().getMetrics(dm)
            getWindow()!!.setLayout((dm.widthPixels * 0.85).toInt(), (dm.heightPixels * 0.65).toInt())
        }
    }
}