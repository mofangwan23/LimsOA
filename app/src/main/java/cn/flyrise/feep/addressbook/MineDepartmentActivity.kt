package cn.flyrise.feep.addressbook

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.MineDepartmentAdapter
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor
import cn.flyrise.feep.addressbook.source.AddressBookRepository
import cn.flyrise.feep.addressbook.utils.AddressBookExceptionInvoker
import cn.flyrise.feep.addressbook.view.LetterFloatingView
import cn.flyrise.feep.core.CoreZygote
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FELetterListView
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.core.watermark.WMAddressDecoration
import cn.flyrise.feep.core.watermark.WMStamp
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-05-22 16:21
 */
class MineDepartmentActivity : BaseActivity() {

    private val mHandler = Handler()
    private var mRecyclerView: RecyclerView? = null
    private var mContactAdapter: MineDepartmentAdapter? = null
    private var mLetterView: FELetterListView? = null

    private var mLetterFloatingView: View? = null                           // 特么的字母、姓氏索引列表
    private var mTvLetterView: TextView? = null
    private var mSurnameListView: ListView? = null
    private var mSurnameAdapter: SurnameAdapter? = null

    private var mWindowManager: WindowManager? = null
    private var mLetterFloatingRunnable: Runnable? = null
    private var mLoadingDialog: FELoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mine_department)
    }

    override fun toolBar(toolbar: FEToolbar) {
        var departmentName = intent.getStringExtra(K.addressBook.department_name)
        if (TextUtils.isEmpty(departmentName)) {
            departmentName = CommonUtil.getString(R.string.organizational_mine_department)
        }
        toolbar.title = departmentName
    }

    override fun bindView() {
        super.bindView()
        mLetterView = findViewById(R.id.letterListView) as FELetterListView?
        mRecyclerView = findViewById(R.id.recyclerView) as RecyclerView?
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)
        mRecyclerView!!.itemAnimator = null

        val watermark = WMStamp.getInstance().waterMarkText
        mRecyclerView!!.addItemDecoration(WMAddressDecoration(watermark))

        mContactAdapter = MineDepartmentAdapter(this)
        mContactAdapter!!.setEmptyView(findViewById(R.id.ivEmptyView))
        mRecyclerView!!.adapter = mContactAdapter
    }

    override fun bindData() {
        val addressBookState = CoreZygote.getLoginUserServices().addressBookState
        if (addressBookState == AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED) {    // 通讯录下载失败
            AddressBookExceptionInvoker.showAddressBookExceptionDialog(this)
            return
        }

        this.bindLetterFloatingView()

        showLoading()
        val deptId = intent.getStringExtra(K.addressBook.department_id)

        Observable.just(deptId)
                .map<List<AddressBook>> { AddressBookRepository.get().queryContactsByDeptId(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ addressBooks ->
                    hideLoading()
                    showContacts(addressBooks)
                }, { exception ->
                    hideLoading()
                    showContacts(null)
                })
    }

    private fun showContacts(addressBooks: List<AddressBook>?) {
        mContactAdapter!!.setContacts(addressBooks)
        mContactAdapter!!.buildSelection(addressBooks)
        val letter = mContactAdapter!!.letterList
        mLetterView!!.setShowLetters(letter)
    }


    override fun bindListener() {
        mLetterFloatingRunnable = Runnable {
            mLetterFloatingView!!.visibility = View.GONE
        }

        mLetterView!!.setOnTouchingLetterChangedListener {                  // 右侧字母索引
            letter ->
            if (mContactAdapter != null) {
                val selection = letter.toLowerCase()[0].toInt()

                val position = mContactAdapter!!.getPositionBySelection(selection)
                if (position != -1) {
                    (mRecyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                }

                val surnames = mContactAdapter!!.getSurnameBySelection(selection)
                mTvLetterView!!.text = letter
                mSurnameAdapter!!.notifyChange(surnames)
                mLetterFloatingView!!.visibility = View.VISIBLE
                mHandler.removeCallbacks(mLetterFloatingRunnable)
                mHandler.postDelayed(mLetterFloatingRunnable, 3000)
            }
        }

        mContactAdapter!!.setOnContactItemClickListener({ addressBook, position -> this.onItemClick(addressBook, position) })
    }

    protected fun onItemClick(addressBook: AddressBook, position: Int) {
        val startIntent = Intent(this, AddressBookDetailActivity::class.java)
        startIntent.putExtra(K.addressBook.user_id, addressBook.userId)
        startIntent.putExtra(K.addressBook.department_id, addressBook.deptId)
        startActivity(startIntent)
    }

    private fun bindLetterFloatingView() {
        mLetterFloatingView = LetterFloatingView(this)
        mTvLetterView = mLetterFloatingView!!.findViewById(R.id.overlaytext)
        mSurnameListView = mLetterFloatingView!!.findViewById(R.id.overlaylist)

        mSurnameAdapter = SurnameAdapter()
        mSurnameListView!!.setAdapter(mSurnameAdapter)
        mLetterFloatingView!!.visibility = View.INVISIBLE

        mLetterFloatingView!!.setOnKeyListener { v, keyCode, event ->
            FELog.i("AddressBookActivity key listener : $keyCode")
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (mLetterFloatingView!!.visibility == View.VISIBLE) {
                    mLetterFloatingView!!.visibility = View.GONE
                    finish()
                }
            }
            false
        }

        mSurnameListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                mHandler.removeCallbacks(mLetterFloatingRunnable)
                mHandler.postDelayed(mLetterFloatingRunnable, 2000)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })

        mSurnameListView!!.setOnItemClickListener { parent, view, position, id ->
            mHandler.removeCallbacks(mLetterFloatingRunnable)
            mHandler.postDelayed(mLetterFloatingRunnable, 2000)
            val surname = mSurnameAdapter!!.getItem(position) as String
            val surnameAscii = surname[0].toInt()
            val surnamePosition = mContactAdapter!!.getPositionBySurname(surnameAscii)
            if (surnamePosition != -1) {
                (mRecyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(surnamePosition, 0)
            }
        }

        val lp = WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, PixelUtil.dipToPx(300f),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)

        lp.gravity = Gravity.TOP or Gravity.RIGHT
        lp.x = PixelUtil.dipToPx(40f)
        lp.y = PixelUtil.dipToPx(128f)

        mWindowManager = this.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager!!.addView(mLetterFloatingView, lp)
    }

    fun showLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }

        mLoadingDialog = FELoadingDialog.Builder(this)
                .setLoadingLabel(resources.getString(R.string.core_loading_wait))
                .setCancelable(true)
                .setOnDismissListener({ this.finish() })
                .create()
        mLoadingDialog!!.show()
    }

    fun hideLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog!!.removeDismissListener()
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
        mHandler.removeCallbacksAndMessages(null)
        if (mLetterFloatingView != null) {
            mLetterFloatingView!!.visibility = View.GONE
            mWindowManager!!.removeViewImmediate(mLetterFloatingView)
        }
    }
}
