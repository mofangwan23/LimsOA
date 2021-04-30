package cn.flyrise.feep.study.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.study.activity.QuestionActivity
import cn.flyrise.feep.study.activity.QuestionResultActivity
import cn.flyrise.feep.study.adapter.ExamListAdapter
import cn.flyrise.feep.study.common.Key.STU_TYPE_EXAM_MINE
import cn.flyrise.feep.study.common.Key.STU_TYPE_EXAM_RECORD
import cn.flyrise.feep.study.entity.RefreshExamBean
import cn.flyrise.feep.study.entity.TrainingSignRequest
import cn.flyrise.feep.study.entity.TrainingSignResponse
import cn.flyrise.feep.study.presenter.ExamPresenter
import cn.flyrise.feep.study.respository.ExamDataRepository
import cn.flyrise.study.R
import com.chad.library.adapter.base.BaseQuickAdapter
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit


interface ExamView {
    fun showPaperList(dataList: MutableList<TrainingSignResponse.QueryBean>)

    fun showLoading()

    fun hideLoading()
}

class ExamBaseFragment : Fragment(), ExamView,BaseQuickAdapter.OnItemClickListener {

    override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        if (type == 1){
            val intent=Intent(activity, QuestionActivity::class.java)
            examList?.let {
                intent.putExtra("examItem",it[position])
            }
            activity?.startActivity(intent)
        }
        if(type == 2){
            val intent=Intent(activity, QuestionResultActivity::class.java)
            examList?.let {
                intent.putExtra("examItem",it[position])
            }
            activity?.startActivity(intent)
        }
    }

    var type:Int = -1;
    lateinit var adapter: ExamListAdapter
    var examList: MutableList<TrainingSignResponse.QueryBean>? = null
    var request = TrainingSignRequest()
    private var notDataView:View?=null
    private var mLoadingDialog: FELoadingDialog? = null

    override fun showPaperList(dataList: MutableList<TrainingSignResponse.QueryBean>) {
        examList?.clear()
        examList?.addAll(dataList)
        adapter.notifyDataSetChanged()
        adapter.emptyView = notDataView
    }

    override fun showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = FELoadingDialog.Builder(activity).setCancelable(false).create()
        }
        mLoadingDialog?.show()
    }

    override fun hideLoading() {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog?.hide()
            mLoadingDialog = null
        }
    }

    companion object {
        fun newInstance(type:Int): ExamBaseFragment {
            val fragment = ExamBaseFragment()
            fragment.presenter = ExamPresenter(fragment, ExamDataRepository())
            fragment.type = type
            return fragment
        }
    }

    private var presenter: ExamPresenter? = null
    private var stuSwipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        EventBus.getDefault().register(this)
        val contentView = inflater.inflate(R.layout.stu_exam_paper_list_fragment, container, false)
        bindView(contentView)
        return contentView
    }

    fun bindView(view: View) {
        if (examList == null) {
            examList = mutableListOf()
        }
        if (type == 1){
            request.setType(STU_TYPE_EXAM_MINE)
        }
        if (type == 2){
            request.setType(STU_TYPE_EXAM_RECORD)
        }

        adapter = ExamListAdapter(R.layout.study_task_item, examList)
        adapter.onItemClickListener = this
        val stuRecyclerView:RecyclerView = view.findViewById(R.id.stuRecyclerView)
        notDataView = layoutInflater.inflate(R.layout.empty_view, stuRecyclerView.parent as ViewGroup, false)
        stuRecyclerView.layoutManager = LinearLayoutManager(activity)
        stuRecyclerView.adapter = adapter
        stuSwipeRefreshLayout = view.findViewById(R.id.stuSwipeRefreshLayout)
        stuSwipeRefreshLayout?.setOnRefreshListener {
            presenter?.start(request)
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { stuSwipeRefreshLayout?.isRefreshing = false }
        }
        presenter?.start(request)

        notDataView?.setOnClickListener { presenter?.start(request) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveJPushMessage(refreshBean: RefreshExamBean) {                        // 收到机关推送
        stuSwipeRefreshLayout?.isRefreshing = true
        Observable.timer(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { stuSwipeRefreshLayout?.isRefreshing = false }
        presenter?.start(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}