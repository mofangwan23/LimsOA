/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.chatui.ui;

import android.Manifest;
import android.Manifest.permission;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import com.hyphenate.chat.EMCallManager.EMCameraDataProcessor;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMVideoCallHelper;
import com.hyphenate.chatui.R;
import com.hyphenate.chatui.utils.EmHelper;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.utils.EaseUserUtils;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.media.EMCallSurfaceView;
import com.superrtc.sdk.VideoView;

/**
 * 视频通话页面
 */
public class VideoCallActivity extends CallActivity implements OnClickListener {

	private boolean isMuteState;
	private boolean isHandsfreeState;
	private boolean isAnswered;
	private boolean endCallTriggerByMe = false;
	private boolean monitor = true;

	private TextView callStateTextView;

	private LinearLayout comingBtnContainer;
	private Button refuseBtn;
	private Button answerBtn;
	private Button hangupBtn;
	private ImageView muteImage;
	private ImageView handsFreeImage;

	private Chronometer chronometer;
	private LinearLayout voiceContronlLayout;
	private RelativeLayout rootContainer;
	private LinearLayout bottomContainer;
	private TextView netwrokStatusVeiw;
	private TextView tvPermission;
	private Handler uiHandler;

	private boolean isInCalling;
	boolean isRecording = false;
	private EMVideoCallHelper callHelper;

	private BrightnessDataProcess dataProcessor = new BrightnessDataProcess();

	protected EMCallSurfaceView localSurface;
	protected EMCallSurfaceView oppositeSurface;

	private RelativeLayout mSendLayout;
	private ImageView mSendUserIcon;
	private TextView nickTextView;
	private Handler mHandler = new Handler();

	class BrightnessDataProcess implements EMCameraDataProcessor {

		byte yDelta = 0;

		synchronized void setYDelta(byte yDelta) {
			Log.d("VideoCallActivity", "brigntness uDelta:" + yDelta);
			this.yDelta = yDelta;
		}

		@Override
		public synchronized void onProcessData(byte[] data, Camera camera, final int width, final int height, final int rotateAngel) {
			int wh = width * height;
			for (int i = 0; i < wh; i++) {
				int d = (data[i] & 0xFF) + yDelta;
				d = d < 16 ? 16 : d;
				d = d > 235 ? 235 : d;
				data[i] = (byte) d;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			finish();
			return;
		}
		setContentView(R.layout.em_activity_video_call);

		EmHelper.getInstance().isVideoCalling = true;
		callType = 1;

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

		uiHandler = new Handler();

		callStateTextView = (TextView) findViewById(R.id.tv_call_state);
		comingBtnContainer = (LinearLayout) findViewById(R.id.ll_coming_call);
		rootContainer = (RelativeLayout) findViewById(R.id.root_layout);
		refuseBtn = (Button) findViewById(R.id.btn_refuse_call);
		answerBtn = (Button) findViewById(R.id.btn_answer_call);
		hangupBtn = (Button) findViewById(R.id.btn_hangup_call);
		muteImage = (ImageView) findViewById(R.id.iv_mute);
		handsFreeImage = (ImageView) findViewById(R.id.iv_handsfree);
		callStateTextView = (TextView) findViewById(R.id.tv_call_state);

		nickTextView = (TextView) findViewById(R.id.tv_nick);
		tvPermission = (TextView) findViewById(R.id.tvPermission);

		mSendLayout = (RelativeLayout) findViewById(R.id.ll_top_container);
		mSendUserIcon = (ImageView) findViewById(R.id.swing_card);

		chronometer = (Chronometer) findViewById(R.id.chronometer);
		voiceContronlLayout = (LinearLayout) findViewById(R.id.ll_voice_control);
		bottomContainer = (LinearLayout) findViewById(R.id.ll_bottom_container);
		netwrokStatusVeiw = (TextView) findViewById(R.id.tv_network_status);
		ImageView switchCameraBtn = (ImageView) findViewById(R.id.btn_switch_camera);
		ImageView captureImageBtn = (ImageView) findViewById(R.id.btn_capture_image);
		SeekBar YDeltaSeekBar = (SeekBar) findViewById(R.id.seekbar_y_detal);

		refuseBtn.setOnClickListener(this);
		answerBtn.setOnClickListener(this);
		hangupBtn.setOnClickListener(this);
		muteImage.setOnClickListener(this);
		handsFreeImage.setOnClickListener(this);
		rootContainer.setOnClickListener(this);
		switchCameraBtn.setOnClickListener(this);
		captureImageBtn.setOnClickListener(this);

		YDeltaSeekBar.setOnSeekBarChangeListener(new YDeltaSeekBarListener());

		isInComingCall = getIntent().getBooleanExtra("isComingCall", false);
		username = getIntent().getStringExtra("username");

		nickTextView.setText(username);

		EaseUserUtils.setUserNick(username, nickTextView);

		EaseUserUtils.setUserAvatar(VideoCallActivity.this, username, mSendUserIcon);

		// local surfaceview
		localSurface = (EMCallSurfaceView) findViewById(R.id.local_surface);
		localSurface.setZOrderMediaOverlay(true);
		localSurface.setZOrderOnTop(true);

		oppositeSurface = (EMCallSurfaceView) findViewById(R.id.opposite_surface);

		addCallStateListener();
		if (!isInComingCall) {// outgoing call
			soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
			outgoing = soundPool.load(this, R.raw.em_outgoing, 1);

			comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			String st = getResources().getString(R.string.Are_connected_to_each_other);
			callStateTextView.setText(st);
			EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
			oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
			handler.sendEmptyMessage(MSG_CALL_MAKE_VIDEO);
			handler.postDelayed(() -> streamID = playMakeCallSounds(), 300);
			sendCmdCallMessage(EmChatContent.CMD_ACTION_PC_VIDEO_CALL, username);
		}
		else {

			if (EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.IDLE
					|| EMClient.getInstance().callManager().getCallState() == EMCallStateChangeListener.CallState.DISCONNECTED) {
				finish();
				return;
			}
			voiceContronlLayout.setVisibility(View.INVISIBLE);
			localSurface.setVisibility(View.INVISIBLE);
			Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
			audioManager.setMode(AudioManager.MODE_RINGTONE);
			audioManager.setSpeakerphoneOn(true);
			try {
				ringtone = RingtoneManager.getRingtone(this, ringUri);
				if (ringtone == null) finish();
				ringtone.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
			EMClient.getInstance().callManager().setSurfaceView(localSurface, oppositeSurface);
			oppositeSurface.setScaleMode(VideoView.EMCallViewScaleMode.EMCallViewScaleModeAspectFill);
		}

		final int MAKE_CALL_TIMEOUT = 50 * 1000;
		handler.removeCallbacks(timeoutHangup);
		handler.postDelayed(timeoutHangup, MAKE_CALL_TIMEOUT);

		callHelper = EMClient.getInstance().callManager().getVideoCallHelper();

		EMClient.getInstance().callManager().setCameraDataProcessor(dataProcessor);

		setAudioPermissions();
	}

	class YDeltaSeekBarListener implements SeekBar.OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			dataProcessor.setYDelta((byte) (20.0f * (progress - 50) / 50.0f));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	/**
	 * set call state listener
	 */
	void addCallStateListener() {
		callStateListener = (callState, error) -> {
			setOppositeSurface(true);
			switch (callState) {
				case CONNECTING: // is connecting
					runOnUiThread(() -> callStateTextView.setText(R.string.Are_connected_to_each_other));
					break;
				case CONNECTED: // connected
					runOnUiThread(() -> callStateTextView.setText(R.string.have_connected_with));
					break;

				case ACCEPTED: // call is accepted
					handler.removeCallbacks(timeoutHangup);
					runOnUiThread(() -> {
						try {
							if (soundPool != null) {
								soundPool.stop(streamID);
							}
						} catch (Exception ignored) {
						}
						openSpeakerOn();
						handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
						isHandsfreeState = true;
						isInCalling = true;
						chronometer.setVisibility(View.VISIBLE);
						chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.start();

						mSendLayout.setVisibility(View.GONE);

						callStateTextView.setText(R.string.In_the_call);
						callingState = CallingState.NORMAL;

						setOppositeSurface(false);
					});
					break;
				case NETWORK_DISCONNECTED:
					runOnUiThread(() -> {
						netwrokStatusVeiw.setVisibility(View.VISIBLE);
						netwrokStatusVeiw.setText(R.string.network_unavailable);
					});
					break;
				case NETWORK_UNSTABLE:
					runOnUiThread(() -> {
						netwrokStatusVeiw.setVisibility(View.VISIBLE);
						if (error == EMCallStateChangeListener.CallError.ERROR_NO_DATA) {
							netwrokStatusVeiw.setText(R.string.no_call_data);
						}
						else {
							netwrokStatusVeiw.setText(R.string.network_unstable);
						}
					});
					break;
				case NETWORK_NORMAL:
					runOnUiThread(() -> netwrokStatusVeiw.setVisibility(View.INVISIBLE));
					break;
				case DISCONNECTED: // call is disconnected
					handler.removeCallbacks(timeoutHangup);
					@SuppressWarnings("UnnecessaryLocalVariable") final EMCallStateChangeListener.CallError fError = error;
					runOnUiThread(new Runnable() {
						private void postDelayedCloseMsg() {
							uiHandler.postDelayed(() -> {
								removeCallStateListener();
								saveCallRecord();
								Animation animation = new AlphaAnimation(1.0f, 0.0f);
								animation.setDuration(1200);
								rootContainer.startAnimation(animation);
								finish();
							}, 200);
						}

						@Override
						public void run() {
							chronometer.stop();
							callDruationText = chronometer.getText().toString();
							String s1 = getResources().getString(R.string.The_other_party_refused_to_accept);
							String s2 = getResources().getString(R.string.Connection_failure);
							String s3 = getResources().getString(R.string.The_other_party_is_not_online);
							String s4 = getResources().getString(R.string.The_other_is_on_the_phone_please);
							String s5 = getResources().getString(R.string.The_other_party_did_not_answer);

							String s6 = getResources().getString(R.string.hang_up);
							String s7 = getResources().getString(R.string.The_other_is_hang_up);
							String s8 = getResources().getString(R.string.did_not_answer);
							String s9 = getResources().getString(R.string.Has_been_cancelled);
							String s10 = getResources().getString(R.string.Refused);

							if (fError == EMCallStateChangeListener.CallError.REJECTED) {
								callingState = CallingState.BEREFUSED;
								callStateTextView.setText(s1);
							}
							else if (fError == EMCallStateChangeListener.CallError.ERROR_TRANSPORT) {
								callStateTextView.setText(s2);
							}
							else if (fError == EMCallStateChangeListener.CallError.ERROR_UNAVAILABLE) {
								callingState = CallingState.OFFLINE;
								callStateTextView.setText(s3);
							}
							else if (fError == EMCallStateChangeListener.CallError.ERROR_BUSY) {
								callingState = CallingState.BUSY;
								callStateTextView.setText(s4);
							}
							else if (fError == EMCallStateChangeListener.CallError.ERROR_NORESPONSE) {
								callingState = CallingState.NO_RESPONSE;
								callStateTextView.setText(s5);
							}
							else if (fError == EMCallStateChangeListener.CallError.ERROR_LOCAL_SDK_VERSION_OUTDATED
									|| fError == EMCallStateChangeListener.CallError.ERROR_REMOTE_SDK_VERSION_OUTDATED) {
								callingState = CallingState.VERSION_NOT_SAME;
								callStateTextView.setText(R.string.call_version_inconsistent);
							}
							else {
								if (isRefused) {
									callingState = CallingState.REFUSED;
									callStateTextView.setText(s10);
								}
								else if (isAnswered) {
									callingState = CallingState.NORMAL;
									if (endCallTriggerByMe) {
									}
									else {
										callStateTextView.setText(s7);
									}
								}
								else {
									if (isInComingCall) {
										callingState = CallingState.UNANSWERED;
										callStateTextView.setText(s8);
									}
									else {
										if (callingState != CallingState.NORMAL) {
											callingState = CallingState.CANCELLED;
											callStateTextView.setText(s9);
										}
										else {
											callStateTextView.setText(s6);
										}
									}
								}
							}
							postDelayedCloseMsg();
						}
					});
					break;
				default:
					break;
			}

		};
		EMClient.getInstance().callManager().addCallStateChangeListener(callStateListener);
	}

	private void setOppositeSurface(boolean isShow) {
		mHandler.postDelayed(() -> {
			if (oppositeSurface != null)
				oppositeSurface.setBackgroundColor(isShow ? Color.parseColor("#000000") : Color.TRANSPARENT);
		}, 1000);
	}

	void removeCallStateListener() {
		EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_refuse_call) {
			isRefused = true;
			refuseBtn.setEnabled(false);
			handler.sendEmptyMessage(MSG_CALL_REJECT);
		}
		else if (v.getId() == R.id.btn_answer_call) {
			answerBtn.setEnabled(false);
			openSpeakerOn();
			if (ringtone != null) {
				ringtone.stop();
			}

			callStateTextView.setText("answering...");
			handler.sendEmptyMessage(MSG_CALL_ANSWER);
			handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
			isAnswered = true;
			isHandsfreeState = true;
			comingBtnContainer.setVisibility(View.INVISIBLE);
			hangupBtn.setVisibility(View.VISIBLE);
			voiceContronlLayout.setVisibility(View.VISIBLE);
			localSurface.setVisibility(View.VISIBLE);
		}
		else if (v.getId() == R.id.btn_hangup_call) {
			FELog.e("Call--> btn_hangup_call onclick");

			hangupBtn.setEnabled(false);
			chronometer.stop();
			endCallTriggerByMe = true;
			callStateTextView.setText(getResources().getString(R.string.hanging_up));
			handler.sendEmptyMessage(MSG_CALL_END);
		}
		else if (v.getId() == R.id.iv_mute) {
			if (isMuteState) {
				// resume voice transfer
				muteImage.setImageResource(R.drawable.em_icon_mute_normal_video);
				try {
					EMClient.getInstance().callManager().resumeVoiceTransfer();
				} catch (HyphenateException e) {
					e.printStackTrace();
				}
				isMuteState = false;
			}
			else {
				muteImage.setImageResource(R.drawable.em_icon_mute_on);
				try {
					EMClient.getInstance().callManager().pauseVoiceTransfer();
				} catch (HyphenateException e) {
					e.printStackTrace();
				}
				isMuteState = true;
			}
		}
		else if (v.getId() == R.id.iv_handsfree) {
			if (isHandsfreeState) {
				handsFreeImage.setImageResource(R.drawable.em_icon_speaker_normal);
				closeSpeakerOn();
				isHandsfreeState = false;
			}
			else {
				handsFreeImage.setImageResource(R.drawable.em_icon_speaker_on);
				openSpeakerOn();
				isHandsfreeState = true;
			}
		}
		else if (v.getId() == R.id.root_layout) {
			if (callingState == CallingState.NORMAL) {
				if (bottomContainer.getVisibility() == View.VISIBLE) {
					bottomContainer.setVisibility(View.GONE);
				}
				else {
					bottomContainer.setVisibility(View.VISIBLE);
				}
			}
		}
		else if (v.getId() == R.id.btn_switch_camera) {
			handler.sendEmptyMessage(MSG_CALL_SWITCH_CAMERA);
		}
//		else if (v.getId() == R.id.btn_capture_image) {
//			DateFormat df = DateFormat.getDateTimeInstance();
//			Date d = new Date();
//			final String filename = Environment.getExternalStorageDirectory() + df.format(d) + ".jpg";
//			EMClient.getInstance().callManager().getVideoCallHelper().takePicture(filename);
//		}
	}


	@Override
	public void finish() {
		super.finish();
	}

	@Override
	protected void onDestroy() {
		EmHelper.getInstance().isVideoCalling = false;
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		onClick(hangupBtn);
	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
		if (isInCalling) {
			try {
				EMClient.getInstance().callManager().pauseVideoTransfer();
			} catch (HyphenateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isInCalling) {
			try {
				EMClient.getInstance().callManager().resumeVideoTransfer();
			} catch (HyphenateException e) {
				e.printStackTrace();
			}
		}
		setTvPermissionText();
	}

	private void setAudioPermissions() {
		FePermissions.with(VideoCallActivity.this)
				.permissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO})
				.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
				.requestCode(PermissionCode.RECORD_AUDIO)
				.request();
	}

	@PermissionGranted(PermissionCode.RECORD_AUDIO)
	public void onAudioPermissionGranted() {
		tvPermission.setVisibility(View.GONE);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		setTvPermissionText();
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	private void setTvPermissionText() {
		boolean hasCamera = FePermissions.checkSelfPermission(this, permission.CAMERA);
		boolean hasAudio = FePermissions.checkSelfPermission(this, permission.RECORD_AUDIO);
		if (hasAudio && hasCamera) {
			tvPermission.setVisibility(View.GONE);
		}
		else if (hasAudio) {
			tvPermission.setVisibility(View.VISIBLE);
			tvPermission.setText(R.string.videoCall_out_camera_permission);
		}
		else if (hasCamera) {
			tvPermission.setVisibility(View.VISIBLE);
			tvPermission.setText(R.string.videoCall_out_voice_permission);
		}
		else {
			tvPermission.setVisibility(View.VISIBLE);
			tvPermission.setText(R.string.videoCall_out_camera_voice_permission);
		}
	}

}
