package com.hyphenate.easeui.model;

import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.domain.EaseEmojicon.Type;
import com.hyphenate.easeui.utils.EaseSmileUtils;

public class EaseDefaultEmojiconDatas {

    private final static int iconNums = 80;

    private static EaseEmojicon[] createData() {
        EaseEmojicon[] datas = new EaseEmojicon[iconNums];
        for (int i = 0; i < iconNums; i++) {
            datas[i] = new EaseEmojicon(EaseSmileUtils.getEaseEmojicon(i + 1), EaseSmileUtils.getEaseEmojiconKey(i), Type.NORMAL);
        }
        return datas;
    }

    public static EaseEmojicon[] getData() {
        return createData();
    }
}
