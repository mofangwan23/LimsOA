package cn.flyrise.feep.workplan7.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import cn.flyrise.feep.R
import kotlinx.android.synthetic.main.particular_edit_text_layout.view.*

/**
 * 新建：陈冕;
 *日期： 2018-7-5-16:05.
 * 字符数提示在下面
 */
class ParticularEditText : RelativeLayout {

    private var numTitleMax = 80

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        LayoutInflater.from(context).inflate(R.layout.particular_edit_text_layout, this)
        etContent.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            @SuppressLint("SetTextI18n")
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var editable: Editable = etContent.getText()
                if (editable.length > numTitleMax) {
                    var selEndIndex: Int = Selection.getSelectionEnd(editable)
                    etContent.setText(editable.toString().substring(0, numTitleMax)) //截取新字符串
                    editable = etContent.getText()
                    //新字符串的长度
                    val newLen = editable.length
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) selEndIndex = editable.length
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex)
                } else {
                    tvTextCount.setText("${editable.length}/$numTitleMax")
                }
            }

            override fun afterTextChanged(s: Editable) {}
        })
    }

    fun getText() = etContent.text.toString().trim()

    fun setMaxNums(nums: Int) {
        this.numTitleMax = nums
    }

    fun setText(text: String?) {
        if(TextUtils.isEmpty(text))return
        this.etContent.setText(text)
    }
}