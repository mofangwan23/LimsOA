package cn.flyrise.feep.media.activity;

import android.os.Bundle;

import com.xiao.nicevideoplayer.NiceVideoPlayer;
import com.xiao.nicevideoplayer.TxVideoPlayerController;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.media.R;



/**
 * @author mo
 */
public class VideoPlayerActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void bindView() {
		setContentView(R.layout.activity_video_player);
		String path =  getIntent().getStringExtra("videoPath");
		String title = getIntent().getStringExtra("title");
		NiceVideoPlayer mNiceVideoPlayer = (NiceVideoPlayer) findViewById(R.id.nice_video_player);
		mNiceVideoPlayer.setPlayerType(NiceVideoPlayer.TYPE_IJK); // or NiceVideoPlayer.TYPE_NATIVE
		mNiceVideoPlayer.setUp(path, null);

		TxVideoPlayerController controller = new TxVideoPlayerController(this);
		controller.setTitle(title);
//		controller.setImage(path);
		mNiceVideoPlayer.setController(controller);
		mNiceVideoPlayer.start();
	}

	private void init() {

	}

//	@Override
//	protected void onStop() {
//		//从前台切到后台，当视频正在播放或者正在缓冲时，调用该方法暂停视频
////		VideoPlayerManager.instance().suspendVideoPlayer();
//
//		NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
//
//	}
//
//	@Override
//	protected void onDestroy() {
//		super.onDestroy();
//		//销毁页面，释放，内部的播放器被释放掉，同时如果在全屏、小窗口模式下都会退出
////		VideoPlayerManager.instance().releaseVideoPlayer();
//	}
//
//	@Override
//	public void onBackPressed() {
//		//处理返回键逻辑；如果是全屏，则退出全屏；如果是小窗口，则退出小窗口
////		if (VideoPlayerManager.instance().onBackPressed()){
////			return;
////		}else {
////			//销毁页面
////			VideoPlayerManager.instance().releaseVideoPlayer();
////		}
//		super.onBackPressed();
//	}
//
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		//从后台切换到前台，当视频暂停时或者缓冲暂停时，调用该方法重新开启视频播放
////		VideoPlayerManager.instance().resumeVideoPlayer();
//	}


}
