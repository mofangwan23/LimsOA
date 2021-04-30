package cn.flyrise.feep.commonality.util;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * Created by klc on 2017/6/21.
 */

public class CachePath {

    public static int COLLABORATION = 1;
    public static int FORM = 2;

    public static String getCachePath(int type, String id, int doType) {
        StringBuilder nameBuilder = new StringBuilder();
        nameBuilder.append(CoreZygote.getLoginUserServices().getServerAddress()).append("_");
        if (type == COLLABORATION) {
            nameBuilder.append("_Collaboration");
        } else {
            nameBuilder.append("_Form");
        }
        nameBuilder.append("_").append(id).append("_").append(doType);
        return CoreZygote.getPathServices().getUserPath() + "/" + CommonUtil.getMD5(nameBuilder.toString());
    }
}
