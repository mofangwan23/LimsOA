package cn.flyrise.feep.study.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.core.network.FEHttpClient
import cn.flyrise.feep.core.network.RepositoryException
import cn.flyrise.feep.core.network.callback.ResponseCallback
import cn.flyrise.feep.study.activity.TrainActivity
import cn.flyrise.feep.study.adapter.TaskListAdapter
import cn.flyrise.feep.study.common.Key
import cn.flyrise.feep.study.entity.*
import cn.flyrise.study.R
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.study_fragment_personal_train_task.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * 培训任务BaseFragment
 */
interface TrainView {
    fun loadTaskListSuccess(listData: MutableList<TrainingTaskBean>)

    fun loadTaskListFail()

    fun showLoading()

    fun hideLoading()
}

class TrainBaseFragment : Fragment(), TrainView, BaseQuickAdapter.OnItemClickListener {
    override fun onItemClick(adapter: BaseQuickAdapter<*, *>?, view: View?, position: Int) {
        var intent = Intent(activity, TrainActivity::class.java)
        intent.putExtra("taskId", taskList!![position].taskid)
        intent.putExtra("type",type)
        activity?.startActivity(intent)
    }

    var type: Int = -1;
    lateinit var adapter: TaskListAdapter
    private lateinit var studyRecyclerView: RecyclerView
    private var taskList: MutableList<TrainingTaskBean>? = null
    private var errorView: View?=null
    private var notDataView:View?=null
    private var mLoadingDialog: FELoadingDialog? = null

    companion object {
        fun newInstance(type: Int): TrainBaseFragment {
            var fragment = TrainBaseFragment()
            fragment.type = type
            return fragment
        }
    }

    override fun loadTaskListSuccess(listData: MutableList<TrainingTaskBean>) {
        taskList?.clear()
        taskList?.addAll(listData)
        adapter.notifyDataSetChanged()
        adapter.emptyView = notDataView
    }

    override fun loadTaskListFail() {
        adapter.emptyView = errorView
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        EventBus.getDefault().register(this)
        bindView()
        requestData()
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.study_fragment_personal_train_task, container, false)
        studyRecyclerView = view.findViewById(R.id.studyRecyclerView)
        return view
    }

    fun bindView() {
        if (taskList == null) {
            taskList = mutableListOf()
        }
        adapter = TaskListAdapter(R.layout.study_task_item, taskList)
        studyRecyclerView.layoutManager = LinearLayoutManager(activity)
        studyRecyclerView.adapter = adapter
        adapter.onItemClickListener = this
        swipeRefreshLayout.setOnRefreshListener {
            requestData()
            Observable.timer(1, TimeUnit.SECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { _ -> swipeRefreshLayout?.isRefreshing = false }
        }

        notDataView = layoutInflater.inflate(R.layout.empty_view, studyRecyclerView.parent as ViewGroup, false)
        notDataView?.setOnClickListener { requestData() }
        errorView = layoutInflater.inflate(R.layout.error_view, studyRecyclerView.parent as ViewGroup, false)
        errorView?.setOnClickListener { requestData() }
    }

    private fun requestData() {
        showLoading()
        val request = TrainingSignRequest()
        if (type == 1) {
            request.setType(Key.STU_TYPE_PERSONAL_TRAIN)
        } else {
            request.setType(Key.STU_TYPE_PERSONAL_HAS_TRAIN)
        }
        FEHttpClient.getInstance().post(request, object : ResponseCallback<TrainingTaskResponse>() {
            override fun onCompleted(response: TrainingTaskResponse?) {
                hideLoading()
                if (response != null && TextUtils.equals(response.errorCode, "0")) {
                    loadTaskListSuccess(response.taskBeanList);
                }
            }

            override fun onFailure(repository: RepositoryException?) {
                loadTaskListFail()
                hideLoading()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveJPushMessage(refreshBean: RefreshBean) {                        // 收到机关推送
        requestData()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}


