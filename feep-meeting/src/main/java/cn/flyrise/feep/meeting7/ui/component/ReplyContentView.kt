package cn.flyrise.feep.meeting7.ui.component

import android.content.Context
import android.text.Editable
import android.text.Selection
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import cn.flyrise.feep.core.common.utils.PreferencesUtils
import cn.flyrise.feep.core.common.utils.SpUtil
import cn.flyrise.feep.meeting7.R

/**
 * @author ZYP
 * @since 2018-07-02 16:39
 */
class ReplyContentView : LinearLayout {

    private val mAttachmentBtn: View
    private val mTvTextSize: TextView
    private val mTvReplySubmit: TextView
    private val mEtReplyContent: EditText
    private val mTvAttachmentSize: TextView
    private val mIvAttachmentIcon: ImageView
    private val mTvTitle: TextView
    private val mIvClose: ImageView

    private var mRecordButtonClickListener: View.OnClickListener? = null
    private var mKeyListener: View.OnKeyListener? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        View.inflate(context, R.layout.nms_dialog_reply_window, this)
        mAttachmentBtn = findViewById(R.id.coreLayoutAttachments)

        mTvAttachmentSize = findViewById(R.id.coreTvAttachmentSize)
        mEtReplyContent = findViewById(R.id.coreEtContent)
        mTvReplySubmit = findViewById(R.id.coreTvSubmit)
        mTvTextSize = findViewById(R.id.coreTvErrorPrompt)
        mIvAttachmentIcon = findViewById(R.id.coreIvAttachment)
        mTvTitle = findViewById(R.id.nmsTvTitle)
        mIvClose = findViewById(R.id.nmsIvX)

        mEtReplyContent.isFocusable = true
        mEtReplyContent.isFocusableInTouchMode = true

        mEtReplyContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                var editable = mEtReplyContent.text
                val len = mEtReplyContent.length()
                if (len > 120) {
                    var selEndIndex = Selection.getSelectionEnd(editable)
                    val str = editable.toString()
                    val newStr = str.substring(0, 120)
                    mEtReplyContent.setText(newStr)
                    editable = mEtReplyContent.text

                    val newLen = editable.length
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length
                    }

                    Selection.setSelection(editable, selEndIndex)
                    val text = String.format("意见不能超过120个字")
                    if (!TextUtils.isEmpty(text)) {
                        mTvTextSize.visibility = View.VISIBLE
                        mTvTextSize.text = text
                    }
                } else {
                    mTvTextSize.visibility = View.GONE
                }

                SpUtil.put(PreferencesUtils.SAVE_REPLY_DATA, editable.toString())
            }
        })

        val reply = SpUtil.get(PreferencesUtils.SAVE_REPLY_DATA, "")
        if (!TextUtils.isEmpty(reply)) {
            mEtReplyContent.setText(reply)
            mEtReplyContent.setSelection(reply!!.length)
        }
    }

    fun setTitle(title: String) {
        mTvTitle?.setText(title)
    }

    fun setContent(content: String?) {
        mEtReplyContent?.setText(content)
        mEtReplyContent?.setSelection(content?.length ?: 0)
    }

    fun setHint(hint: String?) {
        if (!TextUtils.isEmpty(hint)) {
            mEtReplyContent?.hint = hint
        }
    }

    fun setAttachmentClickListener(listener: View.OnClickListener) {
        mAttachmentBtn?.setOnClickListener(listener)
    }

    fun setSubmitClickListener(listener: View.OnClickListener) {
        this.mTvReplySubmit?.setOnClickListener(listener)
    }

    fun setCloseClickListener(listener: OnClickListener) {
        this.mIvClose?.setOnClickListener(listener)
    }

    fun setAttachmentSize(size: Int) {
        if (size > 0) {
            mIvAttachmentIcon.setImageResource(R.mipmap.nms_ic_attachment_enable)
            mTvAttachmentSize.setVisibility(View.VISIBLE)
            mTvAttachmentSize.setText(size.toString() + "")
            return
        }
        mTvAttachmentSize.setVisibility(View.GONE)
        mIvAttachmentIcon.setImageResource(R.mipmap.nms_ic_attachment_unable)
    }

    fun getEditText(): EditText? {
        return this.mEtReplyContent
    }

    fun getContent(): String {
        val replyContent = mEtReplyContent.text.toString()
        return replyContent.trim { it <= ' ' }
    }

    fun setSubmitEnable(isEnable: Boolean) {
        if (!TextUtils.isEmpty(mEtReplyContent.text) && !isEnable) {
            return
        }
        mTvReplySubmit.isEnabled = isEnabled
        mTvReplySubmit.isClickable = isEnable
        if (isEnable) {
            mTvReplySubmit.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_enable)
        } else {
            mTvReplySubmit.setBackgroundResource(R.drawable.nms_bg_blue_btn_round_unable)
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
            if (mKeyListener != null) {
                mKeyListener!!.onKey(this, KeyEvent.KEYCODE_BACK, event)
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun setOnKeyListener(keyListener: View.OnKeyListener) {
        mKeyListener = keyListener
        super.setOnKeyListener(keyListener)
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK || event.keyCode == KeyEvent.KEYCODE_SETTINGS) {
            if (mKeyListener != null) {
                mKeyListener!!.onKey(this, KeyEvent.KEYCODE_BACK, event)
                return true
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }
}