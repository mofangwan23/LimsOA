package com.hyphenate.easeui.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import cn.flyrise.feep.media.common.AttachmentUtils;
import java.io.File;

import cn.flyrise.feep.core.common.FEToast;

/**
 * Created by klc on 2017/7/13.
 */

public class EaseFileUtils {

    public static void openFile(File var0, Activity var1) {
        String var3 = getMIMEType(var0);
        Intent intent = AttachmentUtils.getIntent(var1,var0.getPath(),var3);
        try {
            var1.startActivity(intent);
        } catch (Exception var5) {
            var5.printStackTrace();
            FEToast.showMessage("无法找到此文件类型的程序");
        }
    }

    private static String getMIMEType(File var0) {
        String var1 = "";
        String var2 = var0.getName();
        String var3 = var2.substring(var2.lastIndexOf(".") + 1, var2.length()).toLowerCase();
        var1 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(var3);
        return var1;
    }
}
