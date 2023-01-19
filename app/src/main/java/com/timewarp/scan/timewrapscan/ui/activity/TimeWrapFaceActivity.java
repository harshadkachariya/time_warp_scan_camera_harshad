package com.timewarp.scan.timewrapscan.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.MenuItem;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.PointerIconCompat;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.example.timewarpscanfacefilter.ui.activity.PreviewActivity;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.enums.CAPTURE_MODE;
import com.timewarp.scan.timewrapscan.enums.WARP_DIRECTION;
import com.timewarp.scan.timewrapscan.utils.OnSwipeTouchListener;
import com.timewarp.scan.timewrapscan.utils.Utils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class TimeWrapFaceActivity extends BaseActivity implements CameraXConfig.Provider {

    ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraSelector cameraSelector;
    Dialog dialogLoading;
    ImageAnalysis imageAnalysis;
    LinearLayout llCameraSelection;
    LinearLayout llPhoto;
    LinearLayout llVideo;
    Camera mCamera;
    ObjectAnimator ofFloat;
    ObjectAnimator ofFloat2;
    Preview preview;
    public PreviewView previewView;
    TextView tvPhoto;
    TextView tvVideo;
    Timer GIFTimer = null;
    private final int REQUEST_CODE_PERMISSIONS = PointerIconCompat.TYPE_CONTEXT_MENU;
    private final String[] REQUIRED_PERMISSIONS = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    ConstraintLayout beforeCaptureUI = null;
    boolean capture = false;
    CAPTURE_MODE captureMode = CAPTURE_MODE.PHOTO;
    ConstraintLayout captureUI = null;
    int facing = 0;
    Uri fileURI = null;
    int frameRate = 2;
    ImageView imageView = null;
    boolean isSwitching = false;
    int lineCount = 0;
    int lineResolution = 50;
    ImageView previewViewImageView = null;
    int resolutionX = 480;
    int resolutionY = 640;
    public static Bitmap resultBitmap = null;
    public static List<Bitmap> resultBitmapList = null;
    ConstraintLayout resultUI = null;
    Bitmap subBitmap = null;
    ConstraintLayout tutorialUI = null;
    public WARP_DIRECTION warpDirection = WARP_DIRECTION.DOWN;
    ImageView iv_image, iv_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_time_wrap_face);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        PreviewView previewView = (PreviewView) findViewById(R.id.previewView);
        this.previewView = previewView;
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        this.previewViewImageView = (ImageView) findViewById(R.id.previewView_ImageView);
        this.cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        ImageAnalysis build = new ImageAnalysis.Builder().setTargetResolution(new Size(480, 640)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        this.imageAnalysis = build;
        build.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageCapture());
        this.cameraProviderFuture.addListener(new Runnable() { // from class: time.warp.camera.photo.filter.Activity.TimeWrapFaceActivity.1
            @Override // java.lang.Runnable
            public final void run() {
                TimeWrapFaceActivity.this.lambda$onCreate$0$MainActivity();
            }
        }, ContextCompat.getMainExecutor(this));
        findViewById(R.id.FIREBASE_TEST_BUTTON1).setOnClickListener(new View.OnClickListener() { // from class: time.warp.camera.photo.filter.Activity.TimeWrapFaceActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                try {
                    TimeWrapFaceActivity.this.hideTutorialUI();
                    TimeWrapFaceActivity.this.ofFloat.pause();
                    TimeWrapFaceActivity.this.ofFloat2.pause();
                    TimeWrapFaceActivity timeWrapFaceActivity = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity.startCapture(timeWrapFaceActivity.mCamera, WARP_DIRECTION.RIGHT);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.FIREBASE_TEST_BUTTON2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    TimeWrapFaceActivity.this.hideTutorialUI();
                    TimeWrapFaceActivity.this.ofFloat.pause();
                    TimeWrapFaceActivity.this.ofFloat2.pause();
                    TimeWrapFaceActivity timeWrapFaceActivity = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity.startCapture(timeWrapFaceActivity.mCamera, WARP_DIRECTION.DOWN);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        this.iv_image = findViewById(R.id.iv_image);
        this.iv_video = findViewById(R.id.iv_video);
        this.beforeCaptureUI = (ConstraintLayout) findViewById(R.id.before_capture_UI);
        this.captureUI = (ConstraintLayout) findViewById(R.id.capture_UI);
        this.resultUI = (ConstraintLayout) findViewById(R.id.result_UI);
        this.tutorialUI = (ConstraintLayout) findViewById(R.id.tutorial_UI);
        this.resultBitmapList = new ArrayList();
        this.imageView = (ImageView) findViewById(R.id.result_imageView);
        this.llCameraSelection = (LinearLayout) findViewById(R.id.llCameraSelection);
        this.llPhoto = (LinearLayout) findViewById(R.id.llPhoto);
        this.llVideo = (LinearLayout) findViewById(R.id.llVideo);
        this.tvPhoto = (TextView) findViewById(R.id.tvPhoto);
        this.tvVideo = (TextView) findViewById(R.id.tvVideo);
        this.llPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_image.setImageResource(R.drawable.ic_imgage_black);
                iv_video.setImageResource(R.drawable.ic_video_yellow);

                TimeWrapFaceActivity.this.llPhoto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.blackColor)));
                TimeWrapFaceActivity.this.llVideo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.white)));
                TimeWrapFaceActivity.this.tvPhoto.setTextColor(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.white));
                TimeWrapFaceActivity.this.tvVideo.setTextColor(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.blackColor));
                TimeWrapFaceActivity.this.captureMode = CAPTURE_MODE.PHOTO;
            }
        });
        this.llVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iv_image.setImageResource(R.drawable.ic_imgage_yellow);
                iv_video.setImageResource(R.drawable.ic_video_black);

                TimeWrapFaceActivity.this.llPhoto.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.white)));
                TimeWrapFaceActivity.this.llVideo.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.blackColor)));
                TimeWrapFaceActivity.this.tvPhoto.setTextColor(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.blackColor));
                TimeWrapFaceActivity.this.tvVideo.setTextColor(ContextCompat.getColor(TimeWrapFaceActivity.this, R.color.white));
                TimeWrapFaceActivity.this.captureMode = CAPTURE_MODE.VIDEO;
            }
        });
        this.imageView.setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override
            public void onSwipeLeft() throws InterruptedException {
            }

            @Override
            public void onSwipeTop() {
            }

            @Override
            public void onSwipeRight() throws InterruptedException {
                if (TimeWrapFaceActivity.this.beforeCaptureUI.getVisibility() == View.VISIBLE) {
                    TimeWrapFaceActivity.this.hideTutorialUI();
                    TimeWrapFaceActivity.this.ofFloat.pause();
                    TimeWrapFaceActivity.this.ofFloat2.pause();

                    TimeWrapFaceActivity timeWrapFaceActivity = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity.startCapture(timeWrapFaceActivity.mCamera, WARP_DIRECTION.RIGHT);
                }
            }

            @Override
            public void onSwipeBottom() throws InterruptedException {
                if (TimeWrapFaceActivity.this.beforeCaptureUI.getVisibility() == View.VISIBLE) {
                    TimeWrapFaceActivity.this.hideTutorialUI();
                    TimeWrapFaceActivity.this.ofFloat.pause();
                    TimeWrapFaceActivity.this.ofFloat2.pause();
                    TimeWrapFaceActivity timeWrapFaceActivity = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity.startCapture(timeWrapFaceActivity.mCamera, WARP_DIRECTION.DOWN);
                }
            }
        });
        final Button button = (Button) findViewById(R.id.save_Button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                TimeWrapFaceActivity.this.lambda$onCreate$2$MainActivity(button, view);
            }
        });
        findViewById(R.id.resultCancel_Button).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                TimeWrapFaceActivity.this.lambda$onCreate$5$MainActivity(view);
            }
        });
        findViewById(R.id.switchCamera_ImageView).setOnClickListener(new View.OnClickListener() {
            @Override
            public final void onClick(View view) {
                TimeWrapFaceActivity.this.lambda$onCreate$6$MainActivity(view);
            }
        });

        showTutorialUI();
        showTutorial();
        this.resolutionY = 640;
        this.resolutionX = 480;

    }

    public void lambda$onCreate$0$MainActivity() {
        try {
            this.cameraProvider = this.cameraProviderFuture.get();
            if (allPermissionsGranted()) {
                bindPreview(this.cameraProvider);
            } else {
                ActivityCompat.requestPermissions(this, this.REQUIRED_PERMISSIONS, PointerIconCompat.TYPE_CONTEXT_MENU);
            }
        } catch (InterruptedException | ExecutionException unused) {
        }
    }

    public void lambda$onCreate$2$MainActivity(Button button, View view) {
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        if (this.captureMode == CAPTURE_MODE.PHOTO) {
            initProgress();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override // java.lang.Runnable
                public final void run() {
                    TimeWrapFaceActivity.this.m1660x99b61bb6();
                }
            });
        } else if (this.captureMode == CAPTURE_MODE.VIDEO) {
            initProgress();
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public final void run() {
                    TimeWrapFaceActivity.this.m1661xea15cb8();
                }
            });
        }
        if (this.fileURI != null) {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{this.fileURI.getPath()}, new String[]{"image/jpeg"}, null);
        }
        button.setClickable(true);
    }

    public void m1660x99b61bb6() {
        try {
            this.fileURI = saveBitmapInGalary(this.resultBitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TimeWrapFaceActivity.this.m1659xdf407b35();
            }
        });
    }


    public void m1659xdf407b35() {
        Log.e("Done", "Done");
        if (this.dialogLoading.isShowing()) {
            this.dialogLoading.dismiss();
        }
        APIManager.showInter(TimeWrapFaceActivity.this, false, isfail -> {
            Intent intent = new Intent(this, ShareScreenPhotoActivity.class);
            intent.putExtra("sharePath", this.fileURI.getPath());
            startActivity(intent);
            finish();
        });
    }

    public void m1661xea15cb8() {
        try {
            getImagesFromBitmaps(this.resultBitmapList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void lambda$onCreate$5$MainActivity(View view) {
        resumeToBeforeCaptureUI();
    }

    public void lambda$onCreate$6$MainActivity(View view) {
        this.isSwitching = true;
        if (this.facing == 0) {
            setFacing(1);
        } else {
            setFacing(0);
        }
        bindPreview(this.cameraProvider);
        this.isSwitching = false;
    }

    private boolean allPermissionsGranted() {
        for (String str : this.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != 1001) {
            return;
        }
        if (allPermissionsGranted()) {
            bindPreview(this.cameraProvider);
            return;
        }
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void setFacing(int i) {
        this.facing = i;
    }

    public Bitmap overlay(Bitmap bitmap, Bitmap bitmap2, int i, WARP_DIRECTION warp_direction) {
        new Matrix().preScale(1.0f, -1.0f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        if (warp_direction == WARP_DIRECTION.DOWN) {
            canvas.drawBitmap(bitmap2, 0.0f, i, (Paint) null);
        }
        if (warp_direction == WARP_DIRECTION.RIGHT) {
            canvas.drawBitmap(bitmap2, i, 0.0f, (Paint) null);
        }
        return createBitmap;
    }

    public Bitmap overlay(Bitmap bitmap, Bitmap bitmap2) {
        new Matrix().preScale(1.0f, -1.0f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(createBitmap);
        new Paint().setColor(-1);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        return createBitmap;
    }

    public static Bitmap MirrorBitmap(Bitmap bitmap, int i, int i2) {
        Matrix matrix = new Matrix();
        matrix.preScale(i, i2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.setRotate(i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    public void drawScanEffect(List<Bitmap> list, WARP_DIRECTION warp_direction) {
        for (int i = 0; i < list.size(); i++) {
            Canvas canvas = new Canvas(list.get(i));
            Paint paint = new Paint();
            paint.setStrokeWidth(10.0f);
            paint.setColor(ContextCompat.getColor(this, R.color.blackColor));
            if (warp_direction == WARP_DIRECTION.DOWN) {
                canvas.drawLine(0.0f, this.lineResolution * i, list.get(0).getWidth(), this.lineResolution * i, paint);
            } else if (warp_direction == WARP_DIRECTION.RIGHT) {
                float f = this.lineResolution * i;
                canvas.drawLine(f, 0.0f, f, list.get(0).getHeight(), paint);
            }
            canvas.drawBitmap(list.get(i), 0.0f, 0.0f, (Paint) null);
        }
    }

    public void drawScanEffect(Bitmap bitmap, WARP_DIRECTION warp_direction, int i) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setStrokeWidth(10.0f);
        paint.setColor(ContextCompat.getColor(this, R.color.blackColor));
        if (warp_direction == WARP_DIRECTION.DOWN) {
            float f = i + 5;
            canvas.drawLine(0.0f, f, bitmap.getWidth(), f, paint);
        } else if (warp_direction == WARP_DIRECTION.RIGHT) {
            float f2 = i + 5;
            canvas.drawLine(f2, 0.0f, f2, bitmap.getHeight(), paint);
        }
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
    }

    public void initializeImageView() {
        Bitmap createBitmap = Bitmap.createBitmap(this.resolutionX, this.resolutionY, Bitmap.Config.ARGB_8888);
        this.resultBitmap = createBitmap;
        createBitmap.eraseColor(0);
        this.imageView.setImageBitmap(this.resultBitmap);
    }

    private Uri saveBitmapInGalary(Bitmap bitmap) {
        Utils.isFolderCreated();
        String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
        File file2 = new File(file + "/TimeWarpFaceFilter/Photo");
        if (!file2.exists()) {
            file2.mkdirs();
        }
        String format = new SimpleDateFormat("" + System.currentTimeMillis(), Locale.US).format(new Date());
        File file3 = new File(file2, "WarpPhoto_" + format + ".jpg");
        if (file3.exists()) {
            file3.delete();
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file3);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.fromFile(file3);
    }

    private File getImagesFromBitmaps(final List<Bitmap> list) {
        Utils.isFolderCreated();
        String file = getApplicationContext().getExternalFilesDir("").toString();
        final File file2 = new File(file + "/TimeWarpFaceFilter/temp");
        if (file2.exists()) {
            file2.delete();
        } else {
            file2.mkdirs();
        }
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public final void run() {
                TimeWrapFaceActivity.this.m1658xa9d555e0(list, file2);
            }
        });
        return new File(file);
    }

    public void m1658xa9d555e0(List list, final File file) {
        String str;
        int i = 0;
        while (i < list.size()) {
            int i2 = i + 1;
            if (i2 < 10) {
                str = "00".concat(String.valueOf(i2));
            } else if (i < 99) {
                str = "0".concat(String.valueOf(i2));
            } else {
                str = String.valueOf(i2);
            }
            String str2 = file + "/image" + str + ".jpg";
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(new File(str2));
                ((Bitmap) list.get(i)).compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("HHHH", "" + str2);
            i = i2;
        }
        runOnUiThread(new Runnable() {
            @Override
            public final void run() {
                TimeWrapFaceActivity.this.m1657xef5fb55f(file);
            }
        });
    }

    public void m1657xef5fb55f(File file) {
        Log.e("Done", "Done");
        saveImageAsVideo(file.getAbsolutePath() + "/image%03d.jpg");
    }

    public void saveImageAsVideo(String str) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "TimeWarpFaceFilter/Video");
            file.mkdirs();
            String format = new SimpleDateFormat("" + System.currentTimeMillis(), Locale.US).format(new Date());
            final File file2 = new File(file, "WarpVideo_" + format + ".mp4");
            FFmpeg.executeAsync(new String[]{"-i", str, file2.getAbsolutePath()}, new ExecuteCallback() {
                @Override
                public void apply(long j, int i) {
                    if (i == 0) {
                        File file3 = new File(TimeWrapFaceActivity.this.getApplicationContext().getExternalFilesDir("") + "/TimeWarpFaceFilter/temp");
                        if (file3.exists()) {
                            file3.delete();
                        }
                        if (TimeWrapFaceActivity.this.dialogLoading != null && TimeWrapFaceActivity.this.dialogLoading.isShowing()) {
                            TimeWrapFaceActivity.this.dialogLoading.dismiss();
                        }
                        TimeWrapFaceActivity.this.fileURI = Uri.fromFile(file2);
                        if (TimeWrapFaceActivity.this.fileURI != null) {
                            MediaScannerConnection.scanFile(TimeWrapFaceActivity.this.getApplicationContext(), new String[]{TimeWrapFaceActivity.this.fileURI.getPath()}, new String[]{"image/jpeg"}, null);
                        }
                        APIManager.showInter(TimeWrapFaceActivity.this, false, isfail -> {
                            Intent intent = new Intent(TimeWrapFaceActivity.this, ShareScreenVideoActivity.class);
                            intent.putExtra("shareVideoPath", file2.getAbsolutePath());
                            TimeWrapFaceActivity.this.startActivity(intent);
                            TimeWrapFaceActivity.this.finish();
                        });

                        Log.e("HHHH", "Async command execution completed successfully.");
                    } else if (i == 255) {
                        Log.e("HHHH", "Async command execution cancelled by user.");
                    } else {
                        if (TimeWrapFaceActivity.this.dialogLoading != null && TimeWrapFaceActivity.this.dialogLoading.isShowing()) {
                            TimeWrapFaceActivity.this.dialogLoading.dismiss();
                        }
                        Log.e("HHHH", String.format("Async command execution failed with returnCode=%d.", Integer.valueOf(i)));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initProgress() {
        Dialog dialog = new Dialog(this);
        this.dialogLoading = dialog;
        dialog.setContentView(R.layout.loading_dialog);
        this.dialogLoading.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        this.dialogLoading.setCancelable(false);
        this.dialogLoading.show();
    }

    private void showBeforeCaptureUI() {
        this.beforeCaptureUI.setVisibility(View.VISIBLE);
    }

    private void hideBeforeCaptureUI() {
        this.beforeCaptureUI.setVisibility(View.INVISIBLE);
    }

    private void showCaptureUI() {
        this.captureUI.setVisibility(View.VISIBLE);
    }

    private void hideCaptureUI() {
        this.captureUI.setVisibility(View.INVISIBLE);
    }

    private void showResultUI() {
        this.resultUI.setVisibility(View.VISIBLE);
    }

    private void hideResultUI() {
        this.resultUI.setVisibility(View.INVISIBLE);
    }

    private void showTutorialUI() {
        this.tutorialUI.setVisibility(View.VISIBLE);
    }

    public void hideTutorialUI() {
        this.tutorialUI.setVisibility(View.INVISIBLE);
    }

    public int getRandomColor() {
        Random random = new Random();
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public Drawable getTintedDrawable(Resources resources, int i) {
        Drawable drawable = resources.getDrawable(i);
        drawable.setColorFilter(getRandomColor(), PorterDuff.Mode.MULTIPLY);
        return drawable;
    }

    private void resumeToBeforeCaptureUI() {
        Timer timer = this.GIFTimer;
        if (timer != null) {
            timer.cancel();
        }
        this.lineCount = 0;
        this.resultBitmap = null;
        this.resultBitmapList = null;
        this.llCameraSelection.setVisibility(View.VISIBLE);
        findViewById(R.id.switchCamera_ImageView).setVisibility(View.VISIBLE);
        initializeImageView();
        hideResultUI();
        hideCaptureUI();
        showBeforeCaptureUI();
        showTutorialUI();
        this.ofFloat.resume();
        this.ofFloat2.resume();
        this.capture = false;
    }

    public void startCapture(Camera camera, WARP_DIRECTION warp_direction) throws InterruptedException {
        this.warpDirection = warp_direction;
        this.lineResolution = 2;
        this.lineCount = 0;
        this.resultBitmap = null;
        this.resultBitmapList = null;
        findViewById(R.id.switchCamera_ImageView).setVisibility(View.GONE);
        this.llCameraSelection.setVisibility(View.GONE);
        initializeImageView();
        hideResultUI();
        hideBeforeCaptureUI();
        showCaptureUI();
        this.capture = true;
    }

    private void showTutorial() {
        final ImageView imageView = (ImageView) findViewById(R.id.tutorialHandDown_ImageView);
        final ImageView imageView2 = (ImageView) findViewById(R.id.tutorialHandRight_ImageView);
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        defaultDisplay.getSize(point);
        int i = point.x;
        int i2 = point.y;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(imageView2, "translationX", i / 3);
        this.ofFloat = ofFloat;
        ofFloat.setDuration(1000L);
        this.ofFloat.setRepeatMode(ValueAnimator.RESTART);
        this.ofFloat.start();
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(imageView, "translationY", i2 / 3);
        this.ofFloat2 = ofFloat2;
        ofFloat2.setDuration(1000L);
        this.ofFloat2.setRepeatMode(ValueAnimator.RESTART);
        this.ofFloat.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                imageView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (TimeWrapFaceActivity.this.tutorialUI.getVisibility() == View.VISIBLE) {
                    imageView.setVisibility(View.VISIBLE);
                    TimeWrapFaceActivity.this.ofFloat2.start();
                }
            }
        });
        this.ofFloat2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }

            @Override
            public void onAnimationStart(Animator animator) {
                imageView2.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (TimeWrapFaceActivity.this.tutorialUI.getVisibility() == View.VISIBLE) {
                    imageView2.setVisibility(View.VISIBLE);
                    TimeWrapFaceActivity.this.ofFloat.start();
                }
            }
        });
    }

    public Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        ByteBuffer buffer2 = planes[1].getBuffer();
        ByteBuffer buffer3 = planes[2].getBuffer();
        int remaining = buffer.remaining();
        int remaining2 = buffer2.remaining();
        int remaining3 = buffer3.remaining();
        byte[] bArr = new byte[remaining + remaining2 + remaining3];
        buffer.get(bArr, 0, remaining);
        buffer3.get(bArr, remaining, remaining3);
        buffer2.get(bArr, remaining + remaining3, remaining2);
        YuvImage yuvImage = new YuvImage(bArr, 17, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }

    @Override
    public CameraXConfig getCameraXConfig() {
        return Camera2Config.defaultConfig();
    }

    public void bindPreview(ProcessCameraProvider processCameraProvider) {
        processCameraProvider.unbindAll();
        this.preview = new Preview.Builder().setTargetResolution(new Size(480, 640)).build();
        this.cameraSelector = new CameraSelector.Builder().requireLensFacing(this.facing).build();
        ImageAnalysis build = new ImageAnalysis.Builder().setTargetResolution(new Size(480, 640)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        this.imageAnalysis = build;
        build.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageCapture());
        this.preview.setSurfaceProvider(this.previewView.getSurfaceProvider());
        this.mCamera = processCameraProvider.bindToLifecycle(this, this.cameraSelector, this.preview);
        processCameraProvider.bindToLifecycle(this, this.cameraSelector, this.imageAnalysis, this.preview);
    }

    public class ImageCapture implements ImageAnalysis.Analyzer {
        private ImageCapture() {
        }

        @SuppressLint("UnsafeOptInUsageError")
        @Override
        public void analyze(ImageProxy imageProxy) {
            Bitmap bitmap;
            if (TimeWrapFaceActivity.this.previewView.getPreviewStreamState().getValue() == PreviewView.StreamState.STREAMING && TimeWrapFaceActivity.this.previewView.getChildAt(0).getClass() == TextureView.class) {
                bitmap = ((TextureView) TimeWrapFaceActivity.this.previewView.getChildAt(0)).getBitmap(TimeWrapFaceActivity.this.resolutionX, TimeWrapFaceActivity.this.resolutionY);
            } else if (imageProxy.getFormat() == 35) {
                TimeWrapFaceActivity timeWrapFaceActivity = TimeWrapFaceActivity.this;
                bitmap = timeWrapFaceActivity.rotateBitmap(timeWrapFaceActivity.toBitmap(imageProxy.getImage()), 90);
                if (TimeWrapFaceActivity.this.facing == 0) {
                    bitmap = TimeWrapFaceActivity.MirrorBitmap(bitmap, 1, -1);
                }
            } else {
                bitmap = null;
            }
            if (bitmap == null) {
                imageProxy.close();
                return;
            }
            if ((TimeWrapFaceActivity.this.lineCount >= TimeWrapFaceActivity.this.resolutionY || TimeWrapFaceActivity.this.warpDirection != WARP_DIRECTION.DOWN) && !((TimeWrapFaceActivity.this.lineCount < TimeWrapFaceActivity.this.resolutionX && TimeWrapFaceActivity.this.warpDirection == WARP_DIRECTION.RIGHT && TimeWrapFaceActivity.this.facing == 0) || (TimeWrapFaceActivity.this.lineCount < TimeWrapFaceActivity.this.resolutionX && TimeWrapFaceActivity.this.warpDirection == WARP_DIRECTION.RIGHT && TimeWrapFaceActivity.this.facing == 1))) {
                if (TimeWrapFaceActivity.this.capture) {
                    if (TimeWrapFaceActivity.this.captureMode == CAPTURE_MODE.VIDEO) {
                        TimeWrapFaceActivity timeWrapFaceActivity2 = TimeWrapFaceActivity.this;
                        timeWrapFaceActivity2.drawScanEffect(timeWrapFaceActivity2.resultBitmapList, TimeWrapFaceActivity.this.warpDirection);
//                        TimeWrapFaceActivity.this.playGIF();
                    }
                    TimeWrapFaceActivity.this.stopCapture();
                    APIManager.showInter(TimeWrapFaceActivity.this, false, isfail -> {
                        Intent intent = new Intent(TimeWrapFaceActivity.this, PreviewActivity.class);
                        intent.putExtra("capturemode", TimeWrapFaceActivity.this.captureMode.name());
                        startActivityForResult(intent, 12);
                    });
                }
            } else if (TimeWrapFaceActivity.this.capture) {
                long currentTimeMillis = System.currentTimeMillis();
                if (TimeWrapFaceActivity.this.resultBitmap == null) {
                    TimeWrapFaceActivity.this.initializeImageView();
                }
                if (TimeWrapFaceActivity.this.resultBitmapList == null) {
                    TimeWrapFaceActivity.this.resultBitmapList = new ArrayList();
                }
                if (TimeWrapFaceActivity.this.warpDirection == WARP_DIRECTION.DOWN) {
                    TimeWrapFaceActivity timeWrapFaceActivity3 = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity3.subBitmap = Bitmap.createBitmap(bitmap, 0, timeWrapFaceActivity3.lineCount, TimeWrapFaceActivity.this.resolutionX, TimeWrapFaceActivity.this.lineResolution);
                } else if (TimeWrapFaceActivity.this.warpDirection == WARP_DIRECTION.RIGHT) {
                    TimeWrapFaceActivity timeWrapFaceActivity4 = TimeWrapFaceActivity.this;
                    timeWrapFaceActivity4.subBitmap = Bitmap.createBitmap(bitmap, timeWrapFaceActivity4.lineCount, 0, TimeWrapFaceActivity.this.lineResolution, TimeWrapFaceActivity.this.resolutionY);
                }
                TimeWrapFaceActivity timeWrapFaceActivity5 = TimeWrapFaceActivity.this;
                timeWrapFaceActivity5.resultBitmap = timeWrapFaceActivity5.overlay(timeWrapFaceActivity5.resultBitmap, TimeWrapFaceActivity.this.subBitmap, TimeWrapFaceActivity.this.lineCount, TimeWrapFaceActivity.this.warpDirection);
                if (TimeWrapFaceActivity.this.captureMode == CAPTURE_MODE.VIDEO) {
                    TimeWrapFaceActivity timeWrapFaceActivity6 = TimeWrapFaceActivity.this;
                    TimeWrapFaceActivity.this.resultBitmapList.add(timeWrapFaceActivity6.overlay(bitmap, timeWrapFaceActivity6.resultBitmap));
                }
                TimeWrapFaceActivity.this.imageView.setImageBitmap(TimeWrapFaceActivity.this.resultBitmap);
                TimeWrapFaceActivity timeWrapFaceActivity7 = TimeWrapFaceActivity.this;
                timeWrapFaceActivity7.drawScanEffect(bitmap, timeWrapFaceActivity7.warpDirection, TimeWrapFaceActivity.this.lineCount);
                TimeWrapFaceActivity.this.lineCount += TimeWrapFaceActivity.this.lineResolution;
                long currentTimeMillis2 = currentTimeMillis - System.currentTimeMillis();
                if (currentTimeMillis2 < TimeWrapFaceActivity.this.frameRate) {
                    try {
                        Thread.sleep(TimeWrapFaceActivity.this.frameRate - currentTimeMillis2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            TimeWrapFaceActivity.this.previewViewImageView.setImageBitmap(bitmap);
            imageProxy.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            resumeToBeforeCaptureUI();
        }
    }

    public void stopCapture() {
//        if ((this.lineCount == this.resolutionY && this.warpDirection == WARP_DIRECTION.DOWN) || (this.lineCount == this.resolutionX && this.warpDirection == WARP_DIRECTION.RIGHT)) {
//            showResultUI();
//            hideCaptureUI();
//        } else {
//            hideCaptureUI();
//        }
        this.capture = false;
    }

    public void playGIF() {
        this.GIFTimer = new Timer();
        final Handler handler = new Handler() { // from class: time.warp.camera.photo.filter.Activity.TimeWrapFaceActivity.13
            int i = 0;

            @Override // android.os.Handler
            public void handleMessage(Message message) {
                if (TimeWrapFaceActivity.this.resultBitmapList != null) {
                    if (this.i == TimeWrapFaceActivity.this.resultBitmapList.size() - 1) {
                        this.i = 0;
                    }
                    if (TimeWrapFaceActivity.this.resultBitmapList != null) {
                        TimeWrapFaceActivity.this.imageView.setImageBitmap(TimeWrapFaceActivity.this.resultBitmapList.get(this.i));
                        this.i++;
                    }
                }
            }
        };
        this.GIFTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.obtainMessage(1).sendToTarget();
            }
        }, 0L, this.frameRate * 20);
    }

    @Override
    public void onBackPressed() {
        if (this.captureUI.getVisibility() == View.VISIBLE) {
            resumeToBeforeCaptureUI();
        } else if (this.resultUI.getVisibility() == View.VISIBLE) {
            resumeToBeforeCaptureUI();
        } else {
            APIManager.showInter(TimeWrapFaceActivity.this, true, isfail -> {
                finish();
            });
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 16908332:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }
}