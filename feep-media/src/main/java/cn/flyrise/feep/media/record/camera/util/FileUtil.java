package cn.flyrise.feep.media.record.camera.util;

import android.graphics.Bitmap;
import android.os.Environment;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUtil {

    public static boolean deleteFile(String url) {
        boolean result = false;
        File file = new File(url);
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }
}
