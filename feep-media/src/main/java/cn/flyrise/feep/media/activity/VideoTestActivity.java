package cn.flyrise.feep.media.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.xiao.nicevideoplayer.NiceVideoPlayer;
import com.xiao.nicevideoplayer.NiceVideoPlayerManager;
import com.xiao.nicevideoplayer.TxVideoPlayerController;

import cn.flyrise.feep.media.R;

public class VideoTestActivity extends AppCompatActivity {

    private long startTime;
    private long endTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_test);
        String path =  getIntent().getStringExtra("videoPath");
        String title = getIntent().getStringExtra("title");
        NiceVideoPlayer mNiceVideoPlayer = (NiceVideoPlayer) findViewById(R.id.nice_video_player);
        mNiceVideoPlayer.setPlayerType(NiceVideoPlayer.TYPE_IJK);
        mNiceVideoPlayer.setUp(path, null);

        TxVideoPlayerController controller = new TxVideoPlayerController(this);
        controller.setTitle(title);
        controller.setImage(R.mipmap.personalbg);
        mNiceVideoPlayer.setController(controller);
        mNiceVideoPlayer.start();
        mNiceVideoPlayer.getDuration();
        startTime = System.currentTimeMillis();
    }


    @Override
    protected void onStop() {
        super.onStop();
        // 在onStop时释放掉播放器
        NiceVideoPlayerManager.instance().releaseNiceVideoPlayer();
    }
    @Override
    public void onBackPressed() {
        // 在全屏或者小窗口时按返回键要先退出全屏或小窗口，
        // 所以在Activity中onBackPress要交给NiceVideoPlayer先处理。
        if (NiceVideoPlayerManager.instance().onBackPressd()) return;
        endTime = System.currentTimeMillis();
        Intent data = new Intent();
        data.putExtra("takeTime", (endTime - startTime)+"");
        String id = getIntent().getStringExtra("id");
        if (!TextUtils.isEmpty(id)){
            data.putExtra("dataId",id);
        }
        setResult(Activity.RESULT_OK, data);
        super.onBackPressed();
    }
}