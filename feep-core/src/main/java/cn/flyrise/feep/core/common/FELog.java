package cn.flyrise.feep.core.common;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.flyrise.feep.core.common.utils.FileUtil;

/**
 * @author ZYP
 * @since 2017-02-06 16:23
 */
public class FELog {
    public static final String TAG = "FlyRise";
    public static final boolean isDebug = true;

    public static void d(String tag, Object infos) {
        if (isDebug) {
            Log.d(tag, infos.toString());
        }
    }

    public static void e(String tag, Object infos) {
        if (isDebug) {
            Log.e(tag, infos.toString());
        }
    }

    public static void v(String tag, Object infos) {
        if (isDebug) {
            Log.v(tag, infos.toString());
        }
    }

    public static void w(String tag, Object infos) {
        if (isDebug) {
            Log.w(tag, infos.toString());
        }
    }

    public static void i(String tag, Object infos) {
        if (isDebug) {
            Log.i(tag, infos.toString());
        }
    }

    public static void d(String message) {
        if (isDebug) {
            Log.d(TAG, message);
        }
    }

    public static void i(String message) {
        if (isDebug) {
            Log.i(TAG, message);
        }
    }

    public static void e(String message) {
        if (isDebug) {
            Log.e(TAG, message);
        }
    }

    public static void w(String message) {
        if (isDebug) {
            Log.w(TAG, message);
        }
    }

    public static void v(String message) {
        if (isDebug) {
            Log.v(TAG, message);
        }
    }

    public static void writeSdCard(Throwable e) {
        writeSdCard(getThrowableText(e));
    }

    public static void writeSdCard(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }
        try {
            long currentTime = System.currentTimeMillis();
            SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
            String timeFormat = format.format(new Date(currentTime));
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String logFilePath = "/mnt/sdcard/study/crash/" + File.separator + timeFormat + ".log";
                File logFile = new File(logFilePath);
                if (logFile.exists()) {
                    logFile.delete();
                }
                FileUtil.newFile(logFile);

                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(logFile));
                bos.write(message.getBytes());
                bos.flush();
                bos.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    private static String getThrowableText(Throwable ex) {
        StringBuffer sb = new StringBuffer();

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        return sb.toString();
    }
}
