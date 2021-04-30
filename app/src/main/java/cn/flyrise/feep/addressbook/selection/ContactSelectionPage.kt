package cn.flyrise.feep.addressbook.selection

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.AbsListView
import android.widget.CheckBox
import android.widget.ListView
import android.widget.TextView
import cn.flyrise.android.library.utility.LoadingHint
import cn.flyrise.feep.R
import cn.flyrise.feep.addressbook.adapter.BaseContactAdapter
import cn.flyrise.feep.addressbook.adapter.ContactAdapter
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter
import cn.flyrise.feep.addressbook.selection.presenter.SelectionPresenter
import cn.flyrise.feep.addressbook.view.LetterFloatingView
import cn.flyrise.feep.core.base.views.FELetterListView
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.PixelUtil
import cn.flyrise.feep.core.services.model.AddressBook
import cn.flyrise.feep.core.watermark.WMAddressDecoration
import cn.flyrise.feep.core.watermark.WMStamp
import cn.flyrise.feep.event.EventContactSelection
import org.greenrobot.eventbus.EventBus
import rx.Observable
import rx.Subscriber
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers

/**
 * @author ZYP
 * @since 2018-06-07 13:47
 */
class ContactSelectionPage : Fragment(), ContactSelectionView {

    lateinit var presenter: SelectionPresenter
    lateinit var intent: Intent

    private val mHandler = Handler()
    private lateinit var mLetterView: FELetterListView
    private lateinit var mLetterFloatingView: View
    private lateinit var mTvLetterView: TextView
    private lateinit var mSurnameListView: ListView
    private lateinit var mSurnameAdapter: SurnameAdapter
    private lateinit var mWindowManager: WindowManager
    private lateinit var mLetterFloatingRunnable: Runnable

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter

    private lateinit var tvSelectConfirm: TextView
    private lateinit var cbxSelectAll: CheckBox
    private var selectedUserId: MutableList<String> = mutableListOf()

    fun setSelectedAddress(selectedAddressBooks: List<String>?) {
        if (CommonUtil.isEmptyList(selectedAddressBooks)) return
        this.selectedUserId.addAll(selectedAddressBooks!!)
    }

    override fun onResume() {
        super.onResume()
        presenter.selectionView = ContactSelectionPage@ this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater.inflate(R.layout.fragment_contact_selection, container, false)
        bindView(contentView!!)
        bindListener();
        return contentView
    }

    private fun bindView(view: View) {
        // 初始化 ToolBar
        val toolBar = view.findViewById<FEToolbar>(R.id.toolBar)
        var title = intent.getStringExtra(TITLE)
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.selected_user_title)
        }
        toolBar.title = title
        toolBar.setNavigationVisibility(View.VISIBLE)
        toolBar.setNavigationOnClickListener { activity!!.finish() }


        // 设置搜索
        val searhView = view.findViewById<ViewGroup>(R.id.layoutContactSearch)
        searhView.setOnClickListener { (activity as ContactSelectionActivity).goToSeachPage() }
        searhView.visibility = if (intent.getBooleanExtra(IS_SHOW_SEARCH, true)) View.VISIBLE else View.GONE;


        // 初始化底部栏
        val searchConfirmView = view.findViewById<ViewGroup>(R.id.layoutSelectionConfirm)
        val selectionMode = intent.getIntExtra(SELECTION_MODE, SELECTION_SINGLE)
        searchConfirmView.visibility = if (selectionMode == SELECTION_SINGLE) View.GONE else View.VISIBLE


        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        adapter = ContactAdapter(activity)
        adapter.setEmptyView(view.findViewById(R.id.ivEmptyView))
        recyclerView.adapter = adapter

        val watermark = WMStamp.getInstance().waterMarkText
        recyclerView.addItemDecoration(WMAddressDecoration(watermark))      // 设置水印
        adapter.setOnContactItemClickListener { addressBook, position ->
            val selectionMode = intent.getIntExtra(SELECTION_MODE, SELECTION_SINGLE)
            // 单选
            if (selectionMode == SELECTION_SINGLE) {
                if (intent.getBooleanExtra(SELECTION_FINISH, true)) {
                    val result = Intent()
                    result.putExtra(CONTACT_IDS, addressBook.userId)
                    result.putExtra(CONTACT_NAMES, addressBook.name)
                    activity!!.setResult(Activity.RESULT_OK, result)
                    activity!!.finish()
                } else {
                    EventBus.getDefault().post(EventContactSelection(addressBook.userId))
                }
                return@setOnContactItemClickListener
            }

            // 多选
            adapter.addSelectedContact(addressBook, position)
            updateSelectResult()                 // 更改数量
        }


        // 初始化底部
        tvSelectConfirm = view.findViewById(R.id.tvSelectionConfirm)
        tvSelectConfirm.setOnClickListener {
            val userIds = StringBuilder()
            adapter.selectedContacts.forEachIndexed { index, addressBook ->
                if (index > 0) {
                    userIds.append(",")
                }
                userIds.append(addressBook.userId)
            }

            val result = Intent()
            result.putExtra(CONTACT_IDS, userIds.toString())
            activity!!.setResult(Activity.RESULT_OK, result)
            activity!!.finish()
        }

        cbxSelectAll = view.findViewById<CheckBox>(R.id.cbxCheckAll)
        cbxSelectAll.setOnClickListener {
            adapter.executeSelect(cbxSelectAll.isChecked)
            updateSelectResult()
        }

        if (selectionMode == SELECTION_MULTI) {
            adapter.withSelect(true)
        }


        // 初始化拼音 A-Z
        mLetterView = view.findViewById(R.id.letterListView)
        mLetterFloatingView = LetterFloatingView(activity)
        mTvLetterView = mLetterFloatingView.findViewById(R.id.overlaytext)
        mSurnameListView = mLetterFloatingView.findViewById(R.id.overlaylist)

        mSurnameAdapter = SurnameAdapter()
        mSurnameListView.setAdapter(mSurnameAdapter)
        mLetterFloatingView.visibility = View.INVISIBLE

        mLetterFloatingView.setOnKeyListener { v, keyCode, event ->
            if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
                if (mLetterFloatingView.visibility == View.VISIBLE) {
                    mLetterFloatingView.visibility = View.GONE
                    activity!!.finish()
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
            val surname = mSurnameAdapter!!.getItem(position) as String
            val surnameAscii = surname[0].toInt()
            val surnamePosition = adapter.getPositionBySurname(surnameAscii)
            if (surnamePosition != -1) {
                (recyclerView!!.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(surnamePosition, 0)
            }
        }

        val lp = WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, PixelUtil.dipToPx(300f),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)

        lp.gravity = Gravity.TOP or Gravity.RIGHT
        lp.x = PixelUtil.dipToPx(40f)
        lp.y = PixelUtil.dipToPx(128f)

        mWindowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mWindowManager.addView(mLetterFloatingView, lp)

        presenter.selectionView = ContactSelectionPage@ this
        presenter.start()
    }

    private fun bindListener() {
        mLetterFloatingRunnable = object : Runnable {
            override fun run() {
                mLetterFloatingView.visibility = View.GONE
            }
        }

        mLetterView.setOnTouchingLetterChangedListener {                  // 右侧字母索引
            val selection = it.toLowerCase()[0].toInt()
            val position = adapter.getPositionBySelection(selection)
            if (position != -1) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            }

            val surnames = adapter.getSurnameBySelection(selection)
            mTvLetterView.text = it
            mSurnameAdapter.notifyChange(surnames)
            mLetterFloatingView.visibility = View.VISIBLE
            mHandler.removeCallbacks(mLetterFloatingRunnable)
            mHandler.postDelayed(mLetterFloatingRunnable, 3000)
        }
    }

    /**
     * 在搜索界面选择指定联系人之后回到这个界面，需要滚动到指定位置
     */
    fun scrollToSelectedContact(selectedContact: AddressBook) {
        if (intent.getIntExtra(SELECTION_MODE, SELECTION_SINGLE) == SELECTION_SINGLE) {
            if (intent.getBooleanExtra(SELECTION_FINISH, true)) {
                val result = Intent()
                result.putExtra(CONTACT_IDS, selectedContact.userId)
                result.putExtra(CONTACT_NAMES, selectedContact.name)
                activity!!.setResult(Activity.RESULT_OK, result)
                activity!!.finish()
            } else {
                EventBus.getDefault().post(EventContactSelection(selectedContact.userId))
            }
            return
        }

        var position = 0
        for (contact in adapter.contacts) {
            if (TextUtils.equals(selectedContact.userId, contact.userId)) {
                break
            }
            position++
        }

        adapter.addSelectedContact(selectedContact, position)
        recyclerView.scrollToPosition(position)
        updateSelectResult()
    }

    private fun updateSelectResult() {
        updateSelectCount()
        val state = adapter.selectState()
        cbxSelectAll.isChecked = state == BaseContactAdapter.CODE_INVERT_SELECT_ALL
        cbxSelectAll.text = getString(R.string.selected_but_all)
    }

    private fun updateSelectCount() {
        tvSelectConfirm.text = "${getString(R.string.selected_but_sure)}(${adapter.selectedCount}/${adapter.itemCount})"
    }

    override fun showContacts(addressBooks: List<AddressBook?>?, deptUser: List<AddressBook?>?) {
        adapter.setContacts(addressBooks)
        adapter.buildSelection(addressBooks)
        mLetterView.setShowLetters(adapter.letterList)
        updateSelectCount()
        setSelectedAddressBooks(addressBooks)
    }

    private fun setSelectedAddressBooks(addressBooks: List<AddressBook?>?) {
        if (CommonUtil.isEmptyList(addressBooks) || CommonUtil.isEmptyList(selectedUserId)) return
        Observable.create({ f: Subscriber<in List<AddressBook?>> ->
            //            val selectedAddressBooks = mutableListOf<AddressBook>()
//            for (address in addressBooks!!) {
//                if (address == null) continue
//                if (selectedUserId.contains(address.userId))
//                    selectedAddressBooks.add(address)
//            }
//            f.onNext(selectedAddressBooks)
            f.onNext(addressBooks?.filter { it != null && selectedUserId.contains(it.userId) })
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (CommonUtil.isEmptyList(it)) return@subscribe
                    adapter.selectedContacts = it
                    adapter.notifyDataSetChanged()
                    updateSelectCount()
                })
    }

    override fun showLoading() {
        LoadingHint.show(activity)
    }

    override fun hideLoading() {
        LoadingHint.hide()
    }
}