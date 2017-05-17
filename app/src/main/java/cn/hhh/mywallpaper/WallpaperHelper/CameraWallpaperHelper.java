package cn.hhh.mywallpaper.WallpaperHelper;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;

import cn.hhh.mywallpaper.L;

/**
 * Created by hhh on 2017/5/16.
 */

public class CameraWallpaperHelper implements WallpaperInterface {
    private Camera camera;

    @Override
    public void create(SurfaceHolder holder, Context context) {
        L.d("CameraWallpaper:create");
    }

    @Override
    public void start(SurfaceHolder holder, Context context) {
        L.d("CameraWallpaper:start");
        camera = Camera.open();
        camera.setDisplayOrientation(90);

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.startPreview();
    }

    @Override
    public void pause() {
        L.d("CameraWallpaper:pause");
        if (camera != null) {
            try {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                // camera.lock();
                camera.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            camera = null;
        }
    }

    @Override
    public void destroy() {
        L.d("CameraWallpaper:destroy");
    }
}
