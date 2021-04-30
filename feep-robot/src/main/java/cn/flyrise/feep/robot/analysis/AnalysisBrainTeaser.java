package cn.flyrise.feep.robot.analysis;

import android.text.TextUtils;

import java.util.List;

import cn.flyrise.feep.robot.entity.RiddleResultItem;
import cn.flyrise.feep.robot.entity.RobotResultItem;

/**
 * 新建：陈冕;
 * 日期： 2018-1-9-13:39.
 * 脑筋急转弯
 */

public class AnalysisBrainTeaser {

    private static final String SUB_TEXT = "答案：";

    public static RiddleResultItem analysisRiddle(List<RobotResultItem> results) {
        RobotResultItem item = results.get(0);
        if (item == null) {
            return null;
        }
        RiddleResultItem resultItem = new RiddleResultItem();
        resultItem.title = item.title;
        resultItem.answer = item.riddleAnswer;
        return resultItem;
    }

    public static RiddleResultItem analysisBrainTeaser(String answerText) {
        if (TextUtils.isEmpty(answerText) || !answerText.contains(SUB_TEXT)) {
            return null;
        }
        RiddleResultItem resultItem = new RiddleResultItem();
        resultItem.title = getBrainTeaserTitle(answerText);
        resultItem.answer = getBrainTeaserAnswer(answerText);
        return resultItem;
    }

    private static String getBrainTeaserTitle(String text) {
        return text.substring(0, text.lastIndexOf(SUB_TEXT));
    }

    private static String getBrainTeaserAnswer(String text) {
        return text.substring(text.lastIndexOf(SUB_TEXT), text.length());
    }

}
