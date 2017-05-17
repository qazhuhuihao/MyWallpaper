package cn.hhh.mywallpaper.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author liujian
 */
public class SPManager {
    private static String TAG = SPManager.class.getSimpleName();
    private final static String SP_SELF_NAME = "config";

    private static SharedPreferences sp;

    /**
     * 保存字符串
     *
     * @param key   key
     * @param value value
     */
    public static void saveString(Context context, String key, String value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_SELF_NAME, 0);
        sp.edit().putString(key, value).apply();

    }

    /**
     * 保存int型
     *
     * @param key   key
     * @param value value
     */
    public static void saveInt(Context context, String key, int value) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_SELF_NAME, 0);
        sp.edit().putInt(key, value).apply();
    }

    /**
     * 获取字符值
     *
     * @param key      key
     * @param defValue defValue
     * @return String
     */
    public static String getString(Context context, String key, String defValue) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_SELF_NAME, 0);
        return sp.getString(key, defValue);
    }

    /**
     * 获取int值
     *
     * @param key      key
     * @param defValue defValue
     * @return int
     */
    public static int getInt(Context context, String key, int defValue) {
        if (sp == null)
            sp = context.getSharedPreferences(SP_SELF_NAME, 0);
        return sp.getInt(key, defValue);
    }

    public static void SPClean() {
        sp = null;
    }

}
