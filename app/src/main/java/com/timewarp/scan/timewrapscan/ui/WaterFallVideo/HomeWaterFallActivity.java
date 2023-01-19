package com.timewarp.scan.timewrapscan.ui.WaterFallVideo;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.camera.camera2.Camera2Config;
import androidx.camera.camera2.interop.Camera2Interop;
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

import com.example.timewarpscanfacefilter.ui.activity.PreviewActivity;
import com.timewarp.scan.timewrapscan.R;
import com.timewarp.scan.timewrapscan.utils.BitmapToVideoEncoder;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import think.outside.the.box.handler.APIManager;
import think.outside.the.box.ui.BaseActivity;

public class HomeWaterFallActivity extends BaseActivity implements CameraXConfig.Provider {

    private static final String APP_OPENED_PREF = "WATERFALL_APP_OPENED_PREF";
    private static final int FREE_SCANS = 3;
    private static final String GOOGLE_AD_COUNTER_PREF = "WATERFALL_GOOGLE_AD_COUNTER_PREF_1";
    private static final String MEDIA_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "Time Wrap/Waterfall";
    private static final String RATE_DIALOG_PREF = "WATERFALL_RATE_DIALOG_PREF";
    private static final String SYSTEM_TIME_PREF = "WATERFALL_SYSTEM_TIME_PREF";
    private static final String TIME_WARP_WATERFALL_PREFS = "TIME_WARP_WATERFALL_PREFS";
    public static BitmapToVideoEncoder bitmapToVideoEncoder;
    ProcessCameraProvider cameraProvider;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    CameraSelector cameraSelector;
    ImageAnalysis imageAnalysis;
    Camera mCamera;
    Preview preview;
    public PreviewView previewView;
    private final String[] REQUIRED_PERMISSIONS = {"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    ConstraintLayout beforeCaptureUI = null;
    boolean capture = false;
    ConstraintLayout captureUI = null;
    int facing = 0;
    Uri fileURI = null;
    ImageView imageView = null;
    boolean isSwitching = false;
    int lineCount = 0;
    int lineResolution = 5;
    ImageView previewViewImageView = null;
    int resolutionX = 480;
    int resolutionY = 640;
    Bitmap resultBitmap = null;
    SharedPreferences sharedPref = null;
    Bitmap subBitmap = null;
    ConstraintLayout tutorialUI = null;
    List<Bitmap> waterfallBitmapList = null;
    ImageView waterfallView = null;
    ImageView toogleTorch_ImageView = null;
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private Integer frameRate = 25;

    public static Bitmap MirrorBitmap(Bitmap bitmap, int i, int i2) {
        Matrix matrix = new Matrix();
        matrix.preScale(i, i2);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static void moveFile(File file, File file2) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(file2);
        byte[] bArr = new byte[1024];
        while (true) {
            int read = fileInputStream.read(bArr);
            if (read <= 0) {
                break;
            }
            fileOutputStream.write(bArr, 0, read);
        }
        fileInputStream.close();
        fileOutputStream.close();
        if (!file.exists()) {
            return;
        }
        if (file.delete()) {
            PrintStream printStream = System.out;
            printStream.println("file Deleted :" + file.getPath());
            return;
        }
        PrintStream printStream2 = System.out;
        printStream2.println("file not Deleted :" + file.getPath());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT > 29) {
            this.resolutionX = 720;
            this.resolutionY = 1280;
            this.lineResolution = 7;
        }
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_home_water_fall);

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(TIME_WARP_WATERFALL_PREFS, 0);
        this.sharedPref = sharedPreferences;
        sharedPreferences.edit().putInt(APP_OPENED_PREF, this.sharedPref.getInt(APP_OPENED_PREF, 0) + 1).apply();
        File file = new File(MEDIA_FOLDER);
        if (!file.exists()) {
            file.mkdirs();
            sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
        }
        PreviewView previewView = (PreviewView) findViewById(R.id.previewView);
        this.previewView = previewView;
        previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
        this.toogleTorch_ImageView = (ImageView) findViewById(R.id.toogleTorch_ImageView);
        this.previewViewImageView = (ImageView) findViewById(R.id.previewView_ImageView);
//        this.videoView = (VideoView) findViewById(R.id.videoView);
        this.waterfallView = (ImageView) findViewById(R.id.waterfall_View);
        this.waterfallBitmapList = new ArrayList();
        this.cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        ImageAnalysis build = new ImageAnalysis.Builder().setTargetResolution(new Size(this.resolutionX, this.resolutionY)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        this.imageAnalysis = build;
        build.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageCapture());
        this.cameraProviderFuture.addListener(new Runnable() {
            @Override // java.lang.Runnable
            public final void run() {
                try {
                    HomeWaterFallActivity homeWaterFallActivity = HomeWaterFallActivity.this;
                    homeWaterFallActivity.cameraProvider = (ProcessCameraProvider) homeWaterFallActivity.cameraProviderFuture.get();
                    if (HomeWaterFallActivity.this.allPermissionsGranted()) {
                        HomeWaterFallActivity homeWaterFallActivity2 = HomeWaterFallActivity.this;
                        homeWaterFallActivity2.bindPreview(homeWaterFallActivity2.cameraProvider);
                    } else {
                        HomeWaterFallActivity homeWaterFallActivity3 = HomeWaterFallActivity.this;
                        ActivityCompat.requestPermissions(homeWaterFallActivity3, homeWaterFallActivity3.REQUIRED_PERMISSIONS, HomeWaterFallActivity.this.REQUEST_CODE_PERMISSIONS);
                    }
                } catch (InterruptedException | ExecutionException unused) {
                }
            }
        }, ContextCompat.getMainExecutor(this));
        this.beforeCaptureUI = (ConstraintLayout) findViewById(R.id.before_capture_UI);
        this.captureUI = (ConstraintLayout) findViewById(R.id.capture_UI);
        this.imageView = (ImageView) findViewById(R.id.result_imageView);
        ((ImageView) findViewById(R.id.capture_video_Button)).setOnClickListener(new View.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
//                HomeWaterFallActivity.this.videoView.setVisibility(0);
                try {
                    HomeWaterFallActivity.this.startCapture();
                    HomeWaterFallActivity.this.hideBeforeCaptureUI();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        ((ImageView) findViewById(R.id.stop_capture_video_Button)).setOnClickListener(new View.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
//                HomeWaterFallActivity.this.videoView.setVisibility(0);
                HomeWaterFallActivity.this.stopCapture();
            }
        });
//        ((Button) findViewById(R.id.resultCancel_Button)).setOnClickListener(new View.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.5
//            @Override // android.view.View.OnClickListener
//            public final void onClick(View view) {
//                HomeWaterFallActivity.this.resumeToBeforeCaptureUI();
//            }
//        });
        ((ImageView) findViewById(R.id.switchCamera_ImageView)).setOnClickListener(new View.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.6
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                HomeWaterFallActivity.this.isSwitching = true;
                if (HomeWaterFallActivity.this.facing == 0) {
                    HomeWaterFallActivity.this.setFacing(1);
                } else {
                    HomeWaterFallActivity.this.setFacing(0);
                }
                HomeWaterFallActivity homeWaterFallActivity = HomeWaterFallActivity.this;
                homeWaterFallActivity.bindPreview(homeWaterFallActivity.cameraProvider);
                HomeWaterFallActivity.this.isSwitching = false;
            }
        });
    }

    public boolean allPermissionsGranted() {
        for (String str : this.REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, str) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i != this.REQUEST_CODE_PERMISSIONS) {
            return;
        }
        if (allPermissionsGranted()) {
            bindPreview(this.cameraProvider);
            return;
        }
        Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void setFacing(int i) {
        this.facing = i;
    }

    public Bitmap overlay(Bitmap bitmap, Bitmap bitmap2) {
        new Matrix().preScale(1.0f, -1.0f);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(createBitmap);
        canvas.drawBitmap(bitmap, new Matrix(), null);
        canvas.drawBitmap(bitmap2, 0.0f, 0.0f, (Paint) null);
        return createBitmap;
    }

    public Bitmap rotateBitmap(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.setRotate(i);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    public void drawWaterMark(Bitmap bitmap, Context context) {
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(-1);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paint.setTextSize(15.0f);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("On Google Play Store:", paint.getTextSize(), paint.getTextSize() * 2.0f, paint);
        canvas.drawText(String.valueOf(context.getResources().getText(R.string.app_name)), paint.getTextSize(), (paint.getTextSize() * 3.0f) + 0.0f, paint);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, (Paint) null);
    }

    public void addWaterfallBitmap(Bitmap bitmap) {
        this.waterfallBitmapList.add(0, bitmap);
    }

    public void updateWaterfallBitmap(Bitmap bitmap) {
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bitmap);
        for (int size = this.waterfallBitmapList.size() - 1; size > 0; size--) {
            canvas.drawBitmap(this.waterfallBitmapList.get(size), 0.0f, (bitmap.getHeight() / 2) + (this.lineResolution * size), paint);
        }
        this.waterfallView.setImageBitmap(bitmap);
    }

    public void initializeImageView() {
        Bitmap createBitmap = Bitmap.createBitmap(this.resolutionX, this.resolutionY, Bitmap.Config.ARGB_8888);
        this.resultBitmap = createBitmap;
        createBitmap.eraseColor(0);
        this.imageView.setImageBitmap(this.resultBitmap);
    }

    private void showBeforeCaptureUI() {
        this.beforeCaptureUI.setVisibility(View.VISIBLE);
    }

    public void hideBeforeCaptureUI() {
        this.beforeCaptureUI.setVisibility(View.INVISIBLE);
    }

    private void showCaptureUI() {
        this.captureUI.setVisibility(View.VISIBLE);
    }

    private void hideCaptureUI() {
        this.captureUI.setVisibility(View.INVISIBLE);
    }

    private void showResultUI() {
        APIManager.showInter(HomeWaterFallActivity.this, false, isfail -> {
            Intent intent = new Intent(this, PreviewActivity.class);
            intent.putExtra("isfrom_waterfall", true);
            startActivityForResult(intent, 12);
            finish();
        });

//        HomeWaterFallActivity.bitmapToVideoEncoder.getOutputFile()
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 12) {
            resumeToBeforeCaptureUI();
        }
    }

    private boolean checkAppInstall(String str) {
        try {
            getPackageManager().getPackageInfo(str, 0);
            return true;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    private int getRandomColor() {
        Random random = new Random();
        return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    public void resumeToBeforeCaptureUI() {
        this.waterfallBitmapList = new ArrayList();
        this.waterfallView.setVisibility(View.INVISIBLE);
        initializeImageView();
        hideCaptureUI();
        showBeforeCaptureUI();
        this.capture = false;
    }

    public void startCapture() throws InterruptedException {
        File file;
        bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.7
            @Override
            // com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.BitmapToVideoEncoder.IBitmapToVideoEncoderCallback
            public void onEncodingComplete(File file2) {
                HomeWaterFallActivity.this.runOnUiThread(new Runnable() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.7.1
                    @Override // java.lang.Runnable
                    public void run() {
                        HomeWaterFallActivity.this.playVideo();
                    }
                });
            }
        });
        try {
            file = File.createTempFile("tempFile", ".mp4", getApplicationContext().getCacheDir());
            file.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
            bitmapToVideoEncoder.startEncoding(this.resolutionX, this.resolutionY, null);
            this.lineCount = 0;
            this.resultBitmap = null;
            initializeImageView();
            showCaptureUI();
            this.capture = true;
            file = null;
        }
        bitmapToVideoEncoder.startEncoding(this.resolutionX, this.resolutionY, file);
        this.lineCount = 0;
        this.resultBitmap = null;
        initializeImageView();
        showCaptureUI();
        this.capture = true;
    }

//    private void showRateAlertDialog() {
//        final String packageName = getPackageName();
//        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialog);
//        builder.setMessage("Hello you awsome person! Please help us with a good rating in the Google Play Store! :)");
//        builder.setCancelable(true);
//        builder.setNegativeButton("Yeah for sure!", new DialogInterface.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.8
//            @Override // android.content.DialogInterface.OnClickListener
//            public final void onClick(DialogInterface dialogInterface, int i) {
//                try {
//                    HomeWaterFallActivity homeWaterFallActivity = HomeWaterFallActivity.this;
//                    homeWaterFallActivity.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + packageName)));
//                } catch (ActivityNotFoundException unused) {
//                    HomeWaterFallActivity homeWaterFallActivity2 = HomeWaterFallActivity.this;
//                    homeWaterFallActivity2.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
//                }
//            }
//        });
//        builder.setPositiveButton("No thanks!", new DialogInterface.OnClickListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.9
//            @Override // android.content.DialogInterface.OnClickListener
//            public void onClick(DialogInterface dialogInterface, int i) {
//                dialogInterface.cancel();
//            }
//        });
//        AlertDialog create = builder.create();
//        create.getWindow().setBackgroundDrawable(getDrawable(R.drawable.layout_bg));
//        create.show();
//    }

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
    @SuppressLint("UnsafeOptInUsageError")
    public void bindPreview(ProcessCameraProvider processCameraProvider) {
        processCameraProvider.unbindAll();
        this.preview = new Preview.Builder().setTargetResolution(new Size(this.resolutionX, this.resolutionY)).build();
        this.cameraSelector = new CameraSelector.Builder().requireLensFacing(this.facing).build();
        ImageAnalysis.Builder builder = new ImageAnalysis.Builder();
       Camera2Interop.Extender extender = new Camera2Interop.Extender(builder);
        CaptureRequest.Key key = CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE;
        Integer num = this.frameRate;
        extender.setCaptureRequestOption(key, new Range(num, num));
        ImageAnalysis build = builder.setTargetResolution(new Size(this.resolutionX, this.resolutionY)).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        this.imageAnalysis = build;
        build.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageCapture());
        this.preview.setSurfaceProvider(this.previewView.getSurfaceProvider());
        this.mCamera = processCameraProvider.bindToLifecycle(this, this.cameraSelector, this.preview);
        processCameraProvider.bindToLifecycle(this, this.cameraSelector, this.imageAnalysis, this.preview);
        this.toogleTorch_ImageView.setBackgroundResource(R.drawable.ic_flash_off);
        if (this.mCamera.getCameraInfo().hasFlashUnit()) {
            this.toogleTorch_ImageView.setVisibility(View.VISIBLE);
        } else {
            this.toogleTorch_ImageView.setVisibility(View.INVISIBLE);
        }
    }

    public void toggleTorch(View view) {
        if (!this.mCamera.getCameraInfo().hasFlashUnit()) {
            return;
        }
        if (this.mCamera.getCameraInfo().getTorchState().getValue().intValue() == 1) {
            this.mCamera.getCameraControl().enableTorch(false);
            this.toogleTorch_ImageView.setBackgroundResource(R.drawable.ic_flash_off);
            return;
        }
        this.mCamera.getCameraControl().enableTorch(true);
        this.toogleTorch_ImageView.setBackgroundResource(R.drawable.ic_flash_on);
    }

    public void playVideo() {
//        this.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() { // from class: com.timewarpscan.bluelinefiltertiktok.photoeditor.wrapphoto.WaterFallVideo.HomeWaterFallActivity.10
//            @Override // android.media.MediaPlayer.OnPreparedListener
//            public void onPrepared(MediaPlayer mediaPlayer) {
//                mediaPlayer.setLooping(true);
////                float videoWidth = (mediaPlayer.getVideoWidth() / mediaPlayer.getVideoHeight()) / (HomeWaterFallActivity.this.videoView.getWidth() / HomeWaterFallActivity.this.videoView.getHeight());
////                if (videoWidth >= 1.0f) {
////                    HomeWaterFallActivity.this.videoView.setScaleX(videoWidth);
////                } else {
////                    HomeWaterFallActivity.this.videoView.setScaleY(1.0f / videoWidth);
////                }
//            }
//        });
//        this.videoView.setVideoPath(bitmapToVideoEncoder.getOutputFile().getPath());
//        this.videoView.start();
    }


    public Uri saveVideoMediaStore(File file) {
        try {
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put("title", file.getName());
            contentValues.put("_display_name", System.currentTimeMillis() + ".mp4");
            contentValues.put("mime_type", "video/mp4");
            contentValues.put("relative_path", Environment.DIRECTORY_DCIM + File.separator + "Camera");
            Uri insert = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                OutputStream openOutputStream = getContentResolver().openOutputStream(insert);
                byte[] bArr = new byte[4096];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read == -1) {
                        break;
                    }
                    openOutputStream.write(bArr, 0, read);
                }
                openOutputStream.flush();
                fileInputStream.close();
                openOutputStream.close();
            } catch (Exception e) {
                Log.e("TAG", "exception while writing video: ", e);
            }
            return insert;
        } catch (Exception unused) {
            return null;
        }
    }

    private String getRealPathFromURI(Context context, Uri uri) {
        try {
            Cursor query = context.getContentResolver().query(uri, new String[]{"_data"}, null, null, null);
            int columnIndexOrThrow = query.getColumnIndexOrThrow("_data");
            query.moveToFirst();
            String string = query.getString(columnIndexOrThrow);
            if (query != null) {
                query.close();
            }
            return string;
        } catch (Exception e) {
            Log.e("TAG", "getRealPathFromURI Exception : " + e.toString());
            return "";
        }
    }

    private boolean showAd() {
        return this.sharedPref.getInt(GOOGLE_AD_COUNTER_PREF, 0) == 3 && System.currentTimeMillis() - this.sharedPref.getLong(SYSTEM_TIME_PREF, -1L) > 600000;
    }

    private void incrementCounter() {
        int i = this.sharedPref.getInt(GOOGLE_AD_COUNTER_PREF, 0);
        SharedPreferences.Editor edit = this.sharedPref.edit();
        if (i < 3) {
            edit.putInt(GOOGLE_AD_COUNTER_PREF, i + 1);
            edit.apply();
        }
    }

    private void resetCounter() {
        SharedPreferences.Editor edit = this.sharedPref.edit();
        edit.putInt(GOOGLE_AD_COUNTER_PREF, 0);
        edit.apply();
    }

    public void stopCapture() {
        bitmapToVideoEncoder.stopEncoding();
        showResultUI();
        hideCaptureUI();
        this.capture = false;
        this.waterfallBitmapList = new ArrayList();
    }

    @Override // androidx.activity.ComponentActivity, android.app.Activity
    public void onBackPressed() {
        finish();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onResume() {
//        this.videoView.start();
        ImageView imageView = this.toogleTorch_ImageView;
        if (imageView.getVisibility() == View.VISIBLE && this.mCamera.getCameraInfo().hasFlashUnit() && imageView.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.ic_flash_on).getConstantState())) {
            this.mCamera.getCameraControl().enableTorch(true);
        }
        super.onResume();
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onPause() {
        super.onPause();
    }

    @Override
    // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
    }

    /* loaded from: classes2.dex */
    public class ImageCapture implements ImageAnalysis.Analyzer {


        //        @Override // androidx.camera.core.ImageAnalysis.Analyzer
//        public /* synthetic */ int getTargetCoordinateSystem() {
//            return ImageAnalysis.Analyzer.CC.$default$getTargetCoordinateSystem(this);
//        }
//
//        @Override // androidx.camera.core.ImageAnalysis.Analyzer
//        public /* synthetic */ Size getTargetResolutionOverride() {
//            return ImageAnalysis.Analyzer.CC.$default$getTargetResolutionOverride(this);
//        }
//
//        @Override // androidx.camera.core.ImageAnalysis.Analyzer
//        public /* synthetic */ void updateTransform(Matrix matrix) {
//            ImageAnalysis.Analyzer.CC.$default$updateTransform(this, matrix);
//        }

        private ImageCapture() {
        }

        @SuppressLint("UnsafeOptInUsageError")
        @Override // androidx.camera.core.ImageAnalysis.Analyzer
        public void analyze(ImageProxy imageProxy) {
            Bitmap bitmap;
            if (HomeWaterFallActivity.this.previewView.getPreviewStreamState().getValue() == PreviewView.StreamState.STREAMING && HomeWaterFallActivity.this.previewView.getChildAt(0).getClass() == TextureView.class) {
                bitmap = ((TextureView) HomeWaterFallActivity.this.previewView.getChildAt(0)).getBitmap(HomeWaterFallActivity.this.resolutionX, HomeWaterFallActivity.this.resolutionY);
            } else if (imageProxy.getFormat() == 35) {
                HomeWaterFallActivity homeWaterFallActivity = HomeWaterFallActivity.this;
                bitmap = homeWaterFallActivity.rotateBitmap(homeWaterFallActivity.toBitmap(imageProxy.getImage()), 90);
                if (HomeWaterFallActivity.this.facing == 0) {
                    bitmap = HomeWaterFallActivity.MirrorBitmap(bitmap, 1, -1);
                }
            } else {
                bitmap = null;
            }
            if (bitmap == null) {
                imageProxy.close();
                return;
            }
            if (HomeWaterFallActivity.this.capture) {
                if (HomeWaterFallActivity.this.resultBitmap == null) {
                    HomeWaterFallActivity.this.initializeImageView();
                }
                HomeWaterFallActivity.this.subBitmap = Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2, HomeWaterFallActivity.this.resolutionX, HomeWaterFallActivity.this.lineResolution);
                HomeWaterFallActivity homeWaterFallActivity2 = HomeWaterFallActivity.this;
                homeWaterFallActivity2.addWaterfallBitmap(homeWaterFallActivity2.subBitmap);
                HomeWaterFallActivity homeWaterFallActivity3 = HomeWaterFallActivity.this;
                homeWaterFallActivity3.updateWaterfallBitmap(homeWaterFallActivity3.resultBitmap);
                HomeWaterFallActivity homeWaterFallActivity4 = HomeWaterFallActivity.this;
                HomeWaterFallActivity.bitmapToVideoEncoder.queueFrame(homeWaterFallActivity4.overlay(bitmap, homeWaterFallActivity4.resultBitmap));
            }
            HomeWaterFallActivity.this.previewViewImageView.setImageBitmap(bitmap);
            imageProxy.close();
        }
    }

}