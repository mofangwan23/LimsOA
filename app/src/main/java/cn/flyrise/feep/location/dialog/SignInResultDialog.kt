package cn.flyrise.feep.location.dialog

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import cn.flyrise.feep.FEMainActivity
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.common.utils.GsonUtil
import cn.flyrise.feep.location.Sign
import cn.flyrise.feep.location.SignInMainTabActivity
import cn.flyrise.feep.location.assistant.SignInRapidlyActivity
import cn.flyrise.feep.location.bean.LocationPhotoItem
import cn.flyrise.feep.location.contract.LocationReportSignContract
import cn.flyrise.feep.location.contract.TakePhotoSignContract
import cn.flyrise.feep.location.event.EventMainTabShowFragment
import cn.flyrise.feep.location.util.LocationSignTimeUtil
import cn.flyrise.feep.location.views.LocationSendActivity
import cn.flyrise.feep.location.views.OnSiteSignActivity
import kotlinx.android.synthetic.main.location_sign_success_dialog_fragment.*
import org.greenrobot.eventbus.EventBus


/**
 * 新建：陈冕;
 * 日期： 2017-9-1-17:42.
 * 签到状态
 */

class SignInResultDialog : DialogFragment() {


    private val proportion: Float = 280 / 350f//宽高比例
    private var isLeaders = false
    private var mContext: Context? = null
    private var mListener: ((Boolean) -> Unit?)? = null

    private var isResultSuccess = true//是否为返回成功的提示
    private var errorText: String? = null
    private var errorType: Int? = null
    private var successText: String? = null
    private var time: String? = null
    private var isRestartSignIn = false//是否重新签到

    fun setContext(context: Context): SignInResultDialog {
        this.mContext = context
        return this
    }

    fun setListener(listener: (Boolean) -> Unit?): SignInResultDialog {
        this.mListener = listener
        return this
    }

    fun setTime(time: String): SignInResultDialog {
        this.time = time
        return this
    }

    fun setLeader(isLeader: Boolean): SignInResultDialog {
        this.isLeaders = isLeader
        return this
    }

    fun setSuccessText(successText: String): SignInResultDialog {
        this.successText = successText
        return this
    }

    fun setError(text: String): SignInResultDialog {
        this.errorText = text
        this.isResultSuccess = false
        return this
    }

    fun setErrorType(errorType: Int?): SignInResultDialog {
        this.errorType = errorType
        return this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.getWindow().setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.location_sign_success_dialog_fragment, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(dm)
        val width = dm.widthPixels * 0.75f
        dialog.window!!.setLayout(width.toInt(), (width / proportion).toInt())
        setBackground(width)
    }

    private fun setBackground(width: Float) {
        val bitmap = BitmapFactory.decodeResource(resources, if (isResultSuccess) LocationSignTimeUtil.getSignTimeDrawable(time)
        else R.drawable.location_sign_error)
        val para = mImgIcon.getLayoutParams()
        para.height = (width * bitmap.height / bitmap.width).toInt()
        mImgIcon.setLayoutParams(para)
        mImgIcon!!.setImageBitmap(bitmap)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (isResultSuccess) {
            tvTime.text = time
            tvTitle.text = getString(R.string.location_sign_in_success_title)
            tvTime.visibility = View.VISIBLE
            v_line.visibility = View.VISIBLE
        } else {
            tvTitle.text = getString(R.string.location_sign_in_error_title)
            tvTime.visibility = View.GONE
            v_line.visibility = View.GONE
        }

        successHint.apply {
            if (isResultSuccess) {
                val nav_up = resources.getDrawable(R.drawable.location_leader_field_person_icon)
                nav_up.setBounds(0, 0, nav_up.minimumWidth, nav_up.minimumHeight)
                setCompoundDrawables(nav_up, null, null, null)
                text = successText
                maxLines = 1
            } else {
                text = when (errorType) {
                    Sign.error.noCustom -> getString(R.string.location_error_no_custom)
                    Sign.error.superRange -> getString(R.string.location_error_super_range)
                    Sign.error.superRangeNoPlace -> getString(R.string.location_error_super_range_no_place)
                    Sign.error.noTime -> getString(R.string.location_error_no_time)
                    Sign.error.workSuperRange -> getString(R.string.location_error_work_super_range)
                    Sign.error.network -> getString(R.string.location_error_net_work)
                    Sign.error.signMany -> getString(R.string.location_dialog_hint_sign_many)
                    else -> errorText

                }
                maxLines = Int.MAX_VALUE
            }
        }

        leftBut.apply {
            when (errorType) {
                Sign.error.noCustom -> text = getString(R.string.location_dialog_but_nearby)
                Sign.error.superRange -> text = getString(R.string.location_dialog_but_nearby)
                Sign.error.superRangeNoPlace -> text = getString(R.string.location_dialog_but_setting)
                Sign.error.noTime -> visibility = View.GONE
//                Sign.error.workSuperRange -> text = getString(R.string.location_dialog_but_setting)
                Sign.error.workSuperRange -> text = getString(R.string.location_dialog_but_nearby)
                Sign.error.network -> visibility = View.GONE
                Sign.error.signMany -> text = getString(R.string.location_dialog_but_nearby)
                else -> text = getString(R.string.location_dialog_but_calendar)
            }
            setOnClickListener {
                dismiss()
                when (errorType) {
                    Sign.error.noCustom -> openNearby()
                    Sign.error.superRange -> openNearby()
                    Sign.error.superRangeNoPlace -> openNearby()
//                    Sign.error.workSuperRange -> openErrorPhoto()
                    Sign.error.workSuperRange -> openNearby()
                    Sign.error.signMany -> openNearby()
                    else -> openCalendar()
                }
            }
        }

        rightBut.apply {
            when (errorType) {
                Sign.error.noCustom -> text = getString(R.string.location_dialog_but_setting)
                Sign.error.superRange -> text = getString(R.string.location_dialog_but_wait_minute)
                Sign.error.superRangeNoPlace -> text = getString(R.string.location_dialog_but_wait_minute)
                Sign.error.noTime -> text = getString(R.string.location_sign_dismiss)
                Sign.error.workSuperRange -> text = getString(R.string.location_dialog_but_wait_minute)
                Sign.error.network -> text = getString(R.string.location_dialog_but_restart)
                else -> text = getString(R.string.location_sign_dismiss)
            }
            setOnClickListener {
                dismiss()
                when (errorType) {
                    Sign.error.noCustom -> openSettingCustomAddress()
                    Sign.error.superRange -> openFEMain()
                    Sign.error.network -> {
                        isRestartSignIn = true
                        SignInRapidlyActivity.startSignInRapidly(context!!)
                    }
                    else -> openFEMain()
                }
            }
        }
    }

    private fun openFEMain() {
        val intent = Intent(mContext, FEMainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    private fun openCalendar() {
        if (CoreZygote.getApplicationServices().activityInStacks(SignInMainTabActivity::class.java)) {
            openNearby()
            EventBus.getDefault().post(EventMainTabShowFragment(SignInMainTabActivity.showRecord))
        } else {
            SignInMainTabActivity.start(mContext!!, SignInMainTabActivity.showRecord)
        }
    }

    private fun openSettingCustomAddress() {
        LocationSendActivity.start(mContext!!, true, K.location.LOCATION_CUSTOM_SETTING)
    }

    private fun openNearby() {
        SignInMainTabActivity.start(mContext!!, SignInMainTabActivity.showMain)
    }

    private fun openErrorPhoto() {
        val photoItem = LocationPhotoItem()
        photoItem.takePhotoType = TakePhotoSignContract.POIITEM_SEARCH_ERROR
        val intent = Intent(mContext, OnSiteSignActivity::class.java)
        intent.putExtra(LocationReportSignContract.LOCATION_PHOTO_ITEM, GsonUtil.getInstance().toJson(photoItem))
        (mContext as Activity).startActivityForResult(intent, LocationReportSignContract.POST_PHOTO_SIGN_DATA)
    }

    override fun show(manager: FragmentManager, tag: String) {
        val ft = manager.beginTransaction()
        ft.add(this, tag)
        ft.commitAllowingStateLoss()//小米手机容易见鬼
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        mListener?.invoke(isRestartSignIn)
    }
}

