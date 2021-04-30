package cn.flyrise.feep.location.widget

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import cn.flyrise.feep.core.common.FELog


/**
 * 新建：陈冕;
 *日期： 2018-8-24-9:53.
 */
abstract class BaseSuspensionBar : RelativeLayout {

    private var mCurrentPosition: Int = 0
    private var mSuspensionHeight: Int = 0
    private var viewDisposeHeight: Int = 0
    private var clickY = 0f
    private var mListener: BaseSuspensionBar.NotificationBarDataListener? = null

    protected var mSuspensionBar: RelativeLayout? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun onScrolled(mLayoutManager: LinearLayoutManager) {
        val view = mLayoutManager.findViewByPosition(mCurrentPosition + 1)
        if (view != null) {
            viewDisposeHeight = getSuspensionHeight() - view.top
            if (view.top <= mSuspensionHeight)
                mSuspensionBar?.setY((-viewDisposeHeight).toFloat())
            else
                mSuspensionBar?.setY(0f)
        }

        if (mCurrentPosition != mLayoutManager.findFirstVisibleItemPosition()) {
            val lastPositon = mCurrentPosition
            mCurrentPosition = mLayoutManager.findFirstVisibleItemPosition()
            mListener?.onNotificatiionBarData()
            mSuspensionBar?.setY((if (lastPositon > mCurrentPosition) -height else 0).toFloat())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        FELog.i("--->>>>>month:---d:" + event.action)
        if (event.action == MotionEvent.ACTION_DOWN) {
            clickY = event.y
        } else if (event.action == MotionEvent.ACTION_UP) {
            FELog.i("--->>>>>month:---a:" + (event.y - clickY))
            if (event.y - clickY < 10) {//点击事件，容错
                FELog.i("--->>>>>month:---b:" + event.getY())
                FELog.i("--->>>>>month:---ddd:" + viewDisposeHeight)
                FELog.i("--->>>>>month:---c:" + (if (isPositionAdd(event)) mCurrentPosition + 1 else mCurrentPosition))
                mListener?.onClickBarItem(if (isPositionAdd(event)) mCurrentPosition + 1 else mCurrentPosition)
            }
        }
        return true
    }

    private fun isPositionAdd(event: MotionEvent) = event.y > Math.abs(viewDisposeHeight) && viewDisposeHeight != 0


    fun getPosition(): Int {
        return mCurrentPosition
    }

    fun setSuspensionHeight() {
        if (mSuspensionBar != null) mSuspensionHeight = mSuspensionBar?.getHeight() ?: 0
    }

    fun getSuspensionHeight() = mSuspensionHeight

    fun setNotificationBarDataListener(listener: BaseSuspensionBar.NotificationBarDataListener?) {
        this.mListener = listener
    }

    interface NotificationBarDataListener {

        fun onClickBarItem(position: Int)

        fun onNotificatiionBarData()
    }
}