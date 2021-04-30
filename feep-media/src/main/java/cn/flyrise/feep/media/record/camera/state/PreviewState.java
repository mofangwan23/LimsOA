package cn.flyrise.feep.media.record.camera.state;

import android.graphics.Bitmap;
import android.view.Surface;
import android.view.SurfaceHolder;
import cn.flyrise.feep.media.record.camera.CameraInterface;
import cn.flyrise.feep.media.record.camera.JCameraView;

class PreviewState implements State {

	private CameraMachine machine;

	PreviewState(CameraMachine machine) {
		this.machine = machine;
	}

	@Override
	public void start(SurfaceHolder holder, float screenProp) {
		CameraInterface.getInstance().doStartPreview(holder, screenProp);
	}

	@Override
	public void stop() {
		CameraInterface.getInstance().doStopPreview();
	}


	@Override
	public void foucs(float x, float y, CameraInterface.FocusCallback callback) {
		if (machine.getView().handlerFoucs(x, y)) {
			CameraInterface.getInstance().handleFocus(machine.getContext(), x, y, callback);
		}
	}

	@Override
	public void swtich(SurfaceHolder holder, float screenProp) {
		CameraInterface.getInstance().switchCamera(holder, screenProp);
	}

	@Override
	public void restart() {

	}

	@Override
	public void capture() {
		CameraInterface.getInstance().takePicture(new CameraInterface.TakePictureCallback() {
			@Override
			public void captureResult(Bitmap bitmap, boolean isVertical) {
				machine.getView().showPicture(bitmap, isVertical);
				machine.setState(machine.getBorrowPictureState());
			}
		});
	}

	@Override
	public void record(Surface surface, float screenProp) {
		CameraInterface.getInstance().startRecord(surface, screenProp);
	}

	@Override
	public void stopRecord(final boolean isShort, long time) {
		CameraInterface.getInstance().stopRecord(isShort, new CameraInterface.StopRecordCallback() {
			@Override
			public void recordResult(String url, Bitmap firstFrame) {
				if (isShort) {
					machine.getView().resetState(JCameraView.TYPE_SHORT);
				}
				else {
					machine.getView().playVideo(firstFrame, url);
					machine.setState(machine.getBorrowVideoState());
				}
			}
		});
	}

	@Override
	public void cancle(SurfaceHolder holder, float screenProp) {
//        .i("浏览状态下,没有 cancle 事件");
	}

	@Override
	public void confirm() {
//        .i("浏览状态下,没有 confirm 事件");
	}

	@Override
	public void zoom(float zoom, int type) {
		CameraInterface.getInstance().setZoom(zoom, type);
	}

	@Override
	public void flash(String mode) {
		CameraInterface.getInstance().setFlashMode(mode);
	}
}
