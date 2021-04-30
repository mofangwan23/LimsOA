package cn.flyrise.feep.media.attachments;

import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import java.lang.ref.WeakReference;

/**
 * @author ZYP
 * @since 2017-10-26 09:28
 */
public class AudioPlayer extends DialogFragment {

	private static final int PLAYING = 1;
	private static final int STOP = 2;
	private static final int COMPLETED = 3;

	private TextView mTvAudioTitle;
	private TextView mTvProgress;
	private TextView mTvDuration;
	private ImageView mIvAudioSwitch;
	private SeekBar mProgressBar;

	private boolean isPlaying = false;
	private boolean isDrag = false;
	private Attachment mAttachment;
	private String mAudioPath;          // 如果 audioPath 为空，则使用 attachment.path

	private MediaPlayer mMediaPlayer;
	private WeakHandler mWeakHandler;

	private static class WeakHandler extends Handler {

		private WeakReference<AudioPlayer> weakInstance;

		public WeakHandler(AudioPlayer audioPlayer) {
			this.weakInstance = new WeakReference<>(audioPlayer);
		}

		@Override public void handleMessage(Message msg) {
			AudioPlayer player = weakInstance.get();
			if (player == null || player.mMediaPlayer == null) {
				return;
			}

			if (msg.what == PLAYING) {
				int currentProgress = player.mMediaPlayer.getCurrentPosition();
				player.mProgressBar.setProgress(currentProgress);
				player.mTvProgress.setText(DateUtils.formatElapsedTime(currentProgress / 1000));
				if (!player.isDrag) {
					player.mWeakHandler.sendEmptyMessageDelayed(PLAYING, 1000);
				}
			}
			else if (msg.what == STOP) {
				removeCallbacksAndMessages(null);
			}
			else if (msg.what == COMPLETED) {
				removeCallbacksAndMessages(null);
				player.mMediaPlayer.seekTo(0);
				player.mProgressBar.setProgress(0);
				player.mTvProgress.setText(DateUtils.formatElapsedTime(0));
			}
		}
	}

	public static AudioPlayer newInstance(Attachment attachment, String audioPath) {
		AudioPlayer instance = new AudioPlayer();
		instance.mAttachment = attachment;
		instance.mAudioPath = audioPath;
		return instance;
	}

	@Nullable @Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View contentView = inflater.inflate(R.layout.ms_fragment_audio_player, container, false);
		bindView(contentView);
		return contentView;
	}

	private void bindView(View contentView) {
		mTvAudioTitle = (TextView) contentView.findViewById(R.id.msTvAudioTitle);
		mTvProgress = (TextView) contentView.findViewById(R.id.msTvProgress);
		mTvDuration = (TextView) contentView.findViewById(R.id.msTvDuration);
		mIvAudioSwitch = (ImageView) contentView.findViewById(R.id.msIvAudioSwitch);
		mProgressBar = (SeekBar) contentView.findViewById(R.id.msAudioSeekBar);

		mTvAudioTitle.setText(mAttachment.name);    // 设置标题
		mTvProgress.setText(DateUtils.formatElapsedTime(0));               // 后面更新这玩意就行

		mWeakHandler = new WeakHandler(this);
		try {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(TextUtils.isEmpty(mAudioPath) ? mAttachment.path : mAudioPath);
			mMediaPlayer.setOnCompletionListener(mmp -> {
				isPlaying = false;
				mIvAudioSwitch.setImageResource(R.mipmap.ms_icon_audio_play);
				mWeakHandler.sendEmptyMessage(COMPLETED);
			});
			mMediaPlayer.prepare();

			mProgressBar.setMax(mMediaPlayer.getDuration());
			mTvDuration.setText(DateUtils.formatElapsedTime(mMediaPlayer.getDuration() / 1000));
		} catch (Exception exp) {
		}

		mProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			}

			@Override public void onStartTrackingTouch(SeekBar seekBar) {
				AudioPlayer.this.isDrag = true;
			}

			@Override public void onStopTrackingTouch(SeekBar seekBar) {
				AudioPlayer.this.isDrag = false;
				if (mMediaPlayer != null) {
					mMediaPlayer.seekTo(seekBar.getProgress());
				}
				mWeakHandler.sendEmptyMessage(PLAYING);
			}
		});

		mIvAudioSwitch.setOnClickListener(v -> {
			if (isPlaying) {
				pause();
			}
			else {
				play();
			}
			isPlaying = !isPlaying;
		});
	}

	private void play() {
		if (mMediaPlayer != null) {
			mIvAudioSwitch.setImageResource(R.mipmap.ms_icon_audio_pause);
			try {
				mMediaPlayer.start();
				mWeakHandler.sendEmptyMessage(PLAYING);
			} catch (Exception exp) {
			}
		}
	}

	private void pause() {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mIvAudioSwitch.setImageResource(R.mipmap.ms_icon_audio_play);
			mMediaPlayer.pause();
			mWeakHandler.sendEmptyMessage(STOP);
		}
	}

	@Override public void onCancel(DialogInterface dialog) {
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	@Override public void onDestroyView() {
		super.onDestroyView();
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
}