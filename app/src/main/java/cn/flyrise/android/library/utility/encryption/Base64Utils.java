package cn.flyrise.android.library.utility.encryption;

import it.sauronsoftware.base64.Base64;

/**
 * BASE64编码解码工具包 依赖javabase64-1.3.1.jar
 *
 * @author cww123
 * @version 1.0
 * @date 2014-2-20
 */

public class Base64Utils {

    /**
     * BASE64字符串解码为二进制数据
     *
     * @throws Exception
     */
    public static byte[] decode(String base64) throws Exception {
        return Base64.decode(base64.getBytes());
    }

    /**
     * 二进制数据编码为BASE64字符串
     *
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes));
    }

    // 加密
    public static String getBase64(String str) {
        return android.util.Base64.encodeToString(str.getBytes(), android.util.Base64.DEFAULT);
    }

    // 解密
    public static String getFromBase64(String s) {
        return new String(android.util.Base64.decode(s.getBytes(), android.util.Base64.DEFAULT));
    }
}
