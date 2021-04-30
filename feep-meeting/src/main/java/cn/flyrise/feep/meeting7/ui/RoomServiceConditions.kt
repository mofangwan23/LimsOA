package cn.flyrise.feep.meeting7.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.view.*
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import cn.flyrise.feep.meeting7.R
import cn.flyrise.feep.meeting7.selection.time.TimeSelectionFragment
import cn.flyrise.feep.meeting7.ui.bean.OccupyRoom
import cn.flyrise.feep.meeting7.ui.component.RoomIndicator

/**
 * @author ZYP
 * @since 2018-07-03 16:31
 * 会议室预定详情
 */
class RoomServiceConditions : DialogFragment() {

    private var ors: List<OccupyRoom>? = null

    companion object {
        fun newInstance(ors: List<OccupyRoom>): RoomServiceConditions {
            val instance = RoomServiceConditions()
            instance.ors = ors
            return instance
        }

        fun newInstance(or: OccupyRoom): RoomServiceConditions {
            val ors = ArrayList<OccupyRoom>()
            ors.add(or)
            return newInstance(ors)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog.setCancelable(false)
        val widthPixels = context!!.resources.displayMetrics.widthPixels
        val heightPixels = context!!.resources.displayMetrics.heightPixels

        val window = dialog.window
        val params = window.attributes
        params.gravity = Gravity.CENTER
        params.width = (widthPixels * 0.75f).toInt()
        params.height = (heightPixels * 0.56f).toInt()
        window.attributes = params
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val contentView = inflater.inflate(R.layout.nms_fragment_meeting_room_book_detail, container, false)
        bindView(contentView)
        return contentView
    }

    private fun bindView(v: View) {
        v.findViewById<ImageView>(R.id.nmsIvX).setOnClickListener { dismiss() }          // 关闭

        val indicator = v.findViewById<RoomIndicator>(R.id.nmsRoomIndicator)
        val viewPager = v.findViewById<ViewPager>(R.id.nmsViewPager)

        val fragments = mutableListOf<Fragment>()
        ors?.forEach { fragments.add(RoomServiceCondition.new(it)) }

        val adapter = Adapter(fragments, childFragmentManager)
        val size = ors?.size ?: 0
        viewPager.offscreenPageLimit = size
        viewPager.adapter = adapter
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                indicator.setSelection(position)
            }
        })

        if (size == 1) {
            indicator.visibility = View.INVISIBLE
        } else {
            indicator.setTotalSize(size)
            indicator.setSelection(0)
            indicator.visibility = View.VISIBLE
        }
    }

    private class Adapter(val fs: List<Fragment>, fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(p: Int) = fs?.get(p)
        override fun getCount() = fs?.size
    }

}