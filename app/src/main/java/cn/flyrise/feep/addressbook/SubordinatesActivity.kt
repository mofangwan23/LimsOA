package cn.flyrise.feep.addressbook

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import cn.flyrise.feep.K
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.MineDepartmentAdapter
import cn.flyrise.feep.addressbook.adapter.OnContactItemClickListener
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter
import cn.flyrise.feep.addressbook.selection.ContactSelectionView
import cn.flyrise.feep.addressbook.selection.DATASOURCE_LEADER_POINT
import cn.flyrise.feep.addressbook.selection.newSubordinatesPresenter
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter
import cn.flyrise.feep.addressbook.view.LetterFloatingView
import cn.flyrise.feep.commonality.TheContactPersonSearchActivity
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FELetterListView
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.core.watermark.WMAddressDecoration
import cn.flyrise.feep.core.watermark.WMStamp
import cn.flyrise.feep.meeting7.ui.component.STATE_EMPTY
import cn.flyrise.feep.meeting7.ui.component.StatusView
import com.drop.WaterDropSwipRefreshLayout
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 我的下属列表
 * Create mofangwan
 */
class SubordinatesActivity : BaseActivity(), ContactSelectionView {

    private val mHandler = Handler()
    private lateinit var mSwipeRefreshLayout: WaterDropSwipRefreshLayout;
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var mContactAdapter: MineDepartmentAdapter
    private lateinit var mLetterView: FELetterListView

    private lateinit var mLetterFloatingView: View                          // 姓氏索引列表
    private lateinit var mTvLetterView: TextView
    private lateinit var mSurnameListView: ListView
    private lateinit var mSurnameAdapter: SurnameAdapter
    private lateinit var mStatusView: StatusView

    private lateinit var mWindowManager: WindowManager
    private lateinit var mLetterFloatingRunnable: Runnable
    private lateinit var mPresenter: SelectionPresenter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subordinates)
    }

    override fun toolBar(toolbar: FEToolbar) {
        toolbar.setTitle(R.string.contacts_subordinates)
    }

    override fun bindView() {
        mLetterView = findViewById<View>(R.id.letterListView) as FELetterListView
        mSwipeRefreshLayout = findViewById<View>(R.id.swipeRefreshLayout) as WaterDropSwipRefreshLayout
        mRecyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        mStatusView = findViewById<View>(R.id.statusview) as StatusView;
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.itemAnimator = null

        val watermark = WMStamp.getInstance().waterMarkText
        mRecyclerView.addItemDecoration(WMAddressDecoration(watermark))

        mContactAdapter = MineDepartmentAdapter(this)
        mStatusView.setStatus(STATE_EMPTY);
        mContactAdapter.setEmptyView(mStatusView)
        mRecyclerView.adapter = mContactAdapter
        mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent)

    }

    override fun bindData() {
        this.bindLetterFloatingView()
        val dataSource = DATASOURCE_LEADER_POINT
        mPresenter = newSubordinatesPresenter(dataSource)!!
        mPresenter.selectionView = ContactSelectionPage@ this
        mPresenter.start()
    }


    override fun bindListener() {
        mLetterFloatingRunnable = object : Runnable {
            override fun run() {
                mLetterFloatingView.visibility = View.GONE
            }
        }

        mLetterView.setOnTouchingLetterChangedListener {                  // 右侧字母索引
            letter ->
            if (mContactAdapter != null) {
                val selection = letter.toLowerCase()[0].toInt()

                val position = mContactAdapter.getPositionBySelection(selection)
                if (position != -1) {
                    (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
                }

                val surnames = mContactAdapter.getSurnameBySelection(selection)
                mTvLetterView.text = letter
                mSurnameAdapter.notifyChange(surnames)
                mLetterFloatingView.visibility = View.VISIBLE
                mHandler.removeCallbacks(mLetterFloatingRunnable)
                mHandler.postDelayed(mLetterFloatingRunnable, 3000)
            }
        }

        mContactAdapter.setOnContactItemClickListener(OnContactItemClickListener { addressBook, position -> this.onItemClick(addressBook, position) })

        mSwipeRefreshLayout.setOnRefreshListener {
            mPresenter.start()//刷新数据
            Observable.timer(2, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { time -> mSwipeRefreshLayout.isRefreshing = false }
        }
    }

    protected fun onItemClick(addressBook: AddressBook, position: Int) {
        val startIntent = Intent(this, AddressBookDetailActivity::class.java)
        startIntent.putExtra(K.addressBook.user_id, addressBook.userId)
        startIntent.putExtra(K.addressBook.department_id, addressBook.deptId)
        startActivity(startIntent)
    }

    private fun bindLetterFloatingView() {
        mLetterFloatingView = LetterFloatingView(this)
        mTvLetterView = mLetterFloatingView.findViewById(R.id.overlaytext)
        mSurnameListView = mLetterFloatingView.findViewById(R.id.overlaylist)
        mSurnameAdapter = SurnameAdapter();
        mSurnameListView.setAdapter(mSurnameAdapter)
        mLetterFloatingView.visibility = View.INVISIBLE

        mLetterFloatingView.setOnKeyListener { v, keyCode, event ->
            FELog.i("AddressBookActivity key listener : $keyCode")
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (mLetterFloatingView.visibility == View.VISIBLE) {
                    mLetterFloatingView.visibility = View.GONE
                    finish()
                }
            }
            false
        }

        mSurnameListView.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                mHandler.removeCallbacks(mLetterFloatingRunnable)
                mHandler.postDelayed(mLetterFloatingRunnable, 2000)
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
        })

        mSurnameListView.setOnItemClickListener { parent, view, position, id ->
            mHandler.removeCallbacks(mLetterFloatingRunnable)
            mHandler.postDelayed(mLetterFloatingRunnable, 2000)
            val surname = mSurnameAdapter.getItem(position) as String
            val surnameAscii = surname[0].toInt()
            val surnamePosition = mContactAdapter.getPositionBySurname(surnameAscii)
            if (surnamePosition != -1) {
                (mRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(surnamePosition, 0)
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
        mWindowManager.addView(mLetterFloatingView, lp)
    }

    override fun showContacts(addressBooks: List<AddressBook?>?, deptUser: List<AddressBook?>?) {
        mContactAdapter.setContacts(addressBooks);
        mContactAdapter.buildSelection(addressBooks);
        mLetterView.setShowLetters(mContactAdapter.letterList)

    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }


}
