package cn.flyrise.feep.location.dialog

import android.app.Dialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.LayoutInflater
import cn.flyrise.feep.R

/**
 * 新建：陈冕;
 *日期： 2018-9-6-11:12.
 */
class SignInLoadingHint {

    private var mLoadingDialog: Dialog? = null
    private var mKeyDownListener: (() -> Unit)? = null
    private var mDismissListener: (() -> Unit)? = null
    private var mContext: Context? = null

    fun showDialog(context: Context, isClick: Boolean) {
        mContext = context
        if (mLoadingDialog != null) {
            mLoadingDialog!!.show()
            return
        }
        if (mContext is AppCompatActivity && (mContext as AppCompatActivity).isFinishing) return
        mLoadingDialog = Dialog(context, R.style.TransparentDialogStyle)
        val view = LayoutInflater.from(context).inflate(R.layout.core_view_loading_dialog, null)
        mLoadingDialog!!.setContentView(view)
        mLoadingDialog!!.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                mKeyDownListener?.invoke()
                mKeyDownListener = null
                mLoadingDialog?.hide()
            }
            false
        }                    //监听返回键，主要用于上传附件时关闭线程
        mLoadingDialog!!.setCanceledOnTouchOutside(isClick)                  //设置点击屏幕dialog不消失
        mLoadingDialog!!.setOnDismissListener { mDismissListener?.invoke() }
        mLoadingDialog!!.show()
    }

    fun hide() {
        if (mContext is AppCompatActivity && (mContext as AppCompatActivity).isFinishing) return
        mLoadingDialog?.dismiss()
    }

    fun setOnKeyDownListener(listener: () -> Unit) {
        mKeyDownListener = listener
    }

    fun setOnDismissListener(listener: () -> Unit) {
        mDismissListener = listener
    }
}