package cn.flyrise.feep.workplan7.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.*
import android.widget.LinearLayout
import cn.flyrise.feep.R
import android.view.WindowManager
import android.view.Gravity
import cn.flyrise.feep.core.common.utils.DevicesUtil
import kotlinx.android.synthetic.main.plan_dialog_wheelview.*


class BottomWheelSelectionDialog : DialogFragment() {

	private val wheelViews = ArrayList<WheelSelectionView>()
	private val wheelViewValue = ArrayList<List<String>>()
	private val wheelViewDefaultSelection = ArrayList<Int>()

	var title: String? = null
	var onSelectionListener: ((wheelViews: List<WheelSelectionView>, scrollView: WheelSelectionView) -> Unit?)? = null
	var onClickListener: ((result: List<String>) -> Unit?)? = null
	private var itemWeight: Int? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
		return inflater.inflate(R.layout.plan_dialog_wheelview, null)
	}

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		itemWeight = DevicesUtil.getScreenWidth() / wheelViewValue.size
		bindView()
		bindListener()
	}

	override fun onStart() {
		super.onStart()
		val heightPixels = context!!.resources.displayMetrics.heightPixels
		val window = dialog.window
		val params = window.attributes
		params.gravity = Gravity.BOTTOM
		params.width = WindowManager.LayoutParams.MATCH_PARENT
		params.height = heightPixels * 2 / 5
		window.attributes = params
		window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
	}


	private fun bindView() {
		tvTitle.text = title
		for (i in wheelViewValue.indices) {
			val wheelView = getWheelView(context!!, wheelViewValue[i], wheelViewDefaultSelection[i])
			wheelViews.add(wheelView)
			lyContent.addView(wheelView)
		}

	}

	private fun bindListener() {
		tvNegative.setOnClickListener { dismiss() }
		tvPositive.setOnClickListener {
			if (wheelViews.size != 0) {
				val result = ArrayList<String>()
				for (view in wheelViews) {
					result.add(view.seletedItem)
				}
				onClickListener?.invoke(result)
			}
			dismiss()
		}
	}

	private fun getWheelView(context: Context, value: List<String>, defaultSelection: Int): WheelSelectionView {
		val wheelView = WheelSelectionView(context)
		val params = LinearLayout.LayoutParams(itemWeight!!, LinearLayout.LayoutParams.WRAP_CONTENT)
		params.gravity = Gravity.CENTER
		params.weight = 1f
		wheelView.layoutParams = params
		wheelView.offset = 2
		wheelView.setItems(value)
		if (defaultSelection != -1) {
			wheelView.setSeletion(defaultSelection)
		}
		wheelView.setOnWheelViewListener { _, _ -> onSelectionListener?.invoke(wheelViews, wheelView) }
		return wheelView
	}

	fun addValue(value: List<String>, defaultPosition: Int) {
		wheelViewValue.add(value)
		wheelViewDefaultSelection.add(defaultPosition)
	}

	interface OnClickListener {
		fun onClick(result: List<String>) //选中项的结果
	}

}