package com.hyphenate.easeui.ui;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.busevent.EMMessageEvent.ImMessageRefresh;
import com.hyphenate.easeui.utils.EaseFileUtils;
import java.io.File;
import java.util.Arrays;
import org.greenrobot.eventbus.EventBus;

public class EaseShowNormalFileActivity extends EaseBaseActivity {
	private ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ease_activity_show_file);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);

		final EMMessage message = getIntent().getParcelableExtra("msg");
        if (!(message.getBody() instanceof EMFileMessageBody)) {
            Toast.makeText(EaseShowNormalFileActivity.this, "Unsupported message body", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        final File file = new File(((EMFileMessageBody)message.getBody()).getLocalUrl());

        message.setMessageStatusCallback(new EMCallBack() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    EaseFileUtils.openFile(file,  EaseShowNormalFileActivity.this);
                    finish();
	                //下载完成后去刷新聊天的界面：
	                EventBus.getDefault().post(new ImMessageRefresh(Arrays.asList(message)));
                });

            }

            @Override
            public void onError(int code, String error) {
                runOnUiThread(() -> {
                    if(file.exists() && file.isFile())
                        file.delete();
                    String str4 = getResources().getString(R.string.Failed_to_download_file);
                    Toast.makeText(EaseShowNormalFileActivity.this, str4+message, Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onProgress(final int progress, String status) {
                runOnUiThread(() -> progressBar.setProgress(progress));
            }
        });
        EMClient.getInstance().chatManager().downloadAttachment(message);
	}
}
