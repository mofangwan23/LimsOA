package cn.flyrise.feep.collaboration.utility;

import android.graphics.Color;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-05-04 12:51
 */
public class RichStyle {

    public boolean isBold;
    public boolean isItalic;
    public boolean isAlignLeft;
    public boolean isAlignCenter;
    public boolean isAlignRight;
    public boolean isUnderLine;
    public int fontSize = 4;
    public String rgbColor;    // RGB(255,87,34)

    public int getFontColor() {
        if (TextUtils.isEmpty(rgbColor)) {
            return -1;
        }
        Pattern p = Pattern.compile("\\d+");
        List<Integer> rgb = new ArrayList<>();
        Matcher matcher = p.matcher(rgbColor);
        while (matcher.find()) {
            String group = matcher.group();
            rgb.add(CommonUtil.parseInt(group));
        }

        if (rgb.size() < 3) {
            return -1;
        }

        return Color.rgb(rgb.get(0), rgb.get(1), rgb.get(2));
    }

}
