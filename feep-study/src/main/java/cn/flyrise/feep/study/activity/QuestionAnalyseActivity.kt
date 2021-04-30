package cn.flyrise.feep.study.activity

import android.os.Bundle
import android.text.*
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.util.SparseBooleanArray
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity
import cn.flyrise.feep.core.base.views.FEToolbar
import cn.flyrise.feep.core.common.utils.CommonUtil
import cn.flyrise.feep.study.adapter.NewOptionListAdapter
import cn.flyrise.feep.study.entity.GetQuestionResponse
import cn.flyrise.feep.study.view.NewAnswerCardDialog
import cn.flyrise.study.R
import kotlinx.android.synthetic.main.activity_stu_question.*
import kotlinx.android.synthetic.main.stu_que_edit_content.view.*


class QuestionAnalysetActivity : NotTranslucentBarActivity(){

    private fun showQuestionInfo(response: GetQuestionResponse?) {
        stuStatusView.visibility = View.GONE

        if (CommonUtil.nonEmptyList(response?.info)) {
            info = response?.info!![0]
            toolbar.title = info?.paper_name
        }
        questionList = response?.datalist
        if (CommonUtil.nonEmptyList(questionList)) {
            refreshQuestionView()
        } else {
            ll_card.visibility = View.GONE
            llSubmit.visibility = View.GONE
        }
    }


    fun showQuestionsError() {
        stuStatusView.setStatus(0)
        stuStatusView.setOnRetryClickListener(View.OnClickListener {
        })
    }

    private lateinit var toolbar: FEToolbar
    private var questionIndex = 0
    private var questionList: MutableList<GetQuestionResponse.DatalistBean>? = null
    private var info: GetQuestionResponse.InfoBean? = null
    private var answerCardDialog: NewAnswerCardDialog? = null
    private val right = "1"
    private val wrong = "-1"
    private val notPiGai = "0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stu_question)
        var resopnse = intent.getSerializableExtra("questionDetail") as GetQuestionResponse
        questionIndex = intent.getIntExtra("questionIndex",0)
        showQuestionInfo(resopnse)
    }

    override fun bindView() {
        super.bindView()
        queLimitTime.visibility = View.GONE
        llSubmit.visibility = View.GONE
        ll_card.visibility = View.GONE
        queSubmit.visibility = View.GONE

        queNext.setOnClickListener {
            questionIndex++
            refreshQuestionView()
        }

        queLast.setOnClickListener {
            questionIndex--
            refreshQuestionView()
        }

    }

    override fun toolBar(toolbar: FEToolbar?) {
        super.toolBar(toolbar)
        this.toolbar = toolbar!!
    }


    private fun refreshQuestionView() {
        if (questionIndex < 0) {
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
        queContent.text = getQuestionContent(examQuestionBean.content)

        queTips.visibility = if (TextUtils.isEmpty(questionList!![questionIndex].useR_ANSWER)) View.VISIBLE else View.GONE

        addQuestionView(examQuestionBean)

        queLast.visibility = if (questionIndex == 0) View.GONE else View.VISIBLE

        queNext.visibility = if (questionIndex == questionList?.size!! - 1) View.GONE else View.VISIBLE


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

                val adapter = NewOptionListAdapter(this, examQuestionBean.db, optionListView, examQuestionBean.qtype, true,
                        examQuestionBean.skey,examQuestionBean.useR_ANSWER)
                optionListView.divider = null
                optionListView.dividerHeight = 30
                optionListView.adapter = adapter
                //设置默认选中
                var userAnswer = questionList!![questionIndex].useR_ANSWER
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
                val answerList = mutableListOf<String>()
                for (i in 0 until examQuestionBean.db!!.size) {
                    answerList.add(i, "")
                }
                optionListView.setOnItemClickListener { _, _, i, _ ->
                    if (TextUtils.equals("2", examQuestionBean.qtype)) {
                        val checkedItemPositions: SparseBooleanArray = optionListView.checkedItemPositions
                        val isChecked = checkedItemPositions.get(i)
                        if (isChecked) {
                            answerList[i] = examQuestionBean.db[i].salisa
                        } else {
                            answerList[i] = ""
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
                val dividerHigh = optionListView.dividerHeight * (adapter.count - 1)
                layoutParams.height = totalHeight + dividerHigh
                queBody.addView(optionListView, layoutParams)
            }

            "4", "5" -> {
                val answerContent = layoutInflater.inflate(R.layout.stu_que_edit_content, queBody.parent as ViewGroup, false)
                answerContent.editContent.setText(questionList!![questionIndex].useR_ANSWER)
                answerContent.editContent.isEnabled = false
                queBody.removeAllViews()
                queBody.addView(answerContent)
            }
        }

        when {
            TextUtils.equals(examQuestionBean.rw,right) -> {
                tvKey.visibility = View.GONE
                tvStatus.text = "回答正确!"
                tvStatus.visibility = View.VISIBLE
            }
            TextUtils.equals(examQuestionBean.rw,wrong) -> {
                tvStatus.text = "回答错误!"
                tvKey.text = "正确答案: " + examQuestionBean.skey
                tvStatus.visibility = View.VISIBLE
                tvKey.visibility = if (TextUtils.isEmpty(examQuestionBean.skey)) View.GONE else View.VISIBLE
            }
            TextUtils.equals(examQuestionBean.rw,notPiGai) -> {
                tvStatus.text = "此题未批改"
                tvStatus.visibility = View.VISIBLE
            }
        }
    }

    /**
     * 显示题型、分数、说明、题数
     */
    private fun showQuestionTopTitle() {
        val text = String.format("%d/%d", questionIndex + 1, questionList?.size)
        val spannableString = SpannableString(text)
        val color = resources.getColor(R.color.dj_mian_text_color)
        val length = getNumberLength(questionIndex + 1)
        spannableString.setSpan(ForegroundColorSpan(color), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        spannableString.setSpan(AbsoluteSizeSpan(28, true), 0, length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        queIndex.text = spannableString
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
            newContent = newContent.replace(aa, "")
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

        if (newContent.contains(ee)){
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