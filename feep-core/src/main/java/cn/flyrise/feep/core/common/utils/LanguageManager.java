package cn.flyrise.feep.core.common.utils;

import java.util.Locale;

public class LanguageManager {

    /**
     * 语言类型，0代表简体中文
     */
    public static final int LANGUAGE_TYPE_CN = 0;

    /**
     * 语言类型，1代表繁体中文
     */
    public static final int LANGUAGE_TYPE_CN_FT = 1;

    /**
     * 语言类型，2代表英文
     */
    public static final int LANGUAGE_TYPE_EN = 2;

    private LanguageManager() {
    }

    /**
     * 语言类型，0代表简体中文,1代表繁体中文,语言类型，2代表英文
     */
    public static int getCurrentLanguage() {
        final String language = getLanguage();
        final String country = getCountry();
        if (language.equalsIgnoreCase("en")) {
            return LANGUAGE_TYPE_EN;
        }
        else if (language.equalsIgnoreCase("zh")) {
            if (country.equalsIgnoreCase("cn")) {
                return LANGUAGE_TYPE_CN;
            }
            else {
                return LANGUAGE_TYPE_CN_FT;
            }
        }
        else {
            return LANGUAGE_TYPE_CN;
        }
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 判断当前语言是否是中文
     */
    public static boolean isChinese() {
        return (getCurrentLanguage() == LANGUAGE_TYPE_CN || getCurrentLanguage() == LANGUAGE_TYPE_CN_FT);
    }
}
