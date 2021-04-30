package cn.flyrise.feep.location.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.selection.*
import cn.flyrise.feep.commonality.bean.FEListItem
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.LanguageManager
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.location.adapter.SignInCalandarAdapter
import cn.flyrise.feep.location.bean.SignInCalendarData
import cn.flyrise.feep.location.contract.SignInCalendarContract
import cn.flyrise.feep.location.presenter.SignInCalendarPresenter
import cn.flyrise.feep.location.util.LocationBitmapUtil
import cn.flyrise.feep.location.util.LocationMonthPickerUtil
import cn.flyrise.feep.location.views.SignInMonthStatisActivity
import cn.flyrise.feep.location.views.SignInTrackActivity
import cn.flyrise.feep.schedule.utils.ScheduleUtil
import cn.flyrise.feep.utils.Patches
import com.haibuzou.datepicker.calendar.cons.DPMode
import kotlinx.android.synthetic.main.location_sign_caleandar_layout.*
import java.util.*

/**
 * 新建：陈冕;
 * 日期： 2018-5-5-14:20.
 * 签到月历
 */

class SignInCalendarFragment : Fragment(), SignInCalendarContract.IView, LocationMonthPickerUtil.LocationMonthPickerListener {

    private var mImgTrackIcon: ImageView? = null
    private var mLayoutShowTrack: LinearLayout? = null
    private var mAdapter: SignInCalandarAdapter? = null

    private var mDatePicker: LocationMonthPickerUtil? = null
    private var mPresenter: SignInCalendarPresenter? = null

    private var userId: String? = null
    private var mDay: String? = null
    private var currentDate: String? = null//一般为当前选中的日期
    private var isLeader: Boolean = false
    private var fragmentData: SignInCalendarData? = null

    //当前年月
    private fun getCurrentYears() = if (mTvCurrentMonth == null) "" else mDatePicker?.getCalendarToDate(mTvCurrentMonth!!.text.toString().trim())

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.location_sign_caleandar_layout, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        bindView()
        bindData()
        bindListener()
    }

    private fun setFragmentData(fragmentDatas: SignInCalendarData): SignInCalendarFragment {
        this.apply {
            fragmentData = fragmentDatas
            userId = fragmentDatas.userId
            mDay = fragmentDatas.day
            isLeader = fragmentDatas.isLeader
        }
        return this
    }

    fun setLeader(isLeaders: Boolean): SignInCalendarFragment {
        isLeader = isLeaders
        isShowTrackLayout()
        return this
    }

    private fun isShowTrackLayout() {
        if (!FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS) && isLeader && fragmentData?.isAllowSwicth ?: true) {
            mImgLeftIcon?.visibility = View.VISIBLE
        }
        mLayoutShowTrack?.visibility = if (isLeader && fragmentData?.isTrack ?: false) View.VISIBLE else View.GONE

        if (!FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS) && isLeader) //兼容7.0以前版本
            mLayoutPresonal?.setOnClickListener {
                val intent = Intent(context, ContactSelectionActivity::class.java)
                intent.putExtra(SELECTION_MODE, SELECTION_SINGLE)
                intent.putExtra(DATASOURCE, DATASOURCE_LEADER_POINT)
                intent.putExtra(IS_SHOW_SEARCH, false)
                startActivityForResult(intent, LEADER_CONTACT)
            }
    }

    fun refreshRequestSignHistory() {
        refreshRequestSignHistory("", "")
    }

    fun refreshRequestSignHistory(id: String?, date: String?) {
        if (mMonthView == null) return
        ScheduleUtil.reset()
        val mCalendar = Calendar.getInstance()
        if (!TextUtils.isEmpty(date)) mCalendar.time = mPresenter!!.textToDate(date)
        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH) + 1
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)
        val week = mCalendar.get(Calendar.WEEK_OF_MONTH)

        mMonthView.resetBG(week - 1)
        mMonthView.isUseCache = false
        mMonthView.reset(year, month, day)

        mWeekView.resetMove()
        mWeekView.isUseCache = false
        mWeekView.reset(year, month, day, mMonthView.offsetX)
        mMonthView.offsetX = 0

        mWeekView.cirApr?.clear()
        mWeekView.cirDpr?.clear()
        mMonthView.cirApr?.clear()
        mMonthView.cirDpr?.clear()
        mPresenter!!.setCurrentDay("$year.$month.$day")
        mPresenter?.requestSignHistory(userId, mDatePicker?.getCalendarToYears(mCalendar)
                , if (TextUtils.isEmpty(id)) 0 else id?.toInt() ?: 0)
    }

    fun setToolBarListener(context: Context, toolbar: FEToolbar) {
        toolbar.title = context.getString(R.string.location_month_calendar_title)
        if (FunctionManager.hasPatch(Patches.PATCH_SIGN_IN_STATICS)) {
            toolbar.rightText = context.getString(R.string.location_month_summary_title)
            toolbar.setRightTextVisbility(View.VISIBLE)
        } else {
            toolbar.rightText = ""
            toolbar.setRightIconVisbility(View.GONE)
            toolbar.setRightTextVisbility(View.GONE)
        }
        toolbar.setRightTextColor(Color.parseColor("#28B9FF"))
        toolbar.setRightTextClickListener { SignInMonthStatisActivity.start(context, getCurrentYears()!!, userId!!) }
        toolbar.setLineVisibility(View.VISIBLE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LEADER_CONTACT && resultCode == Activity.RESULT_OK && data != null) {
            if (TextUtils.isEmpty(data.getStringExtra(CONTACT_IDS))) return
            mMonthView?.resetHasTaskDayList()
            mMonthView?.updateTaskRemind()
            mAdapter?.refreshAdapter(null)
            setUserHead(data.getStringExtra(CONTACT_IDS))
            mPresenter!!.requestSignHistory(data.getStringExtra(CONTACT_IDS), "")
        }
    }

    private fun bindView() {
        mImgTrackIcon = activity!!.findViewById(R.id.location_track_icon)
        mLayoutShowTrack = activity!!.findViewById(R.id.show_track)
        mScrollView?.setWeekView(mWeekView)
        mScrollView?.setShadowView(activity!!.findViewById(R.id.shadowView))
        mImgLeftIcon?.setImageBitmap(LocationBitmapUtil.tintBitmap(context, R.drawable.icon_address_filter_down, Color.parseColor("#191919")))
        mRecyclerView?.layoutManager = LinearLayoutManager(context)
        mRecyclerView?.isNestedScrollingEnabled = false
        mAdapter = SignInCalandarAdapter(context)
        mRecyclerView?.setAdapter(mAdapter)
        mMonthView?.setFestivalDisplay(LanguageManager.isChinese())
        mWeekView?.setFestivalDisplay(LanguageManager.isChinese())
        mWeekView.setCirclePaintColor(Color.parseColor("#28B9FF"))
        mMonthView.setCirclePaintColor(Color.parseColor("#28B9FF"))
    }

    private fun bindData() {
        mImgTrackIcon!!.setImageBitmap(LocationBitmapUtil.tintBitmap(context, R.drawable.user_info_right_icon, Color.parseColor("#28B9FF")))
        mPresenter = SignInCalendarPresenter(context, this)
        val mCalendar = Calendar.getInstance()
        if (!TextUtils.isEmpty(mDay)) mCalendar.time = mPresenter!!.textToDate(mDay)
        mDatePicker = LocationMonthPickerUtil(context, mCalendar, this)

        val year = mCalendar.get(Calendar.YEAR)
        val month = mCalendar.get(Calendar.MONTH) + 1
        val day = mCalendar.get(Calendar.DAY_OF_MONTH)

        ScheduleUtil.reset()
        mMonthView?.dpMode = DPMode.SINGLE
        mMonthView?.reset(year, month, day)
        mMonthView.setMove(fragmentData?.isAllowSwicth ?: true)

        mWeekView?.dpMode = DPMode.SINGLE
        mWeekView?.reset(year, month, day, 0)
        mWeekView.setMove(fragmentData?.isAllowSwicth ?: true)

        if (TextUtils.isEmpty(userId)) userId = CoreZygote.getLoginUserServices().userId
        mPresenter!!.setCurrentDay("$year.$month.$day")
        if (fragmentData?.isExistMapNull() ?: true) {
            mPresenter!!.requestSignHistory(userId, mDatePicker!!.getCalendarToYears(mCalendar))
        } else {
            mPresenter!!.requestSignHistory(userId, mDatePicker!!.getCalendarToYears(mCalendar), fragmentData!!.selectedSumId ?: 0)
        }
        mTvCurrentMonth?.text = mDatePicker!!.getCalendarToText(mCalendar)
        setUserHead(userId!!)
        isShowTrackLayout()
        headRightIcon.visibility = if (fragmentData?.isAllowSwicth ?: true) View.VISIBLE else View.GONE
        mLayoutCurrentMonth.isEnabled = fragmentData?.isAllowSwicth ?: true
    }

    private fun setUserHead(userId: String) {
        CoreZygote.getAddressBookServices().queryUserDetail(userId)
                .subscribe({
                    if (it != null) {
                        mTvUserName!!.text = it.name
                        FEImageLoader.load(context, mImgUserIcon, CoreZygote.getLoginUserServices().serverAddress + it.imageHref
                                , it.userId, it.name)
                    } else {
                        FEImageLoader.load(context, mImgUserIcon, R.drawable.administrator_icon)
                    }
                }, { error ->
                    FEImageLoader.load(context, mImgUserIcon, R.drawable.administrator_icon)
                })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun bindListener() {
        mLayoutCurrentMonth!!.setOnClickListener { mDatePicker?.showMonPicker(getCurrentYears()) }
        mMonthView!!.setDateChangeListener { year, month ->
            if (isSwitchMonth(year.toString() + "-" + month)) return@setDateChangeListener
            mPresenter?.requestSignHistoryMonth(mDatePicker?.getDateToYears(year.toString() + "-" + month))
        }

        mMonthView!!.setDatePickedListener {
            if (TextUtils.isEmpty(it)) return@setDatePickedListener
            mScrollView!!.scrollY = 0
            currentDate = it
            mPresenter?.requestSignHistoryDay(it)
        }

        mWeekView!!.setDatePickedListener {
            if (TextUtils.isEmpty(it)) return@setDatePickedListener
            currentDate = it
            mPresenter?.requestSignHistoryDay(it)
        }

        mMonthView!!.setOnTouchListener(object : View.OnTouchListener {
            private var iLastX: Float = 0.toFloat()
            private var iLastY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                val currX = event.x
                val currY = event.y
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        iLastX = currX
                        iLastY = currY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (Math.abs(currX - iLastX) > Math.abs(currY - iLastY))
                            mScrollView!!.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP -> mScrollView!!.requestDisallowInterceptTouchEvent(false)
                }
                return false
            }
        })

        mLayoutShowTrack!!.setOnClickListener {
            SignInTrackActivity.start(context, if (TextUtils.isEmpty(currentDate)) getCurrentYears() else currentDate, userId)
        }
    }

    //判断是否需要切换年月
    private fun isSwitchMonth(years: String) = TextUtils.equals(getCurrentYears(), mDatePicker!!.getDateToYears(years))

    override fun displayList(items: List<FEListItem>?) {
        mAdapter?.refreshAdapter(items)
        mRecyclerView?.visibility = if (CommonUtil.isEmptyList(items)) View.GONE else View.VISIBLE
        mLayoutEmptyView?.visibility = if (CommonUtil.isEmptyList(items)) View.VISIBLE else View.GONE
    }

    override fun displayAgendaPromptInMonthView(promptLists: List<Int>?) {
        mMonthView?.resetHasTaskDayList()
        if (CommonUtil.isEmptyList(promptLists)) return
        mMonthView?.hasTaskDayList?.addAll(promptLists!!)
        mMonthView?.updateTaskRemind()
        mWeekView?.postInvalidate()
    }

    override fun setLocationSignSummary(count: String, interval: String) {
        mTvSignSummary?.text = String.format(resources.getString(R.string.location_calendar_summary), count, interval)
    }

    override fun setCurrentYears(years: String) {
        mTvCurrentMonth?.text = mDatePicker!!.getCalendarToText(years)
    }

    override fun dateMonthPicker(year: Int, month: Int, years: String) {
        mMonthView?.setYears(year, month)
        mWeekView?.setDate(year, month)
    }

    companion object {

        val LEADER_CONTACT = 1029

        fun getInstacne(data: SignInCalendarData): SignInCalendarFragment {
            return SignInCalendarFragment().setFragmentData(data)
        }
    }
}
