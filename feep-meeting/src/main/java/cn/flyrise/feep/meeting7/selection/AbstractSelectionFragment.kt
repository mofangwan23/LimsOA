package cn.flyrise.feep.meeting7.selection

import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.meeting7.selection.bean.MSDateItem
import cn.flyrise.feep.meeting7.ui.bean.RoomInfo
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * @author ZYP
 * @since 2018-06-14 13:54
 */

interface SelectionView {
    fun loading(display: Boolean)
    fun refreshBoard(dateSource: List<MSDateItem>)
}

abstract class AbstractSelectionFragment : Fragment(), SelectableUI, SelectionView {

    protected lateinit var recyclerView: RecyclerView
    protected lateinit var adapter: AbstractSelectionAdapter
    protected var uiDelegate: SelectableUIDelegate? = null
    protected var dialog: FELoadingDialog? = null
    protected var selectedDateChangeListener: ((MSDateItem?, MSDateItem?) -> Unit)? = null
    protected var selectionInterceptListener: ((List<MSDateItem>) -> Unit)? = null

    override fun onResume() {
        super.onResume()
        displayTimeTip()
    }

    fun setOnDateChangeListener(listener: ((MSDateItem?, MSDateItem?) -> Unit)?) {
        this.selectedDateChangeListener = listener
    }

    fun setOnSelectionInterceptListener(listener: ((List<MSDateItem>) -> Unit)?) {
        this.selectionInterceptListener = listener
    }

    override fun promptDateOccupy() = FEToast.showMessage("起止时间不能涵盖已被占用的时间")

    override fun loading(display: Boolean) {
        if (display) {
            if (dialog == null) {
                dialog = FELoadingDialog.Builder(activity).setCancelable(true).create()
            }
            dialog!!.show()
        } else {
            if (dialog != null && dialog!!.isShowing()) {
                dialog!!.hide()
                dialog = null
            }
        }
    }

    protected fun displayTimeTip() {
        val finalAction = uiDelegate?.getFinalAction() ?: return
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val anchroView = recyclerView.layoutManager!!.findViewByPosition(finalAction.anchroViewPos) ?: return@subscribe
                    if (activity != null && !activity!!.isFinishing)
                        uiDelegate?.popupEndTip(anchroView, finalAction.startDate, finalAction.endDate)
                }
    }

    abstract fun getRoomInfo(): RoomInfo?

    override fun onDestroy() {
        uiDelegate!!.popupDismiss()
        super.onDestroy()
    }

}