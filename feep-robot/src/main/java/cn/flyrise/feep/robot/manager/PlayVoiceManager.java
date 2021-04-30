package cn.flyrise.feep.robot.manager;

import android.os.Handler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import cn.flyrise.feep.robot.util.LMediaPlayerUtil;

/**
 * 新建：陈冕;
 * 日期： 2017-12-14-16:37.
 * 播放音频的管理器
 */

public class PlayVoiceManager {

    private List<RobotResultItem> resultItems;
    private int index = 0;
    private boolean isPause = false;

    private boolean isSelectedItem = false; //是用户选中的，循环播放该音频

    private Handler handler;
    private Timer timer;
    private ProgressTimerTask timerTask;

    private OnPlayVoiceListener mListener;

    public int getIndex() {
        return index;
    }

    public boolean isSelectedItem() {
        return isSelectedItem;
    }

    public int getResultItemSize() {
        return CommonUtil.isEmptyList(resultItems) ? 0 : resultItems.size();
    }

    public PlayVoiceManager(List<RobotResultItem> results, OnPlayVoiceListener listener) {
        index = 0;
        this.mListener = listener;
        resultItems = results;
        isPause = false;
    }

    public void newPlayingVoice() {
        if (CommonUtil.isEmptyList(resultItems)) {
            releaseMediaPlayer();
            return;
        }
        RobotResultItem robotResultItem = resultItems.get(index);
        if (robotResultItem == null) {
            releaseMediaPlayer();
            return;
        }
        LMediaPlayerUtil.getInstance().playMusic(index, robotResultItem.urlMp3
                , position -> playMusic());
        index++;
        if (mListener != null) {
            mListener.setRefreshView(robotResultItem);
        }
        createTimer();
    }

    private void playMusic() {
        if (isSelectedItem) {
            startPlayingVoice();
        } else {
            nextPlaying();
        }
    }

    private void createTimer() {
        if (handler == null) {
            handler = new Handler();
        }
        if (timer == null) {
            timer = new Timer();
        }
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = new ProgressTimerTask();
        timer.schedule(timerTask, 0, 500);
    }

    //开始或暂停播放
    public void statePlaying(boolean isStatePause) {
        isPause = isStatePause;
    }

    //播放上一首
    public void lastPlaying() {
        index--;
        if (index <= 0) {
            return;
        }
        startPlayingVoice();
    }

    //播放下一首
    public void nextPlaying() {
        index++;
        if (index > resultItems.size()) {
            return;
        }
        startPlayingVoice();
    }

    //开始播放
    private void startPlayingVoice() {
        if (index > resultItems.size()) {
            index = 0;
            return;
        }
        isPause = false;
        if (mListener != null) {
            mListener.refreshStatePlay();
        }
        RobotResultItem robotResultItem = resultItems.get(index - 1);
        if (robotResultItem == null) {
            releaseMediaPlayer();
            return;
        }
        if (mListener != null) {
            mListener.setRefreshView(robotResultItem);
        }
        LMediaPlayerUtil.getInstance().playMusic02(robotResultItem.urlMp3);
    }

    public void onDestroy() {
        releaseMediaPlayer();
        cancel();
    }

    private void releaseMediaPlayer() {
        isSelectedItem = false;
        index = 0;
        LMediaPlayerUtil.getInstance().releaseMediaPlayer();
    }

    private void cancel() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    public void clickeItem(int position) {
        isSelectedItem = true;
        index = position + 1;
        startPlayingVoice();
    }

    private class ProgressTimerTask extends TimerTask {

        @Override
        public void run() {
            handler.post(() -> {
                if (isPause) {
                    return;
                }
                int progress = 100 * LMediaPlayerUtil.getInstance().getPosition() / LMediaPlayerUtil.getInstance().getDuration();
                if (mListener != null) {
                    mListener.onProgress(progress);
                }
            });
        }
    }

    public interface OnPlayVoiceListener {
        void onProgress(int progress);

        void setRefreshView(RobotResultItem item);

        void refreshStatePlay();
    }
}
