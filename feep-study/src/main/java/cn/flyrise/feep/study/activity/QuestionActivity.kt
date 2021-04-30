package cn.flyrise.feep.study.activity

import android.os.Bundle
import android.os.CountDownTimer
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.FELog
import cn.flyrise.feep.core.common.FEToast
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.core.common.utils.DateUtil
import cn.flyrise.feep.core.dialog.CustomDialog
import cn.flyrise.feep.core.dialog.FELoadingDialog
import cn.flyrise.feep.study.adapter.NewOptionListAdapter
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.feep.study.entity.RefreshExamBean
import cn.flyrise.feep.study.entity.TrainingSignResponse
import cn.flyrise.feep.study.presenter.QuestionDetailPresenter
import cn.flyrise.feep.study.respository.ExamDataRepository
import cn.flyrise.feep.study.view.ElcSignVerifyManager
import cn.flyrise.feep.study.view.NewAnswerCardDialog
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.activity_stu_question.*
import kotlinx.android.synthetic.main.stu_que_edit_content.view.*
import org.greenrobot.eventbus.EventBus

interface QuestionView {

    fun showQuestionInfo(response: GetQuestionResponse)

    fun showQuestionsError()

    fun submitAnswerSuccess()

    fun submitAnswerFaile()

    fun showLoading()

    fun hideLoading()

}

class QuestionActivity : NotTranslucentBarActivity(), QuestionView {
    override fun submitAnswerSuccess() {
        CustomDialog.Builder(this@QuestionActivity)
                .setMessage("提交成功! 可以到【考试记录】查看成绩哦～")
                .setPositiveButtonText("确定") { _, _ ->
                    EventBus.getDefault().post(RefreshExamBean())
                    finish()
                }
                .create()
                .showDialog()
    }

    override fun submitAnswerFaile() {
        FEToast.showMessage("提交失败")
    }

    override fun showQuestionInfo(response: GetQuestionResponse) {
        stuStatusView.visibility = View.GONE
        if (CommonUtil.nonEmptyList(response.info)) {
            info = response.info!![0]
            toolbar.title = info?.paper_name
            timeDown(Integer.valueOf(response.info!![0].paper_minute))
        }
        questionList = response.datalist
        if (CommonUtil.nonEmptyList(questionList)) {
            refreshQuestionView()
        } else {
            ll_card.visibility = View.GONE
            llSubmit.visibility = View.GONE
        }
        startTime = DateUtil.formatTimeForHms(System.currentTimeMillis())
    }


    override fun showQuestionsError() {
        stuStatusView.visibility = View.VISIBLE
        stuStatusView.setStatus(0)
        stuStatusView.setOnRetryClickListener(View.OnClickListener {
            presenter.getQuestionDetail(examItem?.id, examItem?.trainTask_ID, examItem?.infoid)
        })


    }

    override fun showLoading() {
        if (mLoadingDialog == null) {
            mLoadingDialog = FELoadingDialog.Builder(this).setCancelable(false).create()
        }
        mLoadingDialog?.show()
    }

    override fun hideLoading() {
        if (mLoadingDialog?.isShowing != null) {
            mLoadingDialog?.hide()
            mLoadingDialog = null
        }
    }

    private lateinit var toolbar: FEToolbar
    private lateinit var presenter: QuestionDetailPresenter
    private var questionIndex = 0
    private var questionList: MutableList<GetQuestionResponse.DatalistBean>? = null
    private var info: GetQuestionResponse.InfoBean? = null
    private var timer: CountDownTimer? = null  //题目限时，倒计时
    private var startTime: String? = null
    private var endTime: String? = null
    private var answerCardDialog: NewAnswerCardDialog? = null
    private var type = 0
    private var examItem: TrainingSignResponse.QueryBean? = null
    private var mLoadingDialog: FELoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stu_question)
        type = intent.getIntExtra("questionType", 0)
        examItem = intent.getSerializableExtra("examItem") as TrainingSignResponse.QueryBean?
        presenter = QuestionDetailPresenter(this, ExamDataRepository())
        presenter.getQuestionDetail(examItem?.id, examItem?.trainTask_ID, examItem?.infoid)
    }

    override fun bindView() {
        super.bindView()
        queNext.setOnClickListener {
            questionIndex++
            refreshQuestionView()
        }

        queLast.setOnClickListener {
            questionIndex--
            refreshQuestionView()
        }

        queSubmit.setOnClickListener {
            submit()
        }

        llSubmit.setOnClickListener {
            submit()
        }

        ll_card.setOnClickListener {
            answerCardDialog = NewAnswerCardDialog(this).onCreate(this, questionList)
        }


    }

    override fun toolBar(toolbar: FEToolbar) {
        super.toolBar(toolbar)
        this.toolbar = toolbar
    }


    private fun refreshQuestionView() {
        if (questionIndex < 0 && CommonUtil.isEmptyList(questionList)) {
            return
        }
        val examQuestionBean = questionList!![questionIndex]
        when (examQuestionBean.qtype) {
            "1" -> {
                queType.text = "单选题"
            }
            "2" -> {
                queType.text = "多选题"
            }
            "3" -> {
                queType.text = "判断题"
            }
            "4" -> {
                queType.text = "填空题"
            }
            "5" -> {
                queType.text = "问答题"
            }

        }
        queScore.text = "(" + examQuestionBean.score + "分)"
        showQuestionTopTitle()
        queContent.text = getQuestionContent(examQuestionBean
                .content)

        addQuestionView(examQuestionBean)

        queLast.visibility = if (questionIndex == 0) View.GONE else View.VISIBLE

        queNext.visibility = if (questionIndex == questionList?.size!! - 1) View.GONE else View.VISIBLE

        if (questionIndex == questionList?.size!! - 1) {
            toolbar.rightText = "提交"
            toolbar.setRightTextClickListener {
                submit()
            }
        } else {
            toolbar.setRightTextVisbility(View.GONE)
        }

    }

    /**
     * 根据题目类型加载不同的布局
     */
    private fun addQuestionView(examQuestionBean: GetQuestionResponse.DatalistBean) {
        when (examQuestionBean.qtype) {
            "1", "2", "3" -> {  //1-单选，2-多选，3-判断
                val optionListView = ListView(this)
                if (TextUtils.equals("3", examQuestionBean.qtype)) {
                    var optionList = mutableListOf<GetQuestionResponse.DatalistBean.DBBean>()
                    optionList.add(GetQuestionResponse.DatalistBean.DBBean("NO"))
                    optionList.add(GetQuestionResponse.DatalistBean.DBBean("YES"))
                    examQuestionBean.db = optionList
                }

                val adapter = NewOptionListAdapter(this, examQuestionBean.db, optionListView, examQuestionBean.qtype, false, "", "")
                optionListView.divider = null
                optionListView.dividerHeight = 30
                optionListView.adapter = adapter
                //设置默认选中
                var userAnswer = questionList!![questionIndex].userAnswer
                if (TextUtils.equals("1", examQuestionBean.qtype) || TextUtils.equals("3", examQuestionBean.qtype)) {
                    optionListView.choiceMode = ListView.CHOICE_MODE_SINGLE
                    if (!TextUtils.isEmpty(userAnswer)) {
                        when (userAnswer) {
                            "A", "NO" -> {
                                optionListView.setItemChecked(0, true)
                            }
                            "B", "YES" -> {
                                optionListView.setItemChecked(1, true)
                            }
                            "C" -> {
                                optionListView.setItemChecked(2, true)
                            }
                            "D" -> {
                                optionListView.setItemChecked(3, true)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    }
                } else {
                    optionListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
                    if (!TextUtils.isEmpty(userAnswer)) {
                        if (userAnswer.contains("A")) {
                            optionListView.setItemChecked(0, true)
                        }
                        if (userAnswer.contains("B")) {
                            optionListView.setItemChecked(1, true)
                        }
                        if (userAnswer.contains("C")) {
                            optionListView.setItemChecked(2, true)
                        }
                        if (userAnswer.contains("D")) {
                            optionListView.setItemChecked(3, true)
                        }
                        adapter.notifyDataSetChanged()
                    }
                }
                var answerList = mutableListOf<String>()
                for (i in 0 until examQuestionBean.db!!.size) {
                    answerList.add(i, "")
                }
                optionListView.setOnItemClickListener { _, _, i, _ ->
                    if (TextUtils.equals("2", examQuestionBean.qtype)) {
                        val checkedItemPositions: SparseBooleanArray = optionListView.checkedItemPositions
                        val isChecked = checkedItemPositions.get(i)
                        if (isChecked) {
                            answerList.set(i, examQuestionBean.db[i].salisa)
                        } else {
                            answerList.set(i, "")
                        }
                        val answer = StringBuilder()
                        for (j in 0 until answerList.size) {
                            answer.append(answerList[j])
                        }
                        questionList!![questionIndex].userAnswer = answer.toString()
                    } else {
                        questionList!![questionIndex].userAnswer = examQuestionBean.db[i].salisa
                    }

                    adapter.notifyDataSetChanged()
                }

                queBody.removeAllViews()
                val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800)
                var totalHeight = 0
                for (i in 0 until examQuestionBean.db!!.size) {
                    val listItem = adapter.getView(i, null, optionListView)
                    listItem.measure(0, 0)
                    totalHeight += listItem.measuredHeight
                }
                val dividerHight = optionListView.dividerHeight * (adapter.count - 1)
                layoutParams.height = totalHeight + dividerHight
                queBody.addView(optionListView, layoutParams)
            }

            "4", "5" -> {
                val answerContent = layoutInflater.inflate(R.layout.stu_que_edit_content, queBody.parent as ViewGroup, false)
                answerContent.editContent.setText(questionList!![questionIndex].userAnswer)
                answerContent.editContent.addTextChangedListener(object : TextWatcher {
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    }

                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                    }

                    override fun afterTextChanged(s: Editable) {
                        if (s.length >= 0) {
                            questionList!![questionIndex].userAnswer = s.toString()
                        }
                    }
                })
                queBody.removeAllViews()
                queBody.addView(answerContent)
            }
        }
    }

    /**
     * 显示题型、分数、说明、题数
     */
    private fun showQuestionTopTitle() {
        questionList?.let {
            val text = String.format("%d/%d", questionIndex + 1, questionList?.size)
            val spannableString = SpannableString(text)
            val color = resources.getColor(R.color.dj_mian_text_color)
            val length = getNumberLength(questionIndex + 1)
            spannableString.setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            spannableString.setSpan(AbsoluteSizeSpan(28, true), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
            queIndex.text = spannableString
        }
    }

    /**
     * @param time 倒计时多少秒
     */
    private fun timeDown(time: Int) {
        timer = object : CountDownTimer((time * 60 * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minute = (millisUntilFinished / (1000 * 60)).toInt()
                val second = (millisUntilFinished / 1000).toInt() % 60
                queLimitTime.text = String.format("剩余时间: %02d:%02d", minute, second)
            }

            override fun onFinish() {
                queLimitTime.text = "剩余时间: 00:00"
                FEToast.showMessage("时间到，提交答卷！")
                submit()
            }
        }
        timer?.start()
    }

    private fun submit() {
        questionList?.let {
            for (i in 0 until it.size) {
                if (TextUtils.isEmpty(it[i].userAnswer)) {
                    FEToast.showMessage("您有题目未答完！")
                    return
                }
            }
            if (getUserAnswerList().size != it.size) {
                FEToast.showMessage("您有题目未答完")
                return
            }
            endTime = DateUtil.formatTimeForHms(System.currentTimeMillis())
            var recordId = examItem?.id
            var trainTaskId = examItem?.trainTask_ID

            var isPass = "0"
            if (TextUtils.isEmpty(recordId) || TextUtils.isEmpty(trainTaskId)) {
                FEToast.showMessage("暂时不能提交")
                FELog.d("examActivity", "请传入recordId")
                return
            }
            if (getUserMark().toInt() >= info?.pass_score!!.toInt()) {
                isPass = "1"
            }

            //statusIds:MutableList<String>,qTypes:MutableList<String>,userAnswers: MutableList<String>
            var qIds = mutableListOf<String>()
            var statusIds = mutableListOf<String>()
            var qTypes = mutableListOf<String>()
            var userAnswers = mutableListOf<String>()
            var scores = mutableListOf<String>()
            for (i in 0 until it.size) {
                statusIds.add("1")
                qTypes.add(it[i].qtype)
                userAnswers.add(it[i].userAnswer)
                if (TextUtils.equals(it[i].userAnswer, it[i].skey)) {
                    scores.add(it[i].score)
                } else {
                    scores.add("0")
                }
                qIds.add(it[i].id)
            }
            val verifyManager = ElcSignVerifyManager(this)
            verifyManager.setRequestType("SUBMIT")
            verifyManager.setServerId(info?.serverId)
            verifyManager.setMaster_key(info?.master_key)
            verifyManager.setTableName(info?.tableName)
            verifyManager.startVerify(object : ElcSignVerifyManager.ElcVerifyCallback {
                override fun onSuccess() {
                    presenter.submitExam(recordId, startTime, endTime, trainTaskId, getUserMark(), isPass, qIds, statusIds, qTypes, userAnswers,
                            scores)
                }
                override fun onFail(msg: String?) {
                    FEToast.showMessage(msg)
                }
            })

        }
    }

    private fun getUserAnswerList(): MutableList<String> {
        var userAnswerList: MutableList<String> = mutableListOf()
        questionList?.let {
            for (i in 0 until it.size) {
                userAnswerList.add(it[i].userAnswer)
            }
        }
        return userAnswerList
    }

    private fun getUserMark(): String {
        var mark = 0
        questionList?.let {
            (0 until it.size).forEach { i ->
                if (TextUtils.equals(it[i].userAnswer, it[i].skey)) {
                    mark += Integer.valueOf(it[i].score)
                }
            }
            return mark.toString()
        }
        return ""
    }

    private fun getQuestionContent(content: String): String {
        var newContent: String = content
        if (TextUtils.isEmpty(newContent)) {
            return ""
        }
        val aa = "&nbsp;"
        val bb = "<br />"
        val cc = "asd"
        val dd = "\r\n"
        val ee = "[BlankArea]"
        if (newContent.contains(aa)) {
            newContent = newContent.replace(aa, "");
        }

        if (newContent.contains(bb)) {
            newContent = newContent.replace(bb, "")
        }

        if (newContent.contains(cc)) {
            newContent = newContent.replace(cc, "")
        }

        if (newContent.contains(dd)) {
            newContent = newContent.replace(dd, "")
        }

        if (newContent.contains(ee)) {
            newContent = newContent.replace(ee, "____")
        }

        return newContent
    }

    private fun getNumberLength(number: Int): Int {
        var number = number
        var length = 1
        while (number >= 10) {
            length++
            number /= 10
        }
        return length
    }
}