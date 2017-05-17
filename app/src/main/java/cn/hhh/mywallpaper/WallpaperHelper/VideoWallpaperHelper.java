package cn.hhh.mywallpaper.WallpaperHelper;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

import java.io.IOException;

import cn.hhh.mywallpaper.HWallpaper;
import cn.hhh.mywallpaper.L;
import cn.hhh.mywallpaper.R;
import cn.hhh.mywallpaper.manager.SPManager;

/**
 * 视频
 * Created by hhh on 2017/5/16.
 */

public class VideoWallpaperHelper implements WallpaperInterface {
    private MediaPlayer mMediaPlayer;

    @Override
    public void create(SurfaceHolder holder, Context context) {
        L.d("VideoWallpaper:create");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setSurface(holder.getSurface());
        try {
            String path = SPManager.getString(context, HWallpaper.VIDEO_PATH, context.getString(R.string.default_video));
            L.d("path:" + path);
            if (1 == SPManager.getInt(context, HWallpaper.VIDEO_ISDEFAULT, 1)) {
                AssetManager assetMg = context.getAssets();
                AssetFileDescriptor fileDescriptor = assetMg.openFd(path);
                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                        fileDescriptor.getStartOffset(), fileDescriptor.getLength());
            } else {
                mMediaPlayer.setDataSource(path);
            }
            mMediaPlayer.setLooping(true);
            mMediaPlayer.setVolume(0, 0);
            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(SurfaceHolder holder, Context context) {
        L.d("VideoWallpaper:start");
        if (null == mMediaPlayer)
            create(holder, context);
        mMediaPlayer.start();
    }

    @Override
    public void pause() {
        L.d("VideoWallpaper:pause");
        mMediaPlayer.pause();
    }

    @Override
    public void destroy() {
        L.d("VideoWallpaper:destroy");
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void voiceSilence() {
        mMediaPlayer.setVolume(0, 0);
    }

    public void voiceNormal() {
        mMediaPlayer.setVolume(1.0f, 1.0f);
    }

    public void changePath(String path) {
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
