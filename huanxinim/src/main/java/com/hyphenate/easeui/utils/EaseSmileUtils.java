/**
 * Copyright (C) 2016 Hyphenate Inc. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hyphenate.easeui.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.VerticalImageSpan;

import com.hyphenate.chatui.R;
import com.hyphenate.easeui.domain.EaseEmojicon;
import com.hyphenate.easeui.model.EaseDefaultEmojiconDatas;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EaseSmileUtils {
    public static final String DELETE_KEY = "em_delete_delete_expression";

    private static final String KEY_START = "[(A:";
    private static final String KEY_END = ")]";

    private static final Factory spannableFactory = Spannable.Factory.getInstance();

    private static final Map<Pattern, Object> emoticons = new HashMap<>();

    static {
        EaseEmojicon[] emojicons = EaseDefaultEmojiconDatas.getData();
        for (EaseEmojicon emojicon : emojicons) {
            addPattern(emojicon.getEmojiText(), emojicon.getIcon());
        }
    }

    /**
     * add text and icon to the map
     *
     * @param emojiText-- text of emoji
     * @param icon        -- resource id or local path
     */
    public static void addPattern(String emojiText, Object icon) {
        emoticons.put(Pattern.compile(Pattern.quote(emojiText)), icon);
    }


    /**
     * replace existing spannable with smiles
     *
     * @param context
     * @param spannable
     * @return
     */
    public static boolean addSmiles(Context context, Spannable spannable, boolean isBigEmoji) {
        boolean hasChanges = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(spannable);
            while (matcher.find()) {
                boolean set = true;
                for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class))
                    if (spannable.getSpanStart(span) >= matcher.start()
                            && spannable.getSpanEnd(span) <= matcher.end())
                        spannable.removeSpan(span);
                    else {
                        set = false;
                        break;
                    }
                if (set) {
                    hasChanges = true;
                    Object value = entry.getValue();
                    if (value instanceof String && !((String) value).startsWith("http")) {
                        File file = new File((String) value);
                        if (!file.exists() || file.isDirectory()) {
                            return false;
                        }
                        spannable.setSpan(new ImageSpan(context, Uri.fromFile(file)),
                                matcher.start(), matcher.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    } else {
                        Drawable drawable = context.getResources().getDrawable((Integer) value);
                        int intrinsicWidth = drawable.getIntrinsicWidth();
                        int intrinsicHeight = drawable.getIntrinsicHeight();
                        int boundsWidth = isBigEmoji ? intrinsicWidth * 4 / 5 : intrinsicWidth * 3 / 5;
                        int boundsHeight = isBigEmoji ? intrinsicHeight * 4 / 5 : intrinsicHeight * 3 / 5;
                        drawable.setBounds(0, 0, boundsWidth, boundsHeight);
                        ImageSpan imageSpan = new VerticalImageSpan(drawable);
                        spannable.setSpan(imageSpan,
                                matcher.start(), matcher.end(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }
            }
        }
        return hasChanges;
    }


    public static Spannable getSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable, true);
        return spannable;
    }

    public static Spannable getSmallSmiledText(Context context, CharSequence text) {
        Spannable spannable = spannableFactory.newSpannable(text);
        addSmiles(context, spannable, false);
        return spannable;
    }

    public static boolean containsKey(String key) {
        boolean b = false;
        for (Entry<Pattern, Object> entry : emoticons.entrySet()) {
            Matcher matcher = entry.getKey().matcher(key);
            if (matcher.find()) {
                b = true;
                break;
            }
        }

        return b;
    }

    public static String getEaseEmojiconKey(int i) {
        return EaseSmileUtils.KEY_START + i + EaseSmileUtils.KEY_END;
    }

    public static int getEaseEmojicon(int i) {
        String icon = "ee_" + i;
//        R.drawable drawables = new R.drawable();
        Context context = CoreZygote.getContext();
        if (context == null)
            return R.drawable.ee_11;
        return context.getResources().getIdentifier(icon, "drawable", context.getPackageName());
        //默认的id
//        int resId;
//        try {
//            //根据字符串字段名，取字段//根据资源的ID的变量名获得Field的对象,使用反射机制来实现的
//            Field field = R.drawable.class.getField(icon);
//            resId = (Integer) field.get(drawables);
//        } catch (Exception e) {
//            e.printStackTrace();
//            resId = R.drawable.ee_1;
//        }
//        return resId;
    }

    public static int getSmilesSize() {
        return emoticons.size();
    }


}
