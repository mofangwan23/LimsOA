package cn.flyrise.feep.utils;

import android.content.Intent;
import android.os.Bundle;


import cn.flyrise.android.library.utility.encryption.Base64Utils;
import cn.flyrise.android.protocol.model.CaptureReturnData;
import cn.flyrise.feep.core.common.utils.GsonUtil;

/**
 * Created by Administrator on 2016-9-6.
 */
public class ParseCaptureUtils {
    public static final int SCANNIN_GREQUEST_CODE = 10009;                    // 二维码扫描验证码

    public static final int SDCARD_SCANNIN_CODE = 10009;                    // 从本地获取二维码图片

    public static final String CAPTURE_RESULT_DATA = "result";

    public static CaptureReturnData parseData(Intent data, int requestCode) {
        if (requestCode != SCANNIN_GREQUEST_CODE && requestCode != SDCARD_SCANNIN_CODE) {
            return null;
        }
        if (data == null) {
            return null;
        }
        final Bundle bundle = data.getExtras();
        final String str = bundle.getString(CAPTURE_RESULT_DATA);
        CaptureReturnData crd = parseEncryptionData(str);
        return crd;
    }

    /**
     * 解析加密后的数据
     */
    private static CaptureReturnData parseEncryptionData(final String str) {
        CaptureReturnData crd = null;
        try {
            crd = parseUnEncryptionData(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (crd != null) {
            return crd;
        }
        try {
            crd = GsonUtil.getInstance().fromJson(Base64Utils.getFromBase64(str), CaptureReturnData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return crd;
    }

    /**
     * 解析默认未加密后的数据
     */
    private static CaptureReturnData parseUnEncryptionData(final String str) {
        return GsonUtil.getInstance().fromJson(str, CaptureReturnData.class);
    }
}
