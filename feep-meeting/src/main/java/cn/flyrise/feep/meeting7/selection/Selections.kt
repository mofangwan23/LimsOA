package cn.flyrise.feep.meeting7.selection

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import android.widget.TextView
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.time.TimeSelectionDialog
import cn.flyrise.feep.meeting7.selection.time.TimeSelectionFragment
import cn.flyrise.feep.meeting7.selection.time.unableTextColor

/**
 * @author ZYP
 * @since 2018-06-14 14:20
 */

class Selections : DialogFragment() {

    private var selectionFragment: AbstractSelectionFragment? = null
    private var tvConfirm: TextView? = null
    private var startDate: MSDateItem? = null
    private var endDate: MSDateItem? = null
    private var dateConfirmListener: ((MSDateItem?, MSDateItem?) -> Unit)? = null

    companion object {
        fun newInstance(selectionFragment: AbstractSelectionFragment): Selections {
            val dateSelectionDialog = Selections()
            dateSelectionDialog.selectionFragment = selectionFragment
            return dateSelectionDialog
        }
    }

    fun setOnDateConfirmListener(listener: ((MSDateItem?, MSDateItem?) -> Unit)?): Selections {
        this.dateConfirmListener = listener
        return this
    }

    override fun onStart() {
        super.onStart()
        val heightPixels = context!!.resources.displayMetrics.heightPixels

        val window = dialog.window
        val params = window.attributes
        params.gravity = Gravity.BOTTOM
        params.width = WindowManager.LayoutParams.MATCH_PARENT
        if (selectionFragment is TimeSelectionFragment) {
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
        } else {
            params.height = heightPixels * 2 / 3
        }
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setWindowAnimations(R.style.NMSSelectionDialogAnimation)
        val view = inflater?.inflate(R.layout.nms_fragment_dialog_selection, container, false)

        if (selectionFragment == null) {
            throw NullPointerException("Please init the SelectionFragment at first.")
        }

        childFragmentManager.beginTransaction()
                .add(R.id.nmsLayoutSelectionFragment, selectionFragment!!)
                .commit()
        bindView(view!!)
        return view
    }

    private fun bindView(view: View) {
        this.startDate = selectionFragment?.getRoomInfo()?.startDate()
        this.endDate = selectionFragment?.getRoomInfo()?.endDate()
        this.selectionFragment?.apply {
            setOnDateChangeListener { s, e ->
                startDate = s
                endDate = e
                updateConfirmTextStatus()
            }

            setOnSelectionInterceptListener {
                TimeSelectionDialog.newInstance(it) { s, e ->
                    startDate = s
                    endDate = e
                    dateConfirmListener?.invoke(startDate, endDate)
                }.show(childFragmentManager, "")
            }
        }

        view.findViewById<View>(R.id.nmsTvCancel).setOnClickListener { dismiss() }

        tvConfirm = view.findViewById(R.id.nmsTvConfirm)
        tvConfirm?.setOnClickListener {
            dateConfirmListener?.invoke(startDate, endDate)
            dismiss()
        }
        updateConfirmTextStatus()
    }

    private fun updateConfirmTextStatus() {
        if (this.startDate == null || this.endDate == null) {
            tvConfirm?.isClickable = false
            tvConfirm?.setTextColor(unableTextColor)
            return
        }

        tvConfirm?.isClickable = true
        tvConfirm?.setTextColor(Color.parseColor("#28B9FF"))
    }
}