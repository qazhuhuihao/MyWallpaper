package cn.hhh.mywallpaper;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.view.SurfaceHolder;

import cn.hhh.mywallpaper.WallpaperHelper.CameraWallpaperHelper;
import cn.hhh.mywallpaper.WallpaperHelper.VideoWallpaperHelper;
import cn.hhh.mywallpaper.WallpaperHelper.WallpaperInterface;
import cn.hhh.mywallpaper.manager.SPManager;

/**
 * Created by hhh on 2017/5/16.
 */

public class HWallpaper extends WallpaperService {

    public Engine onCreateEngine() {
        return new MagicEngine();
    }

    public static final String VIDEO_PARAMS_CONTROL_ACTION = "com.hhh.wallpaper";
    public static final String KEY_MODEL = "model";
    public static final String KEY_ACTION = "action";
    public static final String VIDEO_PATH = "path";
    public static final String VIDEO_ISDEFAULT = "isDefault";
    public static final int MODEL_CAMERA = 200;
    public static final int MODEL_VOICE = 100;
    public static final int ACTION_VOICE_SILENCE = 110;
    public static final int ACTION_VOICE_NORMAL = 111;
    public static final int ACTION_VOICE_PATH = 120;

    public static void voiceSilence(Context context) {
        Intent intent = new Intent(HWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(HWallpaper.KEY_ACTION, HWallpaper.ACTION_VOICE_SILENCE);
        context.sendBroadcast(intent);
    }

    public static void voiceNormal(Context context) {
        Intent intent = new Intent(HWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(HWallpaper.KEY_ACTION, HWallpaper.ACTION_VOICE_NORMAL);
        context.sendBroadcast(intent);
    }

    public static void videoWallpaper(Context context) {
        Intent intent = new Intent(HWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(HWallpaper.KEY_MODEL, HWallpaper.MODEL_VOICE);
        context.sendBroadcast(intent);
    }

    public static void cameraWallpaper(Context context) {
        Intent intent = new Intent(HWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(HWallpaper.KEY_MODEL, HWallpaper.MODEL_CAMERA);
        context.sendBroadcast(intent);
    }

    public static void setToWallPaper(Context context) {
        final Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
        intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                new ComponentName(context, HWallpaper.class));
        context.startActivity(intent);
    }

    public static void changePath(Context context, String path) {
        Intent intent = new Intent(HWallpaper.VIDEO_PARAMS_CONTROL_ACTION);
        intent.putExtra(HWallpaper.KEY_ACTION, path);
        context.sendBroadcast(intent);
    }

    private class MagicEngine extends Engine {
        private WallpaperInterface wallpaperHelper;
        private BroadcastReceiver mVideoParamsControlReceiver;

        @Override
        public void onCreate(final SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            L.d("VideoEngine#onCreate");

            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);
            registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    L.d("onReceive");

                    int model = intent.getIntExtra(KEY_MODEL, -1);

                    switch (model) {
                        case MODEL_CAMERA:
                            if (null == wallpaperHelper) {
                                wallpaperHelper = new CameraWallpaperHelper();
                                wallpaperHelper.create(surfaceHolder, getApplicationContext());
                            } else if (wallpaperHelper instanceof VideoWallpaperHelper) {
                                wallpaperHelper.pause();
                                wallpaperHelper.destroy();
                                wallpaperHelper = new CameraWallpaperHelper();
                                wallpaperHelper.create(surfaceHolder, getApplicationContext());
                            }
                            return;
                        case MODEL_VOICE:
                            if (null == wallpaperHelper) {
                                wallpaperHelper = new VideoWallpaperHelper();
                                wallpaperHelper.create(surfaceHolder, getApplicationContext());
                            } else {
                                wallpaperHelper.pause();
                                wallpaperHelper.destroy();
                                wallpaperHelper = new VideoWallpaperHelper();
                                wallpaperHelper.create(surfaceHolder, getApplicationContext());
                            }
                            return;
                    }

                    if (null != wallpaperHelper && wallpaperHelper instanceof VideoWallpaperHelper) {

                        int action = intent.getIntExtra(KEY_ACTION, -1);

                        switch (action) {
                            case ACTION_VOICE_NORMAL:
                                ((VideoWallpaperHelper) wallpaperHelper).voiceNormal();
                                break;
                            case ACTION_VOICE_SILENCE:
                                ((VideoWallpaperHelper) wallpaperHelper).voiceSilence();
                                break;
                        }

                        String path = intent.getStringExtra(VIDEO_PATH);
                        if (!TextUtils.isEmpty(path))
                            ((VideoWallpaperHelper) wallpaperHelper).changePath(path);
                    }
                }
            }, intentFilter);


        }

        @Override
        public void onDestroy() {
            L.d("VideoEngine#onDestroy");
            unregisterReceiver(mVideoParamsControlReceiver);
            super.onDestroy();

        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            L.d("VideoEngine#onVisibilityChanged visible = " + visible);
            if (visible) {
                wallpaperHelper.start(getSurfaceHolder(), getApplicationContext());
            } else {
                wallpaperHelper.pause();
            }
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            L.d("VideoEngine#onSurfaceCreated ");
            super.onSurfaceCreated(holder);

            int model = SPManager.getInt(getApplicationContext(), KEY_MODEL, MODEL_VOICE);

            switch (model) {
                case MODEL_CAMERA:
                    wallpaperHelper = new CameraWallpaperHelper();
                    break;
                case MODEL_VOICE:
                    wallpaperHelper = new VideoWallpaperHelper();
                    break;
            }

            wallpaperHelper.create(holder, getApplicationContext());

        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            L.d("VideoEngine#onSurfaceDestroyed ");
            super.onSurfaceDestroyed(holder);
            wallpaperHelper.destroy();

        }

    }

//    private class VideoEngine extends Engine {
//
//        private MediaPlayer mMediaPlayer;
//
//        private BroadcastReceiver mVideoParamsControlReceiver;
//
//        @Override
//        public void onCreate(SurfaceHolder surfaceHolder) {
//            super.onCreate(surfaceHolder);
//            L.d("VideoEngine#onCreate");
//
//            IntentFilter intentFilter = new IntentFilter(VIDEO_PARAMS_CONTROL_ACTION);
//            registerReceiver(mVideoParamsControlReceiver = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    L.d("onReceive");
//                    int action = intent.getIntExtra(KEY_ACTION, -1);
//
//                    switch (action) {
//                        case ACTION_VOICE_NORMAL:
//                            mMediaPlayer.setVolume(1.0f, 1.0f);
//                            break;
//                        case ACTION_VOICE_SILENCE:
//                            mMediaPlayer.setVolume(0, 0);
//                            break;
//
//                    }
//                }
//            }, intentFilter);
//
//
//        }
//
//        @Override
//        public void onDestroy() {
//            L.d("VideoEngine#onDestroy");
//            unregisterReceiver(mVideoParamsControlReceiver);
//            super.onDestroy();
//
//        }
//
//        @Override
//        public void onVisibilityChanged(boolean visible) {
//            L.d("VideoEngine#onVisibilityChanged visible = " + visible);
//            if (visible) {
//                mMediaPlayer.start();
//            } else {
//                mMediaPlayer.pause();
//            }
//        }
//
//
//        @Override
//        public void onSurfaceCreated(SurfaceHolder holder) {
//            L.d("VideoEngine#onSurfaceCreated ");
//            super.onSurfaceCreated(holder);
//            mMediaPlayer = new MediaPlayer();
//            mMediaPlayer.setSurface(holder.getSurface());
//            try {
//                AssetManager assetMg = getApplicationContext().getAssets();
//                AssetFileDescriptor fileDescriptor = assetMg.openFd("test1.mp4");
//                mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
//                        fileDescriptor.getStartOffset(), fileDescriptor.getLength());
//                mMediaPlayer.setLooping(true);
//                mMediaPlayer.setVolume(0, 0);
//                mMediaPlayer.prepare();
//                mMediaPlayer.start();
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//        }
//
//        @Override
//        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            L.d("VideoEngine#onSurfaceChanged ");
//            super.onSurfaceChanged(holder, format, width, height);
//        }
//
//        @Override
//        public void onSurfaceDestroyed(SurfaceHolder holder) {
//            L.d("VideoEngine#onSurfaceDestroyed ");
//            super.onSurfaceDestroyed(holder);
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//
//        }
//    }
//
//    private class CameraEngine extends Engine implements Camera.PreviewCallback {
//        private Camera camera;
//
//        @Override
//        public void onCreate(SurfaceHolder surfaceHolder) {
//            super.onCreate(surfaceHolder);
//
//            startPreview();
//            // 设置处理触摸事件
//            setTouchEventsEnabled(true);
//
//        }
//
//        @Override
//        public void onTouchEvent(MotionEvent event) {
//            super.onTouchEvent(event);
//            // 时间处理:点击拍照,长按拍照
//        }
//
//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//            stopPreview();
//        }
//
//        @Override
//        public void onVisibilityChanged(boolean visible) {
//            if (visible) {
//                startPreview();
//            } else {
//                stopPreview();
//            }
//        }
//
//        /**
//         * 开始预览
//         */
//        void startPreview() {
//            camera = Camera.open();
//            camera.setDisplayOrientation(90);
//
//            try {
//                camera.setPreviewDisplay(getSurfaceHolder());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            camera.startPreview();
//
//        }
//
//        /**
//         * 停止预览
//         */
//        void stopPreview() {
//            if (camera != null) {
//                try {
//                    camera.stopPreview();
//                    camera.setPreviewCallback(null);
//                    // camera.lock();
//                    camera.release();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                camera = null;
//            }
//        }
//
//        @Override
//        public void onPreviewFrame(byte[] bytes, Camera camera) {
//            camera.addCallbackBuffer(bytes);
//        }
//    }
}
