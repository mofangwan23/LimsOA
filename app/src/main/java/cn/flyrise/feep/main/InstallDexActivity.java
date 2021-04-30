package cn.flyrise.feep.main;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.view.Window;
import android.view.WindowManager;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016/7/4 10:02
 * 在 install_dex 进程中进行 Dex 的安装。
 */
public class InstallDexActivity extends Activity {

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FELog.e("MultiDex", "InstallDexActivity onCreate().");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        overridePendingTransition(R.anim.install_dex_null_anim, R.anim.install_dex_null_anim);
        setContentView(R.layout.activity_splash);
        new LoadDexTask().execute();
    }

    private class LoadDexTask extends AsyncTask {

        @Override protected Object doInBackground(Object[] params) {
            FELog.e("MultiDex", "MultiDex start install in mini process.");
            MultiDex.install(getApplication());
            FEApplication application = (FEApplication) getApplication();
            application.installDexFinish(application);
            return null;
        }

        @Override protected void onPostExecute(Object object) {
            FELog.e("MultiDex", "MultiDex install finish in mini process.");
            InstallDexActivity.this.finish();
        }
    }

    @Override public void onBackPressed() {
        // Disable the back press event.
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }
}
