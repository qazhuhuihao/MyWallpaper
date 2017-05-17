package cn.hhh.mywallpaper;

import android.util.Log;

/**
 * Created by zhanghongyang01 on 17/5/14.
 */

public class L {

    private static final String sTag = "WallPaper";

    public static void d(String msg, Object... params) {
        if (BuildConfig.DEBUG)
            Log.d(sTag, String.format(msg, params));

    }

}
