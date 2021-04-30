package cn.flyrise.feep.protocol;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.main.InstallDexActivity;

/**
 * @author ZYP
 * @since 2017-02-26 11:04
 */
public class BaseApplication extends Application {

    private static final String PACKAGE_NAME = "cn.flyrise.study";
    private static final String INSTALL_PROCESS_NAME = ":install_dex";
    private static final String NO_SECOND_DEX = "no_second_dex";
    private static final String KEY_DEX2_SHA1 = "key_dex2_sha1";
    private String m2thDexDigest;

    @Override protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        if (!canQuickStart(base) && Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (needWaitForDexOpt(base)) {
                waitForDexOpt(base);
            }
            MultiDex.install(base);
        }
    }

    protected boolean canQuickStart(Context base) {
        String currentProcessName = getCurrentProcessName(base);
        if (TextUtils.isEmpty(currentProcessName)) {
            return false;
        }
        return currentProcessName.contains(INSTALL_PROCESS_NAME);
    }

    protected boolean needWaitForDexOpt(Context context) {
        String _2thDexSHA1 = get2thDexSHA1(context);
        if (TextUtils.equals(_2thDexSHA1, NO_SECOND_DEX)) {
            return false;
        }

        SharedPreferences sp = context.getSharedPreferences(getPackageInfo(context).versionName, MODE_MULTI_PROCESS);
        String saveValue = sp.getString(KEY_DEX2_SHA1, "");
        return !TextUtils.equals(_2thDexSHA1, saveValue);
    }

    protected void waitForDexOpt(Context base) {
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(PACKAGE_NAME, InstallDexActivity.class.getName());
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        base.startActivity(intent);
        long startWait = System.currentTimeMillis();
        long waitTime = 10 * 1000;
        while (needWaitForDexOpt(base)) {
            try {
                long nowWait = System.currentTimeMillis() - startWait;
                if (nowWait >= waitTime) {
                    return;
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected String get2thDexSHA1(Context context) {
        if (m2thDexDigest == null) {
            ApplicationInfo ai = context.getApplicationInfo();
            String source = ai.sourceDir;
            try {
                JarFile jar = new JarFile(source);
                java.util.jar.Manifest manifest = jar.getManifest();
                Map<String, Attributes> map = manifest.getEntries();
                Attributes a = map.get("classes2.dex");
                m2thDexDigest = a.getValue("SHA1-Digest");
            } catch (Exception e) {
                return NO_SECOND_DEX;
            }
        }
        return m2thDexDigest;
    }

    public void installDexFinish(Context context) {
        SharedPreferences sp = context.getSharedPreferences(getPackageInfo(context).versionName, MODE_MULTI_PROCESS);
        sp.edit().putString(KEY_DEX2_SHA1, get2thDexSHA1(context)).commit();
    }

    protected PackageInfo getPackageInfo(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            FELog.e(e.getLocalizedMessage());
        }
        return null;
    }

    protected String getCurrentProcessName(Context context) {
        try {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
                if (appProcess.pid == pid) {
                    return appProcess.processName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
