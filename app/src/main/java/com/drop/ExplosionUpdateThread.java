package com.drop;

import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;

/**
 * The thead will finish when you called setRunning(false) or Explosion ended.
 * 
 * @author Peter.Ding
 * 
 */
public class ExplosionUpdateThread extends Thread {
	private SurfaceHolder mHolder;
	private DropCover mDropCover;
	private boolean isRunning = false;
	private final int CANCLE_DROW = 10210;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (CANCLE_DROW == msg.what) {
				mDropCover.clearViews();
			}
		};

	};

	public ExplosionUpdateThread(SurfaceHolder holder, DropCover dropCover) {
		mHolder = holder;
		mDropCover = dropCover;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	@Override
	public void run() {
		boolean isAlive = true;
		while (isRunning && isAlive) {
			Canvas canvas = mHolder.lockCanvas();
			if (canvas != null) {
				isAlive = mDropCover.render(canvas);
				mHolder.unlockCanvasAndPost(canvas);
				mDropCover.update();
			}
		}
		if (handler != null) {
			handler.sendEmptyMessage(CANCLE_DROW);
		}
	}
}
