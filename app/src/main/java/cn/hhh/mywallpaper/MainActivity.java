package cn.hhh.mywallpaper;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;

import cn.hhh.mywallpaper.databinding.ActivityMainBinding;
import cn.hhh.mywallpaper.manager.SPManager;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private static final int PERMISSIONS_REQUEST_CAMERA = 454;
    private static final int FILE_SELECT_CODE = 0;
    static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        init();
    }

    private void init() {
        binding.rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_camera:
                        binding.cbVoice.setEnabled(false);
                        binding.tvVideo.setEnabled(false);
                        binding.btDefault.setEnabled(false);
                        HWallpaper.cameraWallpaper(getApplicationContext());
                        SPManager.saveInt(getApplicationContext(), HWallpaper.KEY_MODEL, HWallpaper.MODEL_CAMERA);
                        break;
                    case R.id.rb_video:
                        binding.cbVoice.setEnabled(true);
                        binding.tvVideo.setEnabled(true);
                        binding.btDefault.setEnabled(true);
                        HWallpaper.videoWallpaper(getApplicationContext());
                        SPManager.saveInt(getApplicationContext(), HWallpaper.KEY_MODEL, HWallpaper.MODEL_VOICE);
                        break;
                }
            }
        });

        binding.cbVoice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println(isChecked);
                if (isChecked) {
                    HWallpaper.voiceSilence(getApplicationContext());
                    SPManager.saveInt(getApplicationContext(), HWallpaper.KEY_ACTION, HWallpaper.ACTION_VOICE_SILENCE);
                } else {
                    HWallpaper.voiceNormal(getApplicationContext());
                    SPManager.saveInt(getApplicationContext(), HWallpaper.KEY_ACTION, HWallpaper.ACTION_VOICE_NORMAL);
                }
            }
        });

        binding.tvVideo.setOnClickListener(onClickListener);
        binding.btSet.setOnClickListener(onClickListener);
        binding.btDefault.setOnClickListener(onClickListener);

        int model = SPManager.getInt(getApplicationContext(), HWallpaper.KEY_MODEL, HWallpaper.MODEL_VOICE);
        int action = SPManager.getInt(getApplicationContext(), HWallpaper.KEY_ACTION, HWallpaper.ACTION_VOICE_NORMAL);

        if (model == HWallpaper.MODEL_VOICE)
            binding.rbVideo.setChecked(true);

        if (action == HWallpaper.ACTION_VOICE_NORMAL)
            binding.cbVoice.setChecked(false);
        else
            binding.cbVoice.setChecked(true);

        String videoPath = SPManager.getString(getApplicationContext(), HWallpaper.VIDEO_PATH, getString(R.string.default_video));
        String videoName = new File(videoPath).getName();

        binding.tvVideo.setText(videoName);
        binding.tvVideo.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);

    }

    /**
     * 检查权限
     */
    void checkSelfPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), PERMISSION_CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{PERMISSION_CAMERA},
                    PERMISSIONS_REQUEST_CAMERA);
        } else {
            HWallpaper.setToWallPaper(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    HWallpaper.setToWallPaper(this);

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string._lease_open_permissions), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Potentially direct the user to the Market with a Dialog
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    L.d("File Uri: " + uri.toString());
                    // Get the path
                    String path = getPath(this, uri);
                    L.d("File Path: " + path);
                    // Get the file instance
                    // File file = new File(path);
                    // Initiate the upload

                    try {
                        String name = new File(path).getName();
                        binding.tvVideo.setText(name);
                        SPManager.saveString(getApplicationContext(), HWallpaper.VIDEO_PATH, path);
                        SPManager.saveInt(getApplicationContext(), HWallpaper.VIDEO_ISDEFAULT, 0);
                        HWallpaper.changePath(getApplicationContext(), path);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "路径错误", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
                cursor.close();
            } catch (Exception e) {
                // Eat it  Or Log it.
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tv_video:
                    showFileChooser();
                    break;
                case R.id.bt_default:
                    SPManager.saveString(getApplicationContext(), HWallpaper.VIDEO_PATH, getString(R.string.default_video));
                    SPManager.saveInt(getApplicationContext(), HWallpaper.VIDEO_ISDEFAULT, 1);
                    binding.tvVideo.setText(R.string.default_video);
                    break;
                case R.id.bt_set:
                    checkSelfPermission();
                    break;
            }
        }
    };
}
