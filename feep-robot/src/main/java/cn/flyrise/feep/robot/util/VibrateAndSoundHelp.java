package cn.flyrise.feep.robot.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;

import cn.flyrise.feep.robot.R;

/**
 * 震动和声音
 */
public class VibrateAndSoundHelp {

    private Vibrator vibrator;

    private static final int VIBRATE_TIME = 100;//按钮震动时间

    private SoundPool sound_pool;//声明一个SoundPool
    private int music;

    private AudioManager mgr;

    public VibrateAndSoundHelp(Context context) {
        mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        sound_pool = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        music = sound_pool.load(context, R.raw.robot_news, 1);
    }

    public void play() {
        vSimple();
        setSoundPool();
    }

    //简单音乐
    private void setSoundPool() {
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        float volume = streamVolumeCurrent / streamVolumeMax;
        sound_pool.play(music, volume, volume, 1, 0, 1f);
    }

    //简单震动
    private void vSimple() {
        if (vibrator == null) {
            return;
        }
        vibrator.vibrate(VIBRATE_TIME);
    }

    public void onDestroy() {
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (sound_pool != null) {
            sound_pool.unload(music);
            sound_pool = null;
        }

    }
}
