package cn.flyrise.feep.study.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import cn.flyrise.feep.core.base.component.BaseActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.study.adapter.AnswerResultAdapter
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.feep.study.entity.TrainingSignResponse
import cn.flyrise.feep.study.presenter.QuestionResultPresenter
import cn.flyrise.study.R
import com.chad.library.adapter.base.BaseQuickAdapter
import kotlinx.android.synthetic.main.stu_activity_question_result.*
import java.util.*

interface QuestionResultView {
    fun showQuestionResult(response: GetQuestionResponse)

    fun showError()
}

class QuestionResultActivity : BaseActivity(), QuestionResultView {
    private val right = "1"
    private val wrong = "-1"
    private val notPiGai = "0"
    override fun showQuestionResult(response: GetQuestionResponse) {
        var rightCount = 0
        var totalCount = 0
        list = response.datalist
        adapter?.setNewData(list)
        if (CommonUtil.nonEmptyList(response.datalist)){
            totalCount = response.datalist.size
            for (i in 0 until response.datalist.size) {
                when {
                    TextUtils.equals(response.datalist[i].rw,right) -> {
                        rightCount ++
                    }
                    TextUtils.equals(response.datalist[i].rw,notPiGai) -> {
                        totalCount --
                    }
                }
            }
            tv_right.text = rightCount.toString() + "题"
            val cha = response.datalist.size - totalCount
            if (cha == 0){
                tvTotalNum.text = "共" + response.datalist.size + "题"
            }else {
                tvTotalNum.text = "共" + response.datalist.size + "题" + "（" + cha + "题未批改）"
            }
            var lvDouble = 0.0f
            lvDouble = (rightCount * 1.0f / totalCount)
            val lv = lvDouble * 100
            val lvv = Formatter().format("%.2f", lv).toString()
            tvLv.text = "$lvv%"
        }

        adapter?.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.rlAnswerView ->{
                    val intent= Intent(this, QuestionAnalysetActivity::class.java)
                    intent.putExtra("questionIndex",position)
                    intent.putExtra("questionDetail",response)
                    startActivity(intent)
                }

            }
        }

        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }

    }

    override fun showError() {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog!!.hide()
            mLoadingDialog = null
        }
    }


    private var adapter: AnswerResultAdapter? = null
    private lateinit var presenter: QuestionResultPresenter
    private var list = mutableListOf<GetQuestionResponse.DatalistBean>()
    private var examItem: TrainingSignResponse.QueryBean? = null
    private var mLoadingDialog: FELoadingDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.stu_activity_question_result)
    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        toolbar?.title = "答题卡"
    }

    override fun bindView() {
        super.bindView()
        adapter = AnswerResultAdapter(R.layout.stu_item_answer_view, list)
        recyclerView.layoutManager = GridLayoutManager(this, 5)
        recyclerView.adapter = adapter
    }

    override fun bindData() {
        super.bindData()
        presenter = QuestionResultPresenter(this)
        examItem = intent.getSerializableExtra("examItem") as TrainingSignResponse.QueryBean?
        presenter.getQuestionResultDetail(examItem?.paperid,examItem?.trainTask_ID,examItem?.infoid)
        if (mLoadingDialog == null) {
            mLoadingDialog = FELoadingDialog.Builder(this).setCancelable(false).create()
        }
        mLoadingDialog!!.show()
    }
}