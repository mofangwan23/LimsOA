package cn.flyrise.feep.media.record;

import static java.lang.System.currentTimeMillis;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.media.R;
import cn.squirtlez.frouter.annotations.ResultExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.io.File;
import java.lang.ref.WeakReference;

/**
 * @author ZYP
 * @since 2017-10-31 10:14
 * 录音器
 */
@Route("/media/recorder")
@ResultExtras({
		"SelectionData"     // 录音成功之后，录音文件的 path，String 类型
})
public class RecordActivity extends BaseActivity {

	private static final int CODE_RECORDING = 1;
	private static final int CODE_STOP_RECORD = 2;

	private ImageView mIvRecord;
	private TextView mTvRecordTimer;
	private TextView mTvRecordCancel;

	private long mStartTime;
	private WeakHandler mWeakHandler;

	private boolean isRecording;
	private File mTempRecordFile;
	private ExtAudioRecorder mRecorder;

	private static class WeakHandler extends Handler {

		private WeakReference<RecordActivity> mWeakInstance;

		public WeakHandler(RecordActivity activity) {
			mWeakInstance = new WeakReference<>(activity);
		}

		@Override public void handleMessage(Message msg) {
			RecordActivity activity = mWeakInstance.get();
			if (activity == null) {
				return;
			}

			if (msg.what == CODE_RECORDING) {
				long currentTime = System.currentTimeMillis();
				String time = DateUtils.formatElapsedTime((currentTime - activity.mStartTime) / 1000);
				activity.mTvRecordTimer.setText(time);
				sendEmptyMessageDelayed(CODE_RECORDING, 1000);
			}
			else {
				removeCallbacksAndMessages(null);
			}
		}
	}

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ms_activity_recorder);
	}


	@Override public void bindData() {
		File tempFileDir = new File(CoreZygote.getPathServices().getTempFilePath());
		if (!tempFileDir.exists()) {
			tempFileDir.mkdirs();
		}

		try {
			mRecorder = ExtAudioRecorder.getInstanse();
			mTempRecordFile = File.createTempFile("MUSIC_", ".wav", tempFileDir);
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		mWeakHandler = new WeakHandler(this);
	}

	@Override public void bindView() {
		mIvRecord = (ImageView) findViewById(R.id.msIvRecord);
		mTvRecordTimer = (TextView) findViewById(R.id.msTvRecordTimer);
		mTvRecordCancel = (TextView) findViewById(R.id.msTvRecordCancel);

		// 按下返回键跟取消是一样的
		mTvRecordCancel.setOnClickListener(v -> {
			if (!isRecording) {
				finish();
				return;
			}
			askForSubmit();
		});

		mIvRecord.setOnClickListener(v -> {
			if (isRecording) {
				mIvRecord.setImageResource(R.mipmap.ms_icon_record_start);
				mWeakHandler.sendEmptyMessage(CODE_STOP_RECORD);
				finishRecord();
			}
			else {
				mIvRecord.setImageResource(R.mipmap.ms_icon_record_stop);                // 开始录音
				if (!isRecordAvailable()) {                                              // 不支持录音
					FEToast.showMessage("录音权限已禁止，请进入系统设置开启权限");
					return;
				}
				mRecorder.setOutputFile(mTempRecordFile.getAbsolutePath());
				mRecorder.prepare();
				mRecorder.start();
				mStartTime = currentTimeMillis();    // 记录开始时间
				mWeakHandler.sendEmptyMessageDelayed(CODE_RECORDING, 1000);
			}
			isRecording = !isRecording;
		});
	}

	private void finishRecord() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setCancelable(false)
				.setMessage("语音录入成功，确定发送？")
				.setPositiveButton(null, dialog -> {
					if (!mTempRecordFile.exists() || mTempRecordFile.length() <= 0 || TextUtils
							.equals("00:00", mTvRecordTimer.getText().toString())) {
						FEToast.showMessage(getString(R.string.record_size_error));
						finish();
						return;
					}
					String recordFile = mTempRecordFile.getPath();
					Intent data = new Intent();
					data.putExtra("Record", recordFile);
					data.putExtra("RecordTime", mTvRecordTimer.getText().toString());
					setResult(Activity.RESULT_OK, data);
					finish();
				})
				.setNegativeButton(null, dialog -> {
					if (mTempRecordFile.exists()) { // 清理垃圾文件
						mTempRecordFile.delete();
					}
					finish();
				})
				.build()
				.show();
	}

	/**
	 * 检查是否录音是否可用：系统录音正在进行录用，这时候 app 是用不了录音的。
	 */
	private boolean isRecordAvailable() {
		int bufferSizeInBytes = 0;
		bufferSizeInBytes = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes);
		try {
			audioRecord.startRecording();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}

		if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
			return false;
		}

		audioRecord.stop();
		audioRecord.release();
		return true;
	}

	@Override public void onBackPressed() {
		if (!isRecording) {
			super.onBackPressed();
			return;
		}
		askForSubmit();
	}

	private void askForSubmit() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage("退出后将不进行保存，确定退出？")
				.setPositiveButton(null, dialog -> {
					if (mTempRecordFile.exists()) {
						mTempRecordFile.delete();
					}
					finish();
				})
				.setNegativeButton("继续录音", null)
				.build()
				.show();
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}
}
