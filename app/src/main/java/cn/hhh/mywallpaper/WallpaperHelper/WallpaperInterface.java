package cn.hhh.mywallpaper.WallpaperHelper;

import android.content.Context;
import android.view.SurfaceHolder;

/**
 * Created by hhh on 2017/5/16.
 */

public interface WallpaperInterface {

    void create(SurfaceHolder holder, Context context);

    void start(SurfaceHolder holder, Context context);

    void pause();

    void destroy();
}
