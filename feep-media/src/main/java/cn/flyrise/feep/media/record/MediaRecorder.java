package cn.flyrise.feep.media.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import cn.flyrise.feep.core.common.utils.FileUtil;
import java.io.File;
import java.io.IOException;

/**
 * Create by cm132 on 2019/1/2.
 * Describe:音视频录制
 */
public class MediaRecorder {

	public static final int REQUEST_CODE_RECORD_VIDEO = 1018;//视频返回
	public static final int REQUEST_CODE_RECORD_AUDIO = 1019;//音频返回

	public static void recorderVoide(Activity context, String path) {//系统录像
		File file = new File(path);
		if (!file.exists()) FileUtil.newFile(file);
		Uri uri = Uri.fromFile(file);   // 将路径转换为Uri对象
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);  // 表示跳转至相机的录视频界面
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0.5);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
		intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 10 * 1024 * 1024L);//限制大小
		context.startActivityForResult(intent, REQUEST_CODE_RECORD_VIDEO);
	}

	public static int getRecorderSize(Context context, String path) {
		File file = new File(path);
		if (!file.exists()) return 0;
		Uri uri = Uri.fromFile(file);   // 将路径转换为Uri对象
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(context, uri);
			mediaPlayer.prepare();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mediaPlayer.getDuration();   // 获取到的是毫秒值
	}

	public static Bitmap getBitmap(String path) {
		File file = new File(path);
		if (!file.exists()) return null;
		Uri uri = Uri.fromFile(file);   // 将路径转换为Uri对象
		MediaMetadataRetriever media = new MediaMetadataRetriever();
		String videoPath = uri.getPath();            // 通过Uri获取绝对路径
		media.setDataSource(videoPath);
		return media.getFrameAtTime();      // 视频的第一帧图片
	}

	public static void recorderAudio(Activity context, String path) {//系统音频录制
		File file = new File(path);
		if (!file.exists()) FileUtil.newFile(file);
		Uri uri = Uri.fromFile(file);   // 将路径转换为Uri对象
		Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);  // 表示跳转至相机的录视频界面
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);    // MediaStore.EXTRA_VIDEO_QUALITY 表示录制视频的质量，从 0-1，越大表示质量越好，同时视频也越大
		intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);    // 表示录制完后保存的录制，如果不写，则会保存到默认的路径，在onActivityResult()的回调，通过intent.getData中返回保存的路径
		intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30);   // 设置视频录制的最长时间
		context.startActivityForResult(intent, REQUEST_CODE_RECORD_AUDIO);
	}

}
