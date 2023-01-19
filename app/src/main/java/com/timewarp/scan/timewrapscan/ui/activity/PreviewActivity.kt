package com.example.timewarpscanfacefilter.ui.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.ColorDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.arthenica.mobileffmpeg.FFmpeg
import com.timewarp.scan.timewrapscan.R
import com.timewarp.scan.timewrapscan.databinding.ActivityPreviewBinding
import com.timewarp.scan.timewrapscan.enums.CAPTURE_MODE
import com.timewarp.scan.timewrapscan.ui.WaterFallVideo.HomeWaterFallActivity
import com.timewarp.scan.timewrapscan.ui.activity.ShareScreenPhotoActivity
import com.timewarp.scan.timewrapscan.ui.activity.ShareScreenVideoActivity
import com.timewarp.scan.timewrapscan.ui.activity.TimeWrapFaceActivity
import com.timewarp.scan.timewrapscan.ui.activity.TimeWrapFaceActivity.resultBitmapList
import com.timewarp.scan.timewrapscan.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import think.outside.the.box.callback.AdsCallback
import think.outside.the.box.handler.APIManager
import think.outside.the.box.handler.APIManager.showInter
import think.outside.the.box.ui.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class PreviewActivity : BaseActivity() {

    private var binding: ActivityPreviewBinding? = null
    var GIFTimer: Timer? = null
    var frameRate = 2
    var i = 0
    var dialogLoading: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLightTheme(true)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_preview)

        APIManager.showBanner(binding!!.adsBanner);

        val isWaterfall = intent.getBooleanExtra("isfrom_waterfall", false);
        val s = intent.getStringExtra("capturemode")
        if (isWaterfall) {
            binding!!.ivPreview2.isVisible = true

            binding!!.ivPreview2.setVideoPath(HomeWaterFallActivity.bitmapToVideoEncoder.outputFile.path.toString())
            binding!!.ivPreview2.setOnCompletionListener {
                binding!!.ivPlay.isVisible = true
            }
            binding!!.ivPreview2.setOnClickListener {
                binding!!.ivPreview2.pause()
                binding!!.ivPlay.isVisible = true
            }

            binding!!.ivPreview2.setOnPreparedListener { mediaPlayer ->
                val videoRatio: Float = (mediaPlayer.getVideoWidth() / mediaPlayer.getVideoHeight()
                    .toFloat()).toFloat() as Float
                val screenRatio: Float =
                    (binding!!.ivPreview2.getWidth() / binding!!.ivPreview2.getHeight()
                        .toFloat()).toFloat() as Float
                val scaleX = videoRatio / screenRatio
                if (scaleX >= 1f) {
                    binding!!.ivPreview2.setScaleX(scaleX)
                } else {
                    binding!!.ivPreview2.setScaleY(1f / scaleX)
                }
            }
        } else if (s.equals("PHOTO")) {
            binding!!.ivPlay.isVisible = false
            binding!!.ivPreview.setImageBitmap(
                TimeWrapFaceActivity.resultBitmap
            )
        } else {
            binding!!.ivPreview.setImageBitmap(
                TimeWrapFaceActivity.resultBitmapList.get(
                    0
                )
            )
            binding!!.ivPreview.setOnClickListener {
                if (GIFTimer != null) {
                    cancelGIF()
                    binding!!.ivPlay.isVisible = true
                }
            }
        }
//        binding.ivPreview

        binding!!.ivPlay.setOnClickListener {
            binding!!.ivPlay.isVisible = false
            if (isWaterfall) {
                binding!!.ivPreview2.start()
            } else {
                playGIF()
            }
        }

        binding!!.buttonBack.setOnClickListener { onBackPressed() }

        binding!!.btnsaveMore.setOnClickListener {
            if (isWaterfall) {
                initProgress()
                GlobalScope.launch {
                    val file = File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        "TimeWarpFaceFilter/Video"
                    )
                    file.mkdirs()
                    val newfile = File(file, "water_fall_" + System.currentTimeMillis() + ".mp4")
                    HomeWaterFallActivity.moveFile(
                        HomeWaterFallActivity.bitmapToVideoEncoder.outputFile,
                        newfile
                    )
                    launch(Dispatchers.Main) {
                        if (dialogLoading!!.isShowing) {
                            dialogLoading!!.dismiss()
                        }
                        showInter(this@PreviewActivity, false, object : AdsCallback {
                            override fun onClose(isfail: Boolean) {
                                val intent = Intent(
                                    this@PreviewActivity,
                                    ShareScreenVideoActivity::class.java
                                )
                                intent.putExtra("shareVideoPath", newfile.absolutePath)
                                startActivity(intent)
                                finish()
                            }
                        })
                    }
                }
            } else {
                save()
            }
        }
    }

    private fun save() {
        StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().build())
        if (intent.getStringExtra("capturemode") == CAPTURE_MODE.PHOTO.name) {
            initProgress()
            Executors.newSingleThreadExecutor()
                .execute {
                    try {
                        var fileURI = saveBitmapInGalary(TimeWrapFaceActivity.resultBitmap)

                        if (dialogLoading!!.isShowing) {
                            dialogLoading!!.dismiss()
                        }
                        runOnUiThread {
                            showInter(this@PreviewActivity, false, object : AdsCallback {
                                override fun onClose(isfail: Boolean) {
                                    val intent = Intent(
                                        this@PreviewActivity,
                                        ShareScreenPhotoActivity::class.java
                                    )
                                    intent.putExtra("sharePath", fileURI?.getPath())
                                    startActivity(intent)
                                    finish()
                                }
                            })
                            MediaScannerConnection.scanFile(
                                applicationContext,
                                arrayOf(fileURI?.getPath()),
                                arrayOf("image/jpeg"),
                                null
                            )
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        } else if (intent.getStringExtra("capturemode") == CAPTURE_MODE.VIDEO.name) {
            initProgress()
            Executors.newSingleThreadExecutor()
                .execute { m1661xea15cb8() }
        }
    }

    private fun m1661xea15cb8() {
        try {
            getImagesFromBitmaps(resultBitmapList)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun getImagesFromBitmaps(list: List<Bitmap>): File? {
        Utils.isFolderCreated()
        val file = applicationContext.getExternalFilesDir("").toString()
        val file2 = File("$file/TimeWarpFaceFilter/temp")
        if (file2.exists()) {
            file2.delete()
        } else {
            file2.mkdirs()
        }
        Executors.newSingleThreadExecutor().execute {
            m1658xa9d555e0(
                list,
                file2
            )
        }
        return File(file)
    }

    fun m1658xa9d555e0(list: List<*>, file: File) {
        var str: String
        var i = 0
        while (i < list.size) {
            val i2 = i + 1
            str = if (i2 < 10) {
                "00$i2"
            } else if (i < 99) {
                "0$i2"
            } else {
                i2.toString()
            }
            val str2 = "$file/image$str.jpg"
            try {
                val fileOutputStream = FileOutputStream(File(str2))
                (list[i] as Bitmap).compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
                fileOutputStream.flush()
                fileOutputStream.close()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            Log.e("HHHH", "" + str2)
            i = i2
        }
        runOnUiThread {
            saveImageAsVideo(file.absolutePath + "/image%03d.jpg")
        }
    }

    private fun initProgress() {
        val dialog = android.app.Dialog(this)
        this.dialogLoading = dialog
        dialog.setContentView(R.layout.loading_dialog)
        dialogLoading?.window?.setBackgroundDrawable(ColorDrawable(0))
        dialogLoading?.setCancelable(false)
        dialogLoading?.show()
    }

    private fun saveImageAsVideo(str: String) {
        try {
            val file = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                "TimeWarpFaceFilter/Video"
            )
            file.mkdirs()
            val format = SimpleDateFormat("" + System.currentTimeMillis(), Locale.US).format(Date())
            val file2 = File(file, "WarpVideo_$format.mp4")
            FFmpeg.executeAsync(
                arrayOf("-i", str, file2.absolutePath)
            ) { j, i ->

                if (i == 0) {
                    val file3: File = File(
                        this@PreviewActivity.getApplicationContext().getExternalFilesDir("")
                            .toString() + "/TimeWarpFaceFilter/temp"
                    )
                    if (file3.exists()) {
                        file3.delete()
                    }
                    if (dialogLoading != null && dialogLoading?.isShowing() == true) {
                        this@PreviewActivity.dialogLoading?.dismiss()
                    }
                    var fileURI = Uri.fromFile(file2)
                    if (fileURI != null) {
                        MediaScannerConnection.scanFile(
                            this@PreviewActivity.getApplicationContext(),
                            arrayOf(fileURI.getPath()),
                            arrayOf("image/jpeg"),
                            null
                        )
                    }
                    showInter(this@PreviewActivity, false, object : AdsCallback {
                        override fun onClose(isfail: Boolean) {
                            val intent = Intent(
                                this@PreviewActivity,
                                ShareScreenVideoActivity::class.java
                            )
                            intent.putExtra("shareVideoPath", file2.absolutePath)
                            this@PreviewActivity.startActivity(intent)
                            this@PreviewActivity.finish()
                        }
                    })
                    Log.e("HHHH", "Async command execution completed successfully.")
                } else if (i == 255) {
                    Log.e("HHHH", "Async command execution cancelled by user.")
                } else {
                    if (this@PreviewActivity.dialogLoading != null && this@PreviewActivity.dialogLoading?.isShowing() == true) {
                        this@PreviewActivity.dialogLoading?.dismiss()
                    }
                    Log.e(
                        "HHHH",
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            Integer.valueOf(i)
                        )
                    )
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun playGIF() {
        this.GIFTimer = Timer()
        val handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(message: Message) {
                if (TimeWrapFaceActivity.resultBitmapList != null) {
                    if (i == TimeWrapFaceActivity.resultBitmapList.size - 1) {
                        i = 0
                    }
                    try {
                        if (TimeWrapFaceActivity.resultBitmapList != null) {
                            binding!!.ivPreview.setImageBitmap(TimeWrapFaceActivity.resultBitmapList.get(i))
                            i++
                        }
                    } catch (e: Exception) {
                    }
                }
            }
        }
        GIFTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.obtainMessage(1).sendToTarget()
            }
        }, 0L, (this.frameRate * 20).toLong())
    }

    fun cancelGIF() {
        GIFTimer?.cancel()
        GIFTimer = null
    }

    private fun saveBitmapInGalary(bitmap: Bitmap): Uri? {
        Utils.isFolderCreated()
        val file =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
        val file2 = File("$file/TimeWarpFaceFilter/Photo")
        if (!file2.exists()) {
            file2.mkdirs()
        }
        val format = SimpleDateFormat("" + System.currentTimeMillis(), Locale.US).format(
            Date()
        )
        val file3 = File(file2, "WarpPhoto_$format.jpg")
        if (file3.exists()) {
            file3.delete()
        }
        try {
            val fileOutputStream = FileOutputStream(file3)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return Uri.fromFile(file3)
    }

    override fun onBackPressed() {
        showInter(this@PreviewActivity, true, object : AdsCallback {
            override fun onClose(isfail: Boolean) {
                finish()
            }
        })
    }
}