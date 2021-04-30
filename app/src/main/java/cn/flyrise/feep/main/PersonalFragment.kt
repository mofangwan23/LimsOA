package cn.flyrise.feep.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.android.protocol.entity.UserDetailsRequest
import cn.flyrise.android.protocol.entity.UserDetailsResponse
import cn.flyrise.android.shared.utility.FEUmengCfg
import cn.flyrise.feep.FEApplication
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.model.AddressBookVO
import cn.flyrise.feep.addressbook.model.Department
import cn.flyrise.feep.addressbook.source.AddressBookRepository
import cn.flyrise.feep.collection.CollectionFolderActivity
import cn.flyrise.feep.commonality.help.HelpActivity
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.DevicesUtil
import cn.flyrise.feep.core.common.utils.NetworkUtil
import cn.flyrise.feep.core.function.FunctionManager
import cn.flyrise.feep.core.image.loader.FEImageLoader
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.core.premission.FePermissions
import cn.flyrise.feep.core.premission.PermissionCode
import cn.flyrise.feep.core.premission.PermissionGranted
import cn.flyrise.feep.event.CompanyChangeEvent
import cn.flyrise.feep.event.EventNotifierUpdateApp
import cn.flyrise.feep.event.EventUpdataUserIcon
import cn.flyrise.feep.main.modules.Sasigay
import cn.flyrise.feep.mobilekey.event.MoKeyNormalEvent
import cn.flyrise.feep.more.AboutActivity
import cn.flyrise.feep.more.AccountSecurityActivity
import cn.flyrise.feep.more.SettingActivity
import cn.flyrise.feep.more.ShareActivity
import cn.flyrise.feep.more.download.manager.DownLoadManagerTabActivity
import cn.flyrise.feep.userinfo.views.UserInfoActivity
import cn.flyrise.feep.utils.Patches
import cn.squirtlez.frouter.FRouter
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI
import kotlinx.android.synthetic.main.personal_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

class PersonalFragment : Fragment() {

    private val FEEP_UMENG = "PersonalFragment"

    private lateinit var mToolbar: FEToolbar
    private var companyName: String? = null
    private var userId: String? = null


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        EventBus.getDefault().register(this@PersonalFragment)
        setListener()
        bindData()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.personal_fragment, container, false)
    }

    fun setListener() {
        //信息卡片
        userCardLayout.setOnClickListener { openActivity(UserInfoActivity::class.java) }
        rlCollection.setOnClickListener { openActivity(CollectionFolderActivity::class.java) }
        rlDownloadManager.setOnClickListener { openActivity(DownLoadManagerTabActivity::class.java) }
        rlAboutUs.setOnClickListener { openActivity(AboutActivity::class.java) }
        rlHelp.setOnClickListener { openActivity(HelpActivity::class.java) }
        rlGesture.setOnClickListener { openActivity(AccountSecurityActivity::class.java) }
        rlShare.setOnClickListener { openActivity(ShareActivity::class.java) }
        rlSetting.setOnClickListener { openActivity(SettingActivity::class.java) }
        rlFeedback.setOnClickListener {
            rlFeedback.setEnabled(false);
            FePermissions.with(this@PersonalFragment)
                    .permissions(arrayOf(Manifest.permission.CAMERA))
                    .rationaleMessage(resources.getString(R.string.permission_rationale_camera))
                    .requestCode(PermissionCode.CAMERA)
                    .request()
        }
        if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
//            rlCollection.setVisibility(View.VISIBLE)
        }


    }

    fun bindData() {
        mToolbar = toolBar
        mToolbar.setTitle(activity!!.getString(R.string.personal_title))
        mToolbar.setNavigationVisibility(View.GONE)
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT && !DevicesUtil.isSpecialDevice()) {
            val statusBarHeight = DevicesUtil.getStatusBarHeight(activity!!)
            mToolbar.setPadding(0, statusBarHeight, 0, 0)
        }

        val userInfo = (activity!!.application as FEApplication).userInfo ?: return
        userId = userInfo.userID
        val userName = userInfo.userName

        val services = CoreZygote.getLoginUserServices()
        val url = services.serverAddress + services.userImageHref
        tvName.setText(userName)
        FEImageLoader.load(activity, ivHead, url, userId, userName)

        if ((activity!!.application as FEApplication).hasNewVersion()) {
            app_version_notifier.visibility = View.VISIBLE
        } else {
            app_version_notifier.visibility = View.GONE
        }

        if (CoreZygote.getMobileKeyService() != null && !CoreZygote.getMobileKeyService().isNormal) {
            ivSecurityNotification.visibility = View.VISIBLE
        } else {
            ivSecurityNotification.visibility = View.GONE
        }


        queryCompany(userId!!)

        queryEamil()

    }

    /**
     * 显示公司名
     */
    fun queryCompany(userId: String) {
        //通过EventBus传过来的
        if (!TextUtils.isEmpty(companyName)) {
            tvCompany.text = companyName
        } else if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
            //7.0 以上
            Observable
                    .unsafeCreate<Department> {
                        var company: Department? = null
                        val userId = CoreZygote.getLoginUserServices().userId
                        val dept = AddressBookRepository.get().queryDepartmentWhereUserIn(userId)
                        company = AddressBookRepository.get().queryCompanyWhereDepartmentIn(dept.deptId)
                        it.onNext(company)
                        it.onCompleted()
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (it == null) return@subscribe
                        Sasigay.saveCompany(it)
                        tvCompany.text = it.name
                    }, {
                        it.printStackTrace()
                    })
        } else {
            //7.0以下
            Observable
                    .unsafeCreate<Department> {
                        val department = AddressBookRepository.get().queryDepartmentWhereUserIn(userId)
                        var company: Department? = null
                        if (department != null) {
                            company = AddressBookRepository.get().queryCompanyWhereDepartmentIn(department.deptId)
                        }
                        it.onNext(company)
                    }
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        if (it != null)
                            tvCompany.text = it.name
                    }
        }
    }

    /**
     * 查找邮箱
     */
    fun queryEamil() {
        Observable
                .unsafeCreate<AddressBookVO> {
                    val request = UserDetailsRequest()
                    FEHttpClient.getInstance().post(request, object : ResponseCallback<UserDetailsResponse>() {
                        override fun onCompleted(response: UserDetailsResponse?) {
                            if (response != null && TextUtils.equals(response.errorCode, "0")) {
                                it.onNext(response.result)
                            }
                            it.onCompleted()
                        }

                        override fun onFailure(exception: RepositoryException) {
                            it.onError(exception.exception())
                        }
                    })
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it ->
                    if (it != null) {
                        // 设置邮件
                        tvEmail.text = if (TextUtils.isEmpty(it.email)) "" else it.email

                        // 设置部门+岗位
                        var professionalTitle = ""
                        if (!TextUtils.isEmpty(it.departmentName)) {
                            professionalTitle = it.departmentName
                        }

                        if (!TextUtils.isEmpty(it.position)) {
                            professionalTitle = if (TextUtils.isEmpty(professionalTitle))
                                it.position
                            else
                                professionalTitle + "-" + it.position
                        }
                        tvTitle.text = professionalTitle
                    }
                }, { exception -> exception.printStackTrace() })
    }

    fun openActivity(activityClass: Class<out Activity>) {
        startActivity(Intent(activity, activityClass))
    }

    override fun onResume() {
        super.onResume()
        FEUmengCfg.onFragmentResumeUMeng(FEEP_UMENG)
        rlFeedback.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        FEUmengCfg.onFragmentPauseUMeng(FEEP_UMENG)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusNotifierUpdataApp(updataApp: EventNotifierUpdateApp) {
        if (updataApp.isUpdataApp) {
            app_version_notifier.visibility = View.VISIBLE
        } else {
            app_version_notifier.visibility = View.GONE
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun eventBusUpdataUserIcon(updata: EventUpdataUserIcon) {
        if (TextUtils.isEmpty(updata.version)) {
            return
        }
        val userInfo = (activity!!.application as FEApplication).userInfo
        val userId = userInfo.userID
        val userName = userInfo.userName
        val services = CoreZygote.getLoginUserServices().serverAddress
        val url = services + updata.version
        FEImageLoader.load(activity, ivHead, url, userId, userName)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onCompanyChange(event: CompanyChangeEvent) {
        companyName = event.selectedCompanyName
        queryCompany(userId!!)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setMobileKeyActivitState(event: MoKeyNormalEvent) {
        ivSecurityNotification.visibility = if (event.isNormal) View.GONE else View.VISIBLE
    }

    @PermissionGranted(PermissionCode.CAMERA)
    open fun onCameraPermissionGranted() {
        LoadingHint.show(this.activity)
        Observable
                .unsafeCreate<Boolean> {
                    it.onNext(NetworkUtil.ping())
                    it.onCompleted()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { aBoolean ->
                    if (aBoolean!!) {
                        FeedbackAPI.openFeedbackActivity()
                    } else {
                        FEToast.showMessage(R.string.core_http_network_exception)
                        rlFeedback.isEnabled = true
                    }
                    LoadingHint.hide()
                }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }


    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@PersonalFragment)
    }
}