package com.hyphenate.chatui.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEStatusBar;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMCallManager;
import com.hyphenate.chat.EMCallStateChangeListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.utils.MMPMessageUtil;
import com.hyphenate.exceptions.EMServiceNotReadyException;
import com.hyphenate.util.EMLog;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("Registered")
public class CallActivity extends AppCompatActivity {

	public final static String TAG = "CallActivity";
	protected final int MSG_CALL_MAKE_VIDEO = 0;
	protected final int MSG_CALL_MAKE_VOICE = 1;
	protected final int MSG_CALL_ANSWER = 2;
	protected final int MSG_CALL_REJECT = 3;
	protected final int MSG_CALL_END = 4;
	protected final int MSG_CALL_RLEASE_HANDLER = 5;
	protected final int MSG_CALL_SWITCH_CAMERA = 6;

	protected boolean isInComingCall;
	protected boolean isRefused = false;
	protected String username;
	protected CallingState callingState = CallingState.CANCELLED;
	protected String callDruationText;
	protected AudioManager audioManager;
	protected SoundPool soundPool;
	protected Ringtone ringtone;
	protected int outgoing;
	protected EMCallStateChangeListener callStateListener;
	protected boolean isAnswered = false;
	protected int streamID = -1;

	EMCallManager.EMCallPushProvider pushProvider;

	/**
	 * 0：voice call，1：video call
	 */
	protected int callType = 0;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		FEStatusBar.setupStatusBar(getWindow(), Color.TRANSPARENT);
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

		pushProvider = new EMCallManager.EMCallPushProvider() {

			void updateMessageText(final EMMessage oldMsg, final String to) {
				// update local message text
				EMConversation conv = EMClient.getInstance().chatManager().getConversation(oldMsg.getTo());
				conv.removeMessage(oldMsg.getMsgId());
			}

			@Override
			public void onRemoteOffline(final String to) {

				//this function should exposed & move to Demo
				EMLog.d(TAG, "onRemoteOffline, to:" + to);

				final EMMessage message = EMMessage.createTxtSendMessage(getResources().getString(R.string.incoming_call), to);
				message.setAttribute(callType == 0 ? EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VOICE_CALL
						: EaseUiK.EmChatContent.MESSAGE_ATTR_IS_VIDEO_CALL, true);
				// set the user-defined extension field
				message.setAttribute("em_apns_ext", true);
//				message.setAttribute("is_voice_call", callType);
				message.setAttribute("em_apns_ext",
						getOfflineMessageJson(String.format(getResources().getString(R.string.incoming_call_hint)
								, CoreZygote.getLoginUserServices().getUserName())));// 将推送扩展设置到消息中

				message.setMessageStatusCallback(new EMCallBack() {

					@Override
					public void onSuccess() {
						EMLog.d(TAG, "onRemoteOffline success");
						updateMessageText(message, to);
					}

					@Override
					public void onError(int code, String error) {
						EMLog.d(TAG, "onRemoteOffline Error");
						updateMessageText(message, to);
					}

					@Override
					public void onProgress(int progress, String status) {
					}
				});
				// send messages
				EMClient.getInstance().chatManager().sendMessage(message);
			}
		};

		EMClient.getInstance().callManager().setPushProvider(pushProvider);
	}

	private static JSONObject getOfflineMessageJson(String text) {// 将推送扩展设置到消息中
		// 设置自定义推送提示
		JSONObject extObject = new JSONObject();
		try {
			extObject.put("em_push_name", "食药协作平台");
			extObject.put("em_push_content", text);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return extObject;
	}

	@Override
	protected void onDestroy() {
		if (soundPool != null) {
			soundPool.release();
		}
		if (ringtone != null && ringtone.isPlaying()) {
			ringtone.stop();
		}
		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setMicrophoneMute(false);

		if (callStateListener != null) {
			EMClient.getInstance().callManager().removeCallStateChangeListener(callStateListener);
		}

		if (pushProvider != null) {
			EMClient.getInstance().callManager().setPushProvider(null);
			pushProvider = null;
		}
		releaseHandler();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		handler.sendEmptyMessage(MSG_CALL_END);
		saveCallRecord();
		finish();
		super.onBackPressed();
	}

	Runnable timeoutHangup = new Runnable() {

		@Override
		public void run() {
			handler.sendEmptyMessage(MSG_CALL_END);
		}
	};

	HandlerThread callHandlerThread = new HandlerThread("callHandlerThread");

	{
		callHandlerThread.start();
	}

	protected Handler handler = new Handler(callHandlerThread.getLooper()) {
		@Override
		public void handleMessage(Message msg) {
			EMLog.d("EMCallManager CallActivity", "handleMessage ---enter block--- msg.what:" + msg.what);
			switch (msg.what) {
				case MSG_CALL_MAKE_VIDEO:
				case MSG_CALL_MAKE_VOICE:
					try {
						if (msg.what == MSG_CALL_MAKE_VIDEO) {
							EMClient.getInstance().callManager().makeVideoCall(username);
						}
						else {
							EMClient.getInstance().callManager().makeVoiceCall(username);
						}
					} catch (final EMServiceNotReadyException e) {
						e.printStackTrace();
						runOnUiThread(() -> {
							String st2 = e.getMessage();
							if (e.getErrorCode() == EMError.CALL_REMOTE_OFFLINE) {
								st2 = getResources().getString(R.string.The_other_is_not_online);
							}
							else if (e.getErrorCode() == EMError.USER_NOT_LOGIN) {
								st2 = getResources().getString(R.string.Is_not_yet_connected_to_the_server);
							}
							else if (e.getErrorCode() == EMError.INVALID_USER_NAME) {
								st2 = getResources().getString(R.string.illegal_user_name);
							}
							else if (e.getErrorCode() == EMError.CALL_BUSY) {
								st2 = getResources().getString(R.string.The_other_is_on_the_phone);
							}
							else if (e.getErrorCode() == EMError.NETWORK_ERROR) {
								st2 = getResources().getString(R.string.can_not_connect_chat_server_connection);
							}
							Toast.makeText(CallActivity.this, st2, Toast.LENGTH_SHORT).show();
							finish();
						});
					}
					break;
				case MSG_CALL_ANSWER:
					EMLog.d(TAG, "MSG_CALL_ANSWER");
					if (ringtone != null) {
						ringtone.stop();
					}
					if (isInComingCall) {
						try {
							EMClient.getInstance().callManager().answerCall();
							isAnswered = true;
							// meizu MX5 4G, hasDataConnection(context) return status is incorrect
							// MX5 con.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected() return false in 4G
							// so we will not judge it, App can decide whether judge the network status

//                        if (NetUtils.hasDataConnection(CallActivity.this)) {
//                            EMClient.getInstance().callManager().answerCall();
//                            isAnswered = true;
//                        } else {
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    final String st2 = getResources().getString(R.string.Is_not_yet_connected_to_the_server);
//                                    Toast.makeText(CallActivity.this, st2, Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                            throw new Exception();
//                        }
						} catch (Exception e) {
							e.printStackTrace();
							saveCallRecord();
							finish();
							return;
						}
					}
					else {
						EMLog.d(TAG, "answer call isInComingCall:false");
					}
					break;
				case MSG_CALL_REJECT:
					if (ringtone != null) {
						ringtone.stop();
					}
					try {
						EMClient.getInstance().callManager().rejectCall();
					} catch (Exception e1) {
						e1.printStackTrace();
						saveCallRecord();
						finish();
					}
					callingState = CallingState.REFUSED;
					break;
				case MSG_CALL_END:
					if (soundPool != null) {
						soundPool.stop(streamID);
					}
					try {
						EMClient.getInstance().callManager().endCall();
					} catch (Exception e) {
						FELog.e("Call--> MSG_CALL_END ");
						saveCallRecord();
						finish();
					}

					break;
				case MSG_CALL_RLEASE_HANDLER:
					try {
						EMClient.getInstance().callManager().endCall();
					} catch (Exception e) {
					}
					handler.removeCallbacks(timeoutHangup);
					handler.removeMessages(MSG_CALL_MAKE_VIDEO);
					handler.removeMessages(MSG_CALL_MAKE_VOICE);
					handler.removeMessages(MSG_CALL_ANSWER);
					handler.removeMessages(MSG_CALL_REJECT);
					handler.removeMessages(MSG_CALL_END);
					callHandlerThread.quit();
					break;
				case MSG_CALL_SWITCH_CAMERA:
					EMClient.getInstance().callManager().switchCamera();
					break;
				default:
					break;
			}
			EMLog.d("EMCallManager CallActivity", "handleMessage ---exit block--- msg.what:" + msg.what);
		}
	};

	void releaseHandler() {
		handler.sendEmptyMessage(MSG_CALL_RLEASE_HANDLER);
	}

	/**
	 * play the incoming call ringtone
	 */
	protected int playMakeCallSounds() {
		try {
			audioManager.setMode(AudioManager.MODE_RINGTONE);
			audioManager.setSpeakerphoneOn(false);

			// play
			int id = soundPool.play(outgoing, // sound resource
					0.3f, // left volume
					0.3f, // right volume
					1,    // priority
					-1,   // loop，0 is no loop，-1 is loop forever
					1);   // playback rate (1.0 = normal playback, range 0.5 to 2.0)
			return id;
		} catch (Exception e) {
			return -1;
		}
	}

	protected void openSpeakerOn() {
		try {
			if (!audioManager.isSpeakerphoneOn()) {
				audioManager.setSpeakerphoneOn(true);
			}
			audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void closeSpeakerOn() {

		try {
			if (audioManager != null) {
				// int curVolume =
				// mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
				if (audioManager.isSpeakerphoneOn()) {
					audioManager.setSpeakerphoneOn(false);
				}
				audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
				// mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
				// curVolume, AudioManager.STREAM_VOICE_CALL);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * save call record
	 */
	protected void saveCallRecord() {

		String hint;
		String st1 = getResources().getString(R.string.call_duration);
		String st2 = getResources().getString(R.string.Refused);
		String st3 = getResources().getString(R.string.The_other_party_has_refused_to);
		String st4 = getResources().getString(R.string.The_other_is_not_online);
		String st5 = getResources().getString(R.string.The_other_is_on_the_phone);
		String st6 = getResources().getString(R.string.The_other_party_did_not_answer);
		String st7 = getResources().getString(R.string.did_not_answer);
		String st8 = getResources().getString(R.string.Has_been_cancelled);
		switch (callingState) {
			case NORMAL:
				hint = st1 + callDruationText;
				break;
			case REFUSED:
				hint = st2;
				break;
			case BEREFUSED:
				hint = st3;
				break;
			case OFFLINE:
				hint = st4;
				break;
			case BUSY:
				hint = st5;
				break;
			case NO_RESPONSE:
				hint = st6;
				break;
			case UNANSWERED:
				hint = st7;
				break;
			case VERSION_NOT_SAME:
				hint = getString(R.string.call_version_inconsistent);
				break;
			default:
				hint = (st8);
				break;
		}

		MMPMessageUtil.saveCallMsg(callType == 0, !isInComingCall, username, -1, hint);

		//发送一条消息给对方让对方知道自己没有接听到消息。
		if (callingState == CallingState.OFFLINE) {
			EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
			String action = callType == 0 ? EaseUiK.EmChatContent.CMD_ACTION_VOICECALL : EmChatContent.CMD_ACTION_VIDEOCALL;
			EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
			cmdMsg.setTo(username);
			cmdMsg.addBody(cmdBody);
			EMClient.getInstance().chatManager().sendMessage(cmdMsg);
		}
	}

	protected void sendCmdCallMessage(String action,String toName) {
		EMMessage cmdMsg = EMMessage.createSendMessage(EMMessage.Type.CMD);
		EMCmdMessageBody cmdBody = new EMCmdMessageBody(action);
		cmdMsg.setChatType(ChatType.GroupChat);
		cmdMsg.setTo(toName);
		cmdMsg.addBody(cmdBody);
		EMClient.getInstance().chatManager().sendMessage(cmdMsg);
	}

	enum CallingState {
		CANCELLED, NORMAL, REFUSED, BEREFUSED, UNANSWERED, OFFLINE, NO_RESPONSE, BUSY, VERSION_NOT_SAME
	}

}
