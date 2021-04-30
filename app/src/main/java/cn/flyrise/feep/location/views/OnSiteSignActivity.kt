package cn.flyrise.feep.location.views

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import cn.flyrise.android.shared.utility.FEUmengCfg
import cn.flyrise.feep.R
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.location.contract.TakePhotoSignContract
import cn.flyrise.feep.location.event.EventPhotographFinish
import cn.flyrise.feep.location.presenter.OnSiteSignPresenter
import cn.flyrise.feep.location.widget.MaxLengthWatcher
import cn.flyrise.feep.media.record.camera.CameraManager
import kotlinx.android.synthetic.main.onsite_sign.*
import org.greenrobot.eventbus.EventBus

/**
 * 现场签到
 * @author 罗展健 <br></br>
 * by 2015-02-09
 * cm-2018---7.0
 */
class OnSiteSignActivity : BaseOnSiteSignActivity(), TakePhotoSignContract.View {

    private var mPresenter: TakePhotoSignContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.onsite_sign)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mPresenter!!.setSavedTakePhoto(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mPresenter!!.getSavedTakePhoto(savedInstanceState)
    }

    override fun toolBar(toolbar: FEToolbar) {
        toolbar.setTitle(R.string.onsite_sign_title)
        toolbar.setNavigationOnClickListener {
            EventBus.getDefault().post(EventPhotographFinish())
            finish()
        }
    }

    override fun bindData() {
        mPresenter = OnSiteSignPresenter(this)
        mPresenter!!.getIntentData(intent)
        mEtDescribe!!.addTextChangedListener(MaxLengthWatcher().setMaxLen(100).setEditText(mEtDescribe)
                .setOnTextChatLengthListener { chatNumber -> mTvDescribeNum!!.text = chatNumber })
    }

    override fun bindListener() {
        mImgPhotoView!!.setOnClickListener { v -> mPresenter!!.hasPhoto() }
        mImgDeleteView!!.setOnClickListener { v -> mPresenter!!.clickDeleteView() }
        mTvSubmit!!.setOnClickListener { v -> mPresenter!!.submit(describeText) }
    }

    override fun setTime(text: String) {
        mTvTime?.text = text
        mTvSubmit.isEnabled = !(mPresenter?.isWorkingTimeNotAllowSign() ?: false)
    }

    override fun getTime(): TextView? {
        return mTvTime
    }

    override fun setTitle(text: String) {
        mEtTitle?.setText(text)
    }

    override fun getTitleText(): String {
        return if (mEtTitle == null) "" else mEtTitle!!.text.toString().trim { it <= ' ' }
    }

    override fun getDescribeText(): String {
        return if (mEtDescribe == null) "" else mEtDescribe!!.text.toString().trim { it <= ' ' }
    }

    override fun setEdTitleMaxLen() {
        mEtTitle?.addTextChangedListener(MaxLengthWatcher().setEditText(mEtTitle).setMaxLen(100)
                .setOnTextChatLengthListener { mTvTitleNums?.text = it })
    }

    override fun setTitleNumberVisibile(isVisibile: Boolean) {
        mTvTitleNums?.visibility = if (isVisibile) View.VISIBLE else View.INVISIBLE
    }

    override fun setAddress(text: String) {
        mTvAddress?.text = text
    }

    override fun setTitleEnabled(isEnabled: Boolean) {
        mEtTitle?.isEnabled = isEnabled
    }

    override fun setTimeVisible(isVisile: Boolean) {
        mLayoutTime?.visibility = if (isVisile) View.VISIBLE else View.GONE
        mLineTime?.visibility = if (isVisile) View.VISIBLE else View.GONE
    }

    override fun setAddressVisible(isVisile: Boolean) {
        mLayoutAddress?.visibility = if (isVisile) View.VISIBLE else View.GONE
        mLineAddress?.visibility = if (isVisile) View.VISIBLE else View.GONE
    }

    override fun onCameraPermission() {
        mPresenter!!.openTakePhoto()
    }

    override fun onLocaPermission() {
        mPresenter!!.locationPermissionGranted()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != CameraManager.TAKE_PHOTO_RESULT || resultCode != Activity.RESULT_OK) return
        mPresenter!!.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        mPresenter!!.deletePhoto()
        mPresenter!!.onDestroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.LocationOnSite)
    }

    override fun onResume() {
        super.onResume()
        FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.LocationOnSite)
    }

    override fun setmImgPhotoView(bitmap: Bitmap?) {
        if (bitmap == null) mImgPhotoView!!.setImageResource(R.drawable.location_photo_add_piture)
        else mImgPhotoView!!.setImageBitmap(bitmap)
    }

    override fun getmImgPhotoView(): BitmapDrawable? {
        return if (mImgPhotoView == null) null else mImgPhotoView!!.drawable as BitmapDrawable
    }

    override fun setDeleteViewVisible(isVisible: Boolean) {
        mImgDeleteView!!.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) EventBus.getDefault().post(EventPhotographFinish())
        return super.onKeyDown(keyCode, event)
    }
}