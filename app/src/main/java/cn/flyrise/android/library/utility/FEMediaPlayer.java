/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-24 下午2:58:30
 */
package cn.flyrise.android.library.utility;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import java.io.IOException;

import cn.flyrise.feep.collaboration.utility.SupportsAttachments;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-1-24</br> 修改备注：</br>
 */
public class FEMediaPlayer extends MediaPlayer {
    /**
     * 停止状态
     */
    public static final int STOP_STATE = 0;
    /**
     * 暂停状态
     */
    public static final int PAUSE_STATE = 1;
    /**
     * 播放状态
     */
    public static final int PLAYING_STATE = 2;
    /**
     * 当前播放状态
     */
    private int state = STOP_STATE;

    private final Handler playerHandler;

    private onPlayProgressChangeListener progressChangeListener;

    public FEMediaPlayer() {
        playerHandler = new Handler();
        // 监听电话
        final TelephonyManager tm = (TelephonyManager) CoreZygote.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        tm.listen(new MyPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * 准备音乐
     */
    public void prepareMusic(String filePath) {
        if (filePath != null) {
            try {
                reset();
                setDataSource(filePath);
                prepare();
            } catch (final IllegalArgumentException | IOException | IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判断是否音频文件
     *
     * @param filePath 文件路径
     */
    public static boolean isAudioFile(String filePath) {
        final int index = filePath.lastIndexOf(".");
        boolean isAudioFile = false;
        if (index != -1) {
            final String n = filePath.substring(index, filePath.length());
            if (CommonUtil.checkArray(n, SupportsAttachments.reclastArray)) {
                isAudioFile = true;
            }
        }
        return isAudioFile;
    }

    @Override
    public void start() throws IllegalStateException {
        super.start();
        if (isPlaying()) {
            playerHandler.post(playCallback);
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        super.pause();
        playerHandler.removeCallbacks(playCallback);
        state = PAUSE_STATE;
        callProgressListener();
    }

    @Override
    public void stop() throws IllegalStateException {
        super.stop();
        state = STOP_STATE;
        playerHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 设置当前状态为停止，并且通知监听器
     */
    public void changeToStopState() {
        state = STOP_STATE;
        callProgressListener();
    }

    private final Runnable playCallback = new Runnable() {
        @Override
        public void run() {
            state = PLAYING_STATE;
            callProgressListener();
            playerHandler.postDelayed(playCallback, 1000);
        }

    };

    /**
     * 调用播放进度监听器的方法
     */
    private void callProgressListener() {
        final int progress = getCurrentProgress();
        if (progressChangeListener != null) {
            progressChangeListener.onPlayProgressChange(state, progress);
        }
    }

    /**
     * 获取当前的播放进度（getCurrentPosition() / 1000）
     */
    public int getCurrentProgress() {
        return getCurrentPosition() / 1000;
    }

    /**
     * 设置播放进度监听器
     */
    public void setOnPlayProgressChangeListener(onPlayProgressChangeListener listener) {
        this.progressChangeListener = listener;
    }

    /**
     * 播放进度监听器
     */
    public interface onPlayProgressChangeListener {
        /**
         * 播放状态与进度
         *
         * @param playState (0为停止，1为暂停，2为播放)
         * @param progress  播放进度
         */
        void onPlayProgressChange(int playState, int progress);
    }

    /**
     * 监听来电
     */
    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            if (isPlaying()) {
                pause();
            }
        }
    }

}
