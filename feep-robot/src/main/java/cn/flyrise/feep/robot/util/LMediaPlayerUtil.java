package cn.flyrise.feep.robot.util;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * 新建：陈冕;
 * 日期： 2017-11-28-9:30.
 * 音频播放
 */

public class LMediaPlayerUtil {
    private static LMediaPlayerUtil instance;
    private MediaPlayer mediaPlayer;
    private HandlerThread playHandlerThread;
    private Handler playHandler;
    /**
     * 播放
     */
    private static final int PLAY = 101;
    /**
     * 停止
     */
    private static final int STOP = 102;
    /**
     * 释放
     */
    private static final int RELEASE = 103;
    /**
     * 界面不可见
     */
    private static final int ONSTOP = 104;
    private Handler handler;

    private int position;
    private String url;
    private PlayMusicCompleteListener listener;

    private boolean isPause = false;

    /**
     * 播放一首完成的回调
     */
    public interface PlayMusicCompleteListener {
        void playMusicComplete(int position);
    }

    private void createHandlerThreadIfNeed() {
        if (playHandlerThread == null) {
            playHandlerThread = new HandlerThread("playHandlerThread");
            playHandlerThread.start();
        }
    }

    private void createHandlerIfNeed() {
        if (playHandler == null) {
            playHandler = new Handler(playHandlerThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case PLAY:
                            playMusic01();
                            break;
                        case STOP:
                            stopMediaPlayer02();
                            break;
                        case RELEASE:
                            releaseMediaPlayer02();
                            break;
                        case ONSTOP:
                            stopMediaPlayer03();
                            break;
                    }
                }
            };
        }
    }

    private void createPlayerIfNeed() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    private LMediaPlayerUtil() {
        handler = new Handler(Looper.getMainLooper());
        createHandlerThreadIfNeed();
        createHandlerIfNeed();
    }

    public static LMediaPlayerUtil getInstance() {
        if (instance == null) {
            instance = new LMediaPlayerUtil();
        }
        return instance;
    }

    public void playMusic(int position, String url, PlayMusicCompleteListener listener) {
        this.position = position;
        this.url = url;
        this.listener = listener;
        playHandler.sendEmptyMessageDelayed(PLAY, 0L);
    }

    private void playMusic01() {
        createPlayerIfNeed();
        playMusic02(url);
    }

    public void playMusic02(String url) {
        try {
            isPause = false;
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url);
            mediaPlayer.setLooping(false);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnPreparedListener(player -> {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(player -> {
                stopMediaPlayer();
                playMusicComplete();
            });
            mediaPlayer.setOnErrorListener((player, what, extra) -> {
                stopMediaPlayer();
                return false;
            });
            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopMediaPlayer() {
        playHandler.sendEmptyMessage(STOP);
    }

    private void stopMediaPlayer02() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.setOnCompletionListener(null);
            try {
                mediaPlayer.stop();
            } catch (IllegalStateException ignored) {

            }
        }
    }

    private void stopMediaPlayer03() {
        stopMediaPlayer02();
        playMusicComplete();
    }

    /**
     * 播放完成,需要在主线程里面更新UI
     */
    private void playMusicComplete() {
        handler.post(() -> {
            if (listener != null) {
                listener.playMusicComplete(position);
            }
        });
    }

    public void releaseMediaPlayer() {
        playHandler.sendEmptyMessage(RELEASE);
    }

    private void releaseMediaPlayer02() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException ignored) {

            }
        }
        mediaPlayer = null;
        // 避免内存泄露
        listener = null;
        handler.removeCallbacksAndMessages(null);
    }

    public int getPosition() {
        try {
            return mediaPlayer == null
                    || !mediaPlayer.isPlaying() ? 0 : mediaPlayer.getCurrentPosition();
        } catch (IllegalStateException ignored) {

        }
        return 0;
    }

    public int getDuration() {
        try {
            return mediaPlayer == null || !mediaPlayer.isPlaying()
                    || mediaPlayer.getDuration() <= 0 ? 1 : mediaPlayer.getDuration();
        } catch (IllegalStateException ignored) {

        }
        return 1;
    }

    public boolean getStatePause() {
        if (mediaPlayer == null) {
            return isPause;
        }
        if (mediaPlayer.isPlaying() && !isPause) {
            mediaPlayer.pause();
            isPause = true;
        } else {
            mediaPlayer.start();
            isPause = false;
        }
        return isPause;
    }

}
