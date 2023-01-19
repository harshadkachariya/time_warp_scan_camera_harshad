package com.timewarp.scan.timewrapscan.utils;

import android.graphics.Bitmap;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import androidx.core.view.MotionEventCompat;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class BitmapToVideoEncoder {
    private static final int BIT_RATE = 16000000;
    private static final int FRAME_RATE = 25;
    private static final int I_FRAME_INTERVAL = 1;
    private static final String MIME_TYPE = "video/avc";
    private static final String TAG = "BitmapToVideoEncoder";
    private static int mHeight;
    private static int mWidth;
    private IBitmapToVideoEncoderCallback mCallback;
    private CountDownLatch mNewFrameLatch;
    private File mOutputFile;
    private int mTrackIndex;
    private MediaCodec mediaCodec;
    private MediaMuxer mediaMuxer;
    private boolean mAbort = false;
    private Queue<Bitmap> mEncodeQueue = new ConcurrentLinkedQueue();
    private Object mFrameSync = new Object();
    private int mGenerateIndex = 0;
    private boolean mNoMoreFrames = false;

    public interface IBitmapToVideoEncoderCallback {
        void onEncodingComplete(File file);
    }

    private static boolean isRecognizedFormat(int i) {
        if (i == 39 || i == 2130706688) {
            return true;
        }
        switch (i) {
            case 19:
            case 20:
            case 21:
                return true;
            default:
                return false;
        }
    }

    public BitmapToVideoEncoder(IBitmapToVideoEncoderCallback iBitmapToVideoEncoderCallback) {
        this.mCallback = iBitmapToVideoEncoderCallback;
    }

    private static MediaCodecInfo selectCodec(String str) {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                for (String str2 : codecInfoAt.getSupportedTypes()) {
                    if (str2.equalsIgnoreCase(str)) {
                        return codecInfoAt;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static int selectColorFormat(MediaCodecInfo mediaCodecInfo, String str) {
        int[] iArr;
        for (int i : mediaCodecInfo.getCapabilitiesForType(str).colorFormats) {
            if (isRecognizedFormat(i)) {
                return i;
            }
        }
        return 0;
    }

    public boolean isEncodingStarted() {
        return this.mediaCodec != null && this.mediaMuxer != null && !this.mNoMoreFrames && !this.mAbort;
    }

    public int getActiveBitmaps() {
        return this.mEncodeQueue.size();
    }

    public void startEncoding(int i, int i2, File file) {
        mWidth = i;
        mHeight = i2;
        this.mOutputFile = file;
        try {
            String canonicalPath = file.getCanonicalPath();
            MediaCodecInfo selectCodec = selectCodec(MIME_TYPE);
            if (selectCodec == null) {
                Log.e(TAG, "Unable to find an appropriate codec for video/avc");
                return;
            }
            Log.d(TAG, "found codec: " + selectCodec.getName());
            try {
                this.mediaCodec = MediaCodec.createByCodecName(selectCodec.getName());
                MediaFormat createVideoFormat = MediaFormat.createVideoFormat(MIME_TYPE, mWidth, mHeight);
                createVideoFormat.setInteger("bitrate", BIT_RATE);
                createVideoFormat.setInteger("frame-rate", 25);
                createVideoFormat.setInteger("color-format", 21);
                createVideoFormat.setInteger("i-frame-interval", 1);
                this.mediaCodec.configure(createVideoFormat, (Surface) null, (MediaCrypto) null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                this.mediaCodec.start();
                try {
                    this.mediaMuxer = new MediaMuxer(canonicalPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    Log.d(TAG, "Initialization complete. Starting encoder...");
                    Completable.fromAction(() -> BitmapToVideoEncoder.this.lambda$startEncoding$0$BitmapToVideoEncoder()).subscribeOn(Schedulers.io()).observeOn(Schedulers.newThread()).subscribe();
                } catch (IOException e) {
                    Log.e(TAG, "MediaMuxer creation failed. " + e.getMessage());
                }
            } catch (IOException e2) {
                Log.e(TAG, "Unable to create MediaCodec " + e2.getMessage());
            }
        } catch (IOException unused) {
            Log.e(TAG, "Unable to get path for " + file);
        }
    }

    public void stopEncoding() {
        if (this.mediaCodec == null || this.mediaMuxer == null) {
            Log.d(TAG, "Failed to stop encoding since it never started");
            return;
        }
        Log.d(TAG, "Stopping encoding");
        this.mNoMoreFrames = true;
        synchronized (this.mFrameSync) {
            CountDownLatch countDownLatch = this.mNewFrameLatch;
            if (countDownLatch != null && countDownLatch.getCount() > 0) {
                this.mNewFrameLatch.countDown();
            }
        }
    }

    public void abortEncoding() {
        if (this.mediaCodec == null || this.mediaMuxer == null) {
            Log.d(TAG, "Failed to abort encoding since it never started");
            return;
        }
        Log.d(TAG, "Aborting encoding");
        this.mNoMoreFrames = true;
        this.mAbort = true;
        this.mEncodeQueue = new ConcurrentLinkedQueue();
        synchronized (this.mFrameSync) {
            CountDownLatch countDownLatch = this.mNewFrameLatch;
            if (countDownLatch != null && countDownLatch.getCount() > 0) {
                this.mNewFrameLatch.countDown();
            }
        }
    }

    public void queueFrame(Bitmap bitmap) {
        if (this.mediaCodec == null || this.mediaMuxer == null) {
            Log.d(TAG, "Failed to queue frame. Encoding not started");
            return;
        }
        Log.d(TAG, "Queueing frame");
        this.mEncodeQueue.add(bitmap);
        synchronized (this.mFrameSync) {
            CountDownLatch countDownLatch = this.mNewFrameLatch;
            if (countDownLatch != null && countDownLatch.getCount() > 0) {
                this.mNewFrameLatch.countDown();
            }
        }
    }

    public void lambda$startEncoding$0$BitmapToVideoEncoder() {
        CountDownLatch countDownLatch;
        Log.d(TAG, "Encoder started");
        while (true) {
            if (this.mNoMoreFrames && this.mEncodeQueue.size() == 0) {
                break;
            }
            Bitmap poll = this.mEncodeQueue.poll();
            if (poll == null) {
                synchronized (this.mFrameSync) {
                    countDownLatch = new CountDownLatch(1);
                    this.mNewFrameLatch = countDownLatch;
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException unused) {
                }
                poll = this.mEncodeQueue.poll();
            }
            if (poll != null) {
                byte[] nv21 = getNV21(poll.getWidth(), poll.getHeight(), poll);
                int dequeueInputBuffer = this.mediaCodec.dequeueInputBuffer(500000L);
                long computePresentationTime = computePresentationTime(this.mGenerateIndex, 25);
                if (dequeueInputBuffer >= 0) {
                    ByteBuffer inputBuffer = this.mediaCodec.getInputBuffer(dequeueInputBuffer);
                    inputBuffer.clear();
                    inputBuffer.put(nv21);
                    this.mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, nv21.length, computePresentationTime, 0);
                    this.mGenerateIndex++;
                }
                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int dequeueOutputBuffer = this.mediaCodec.dequeueOutputBuffer(bufferInfo, 500000L);
                if (dequeueOutputBuffer == -1) {
                    Log.e(TAG, "No output from encoder available");
                } else if (dequeueOutputBuffer == -2) {
                    this.mTrackIndex = this.mediaMuxer.addTrack(this.mediaCodec.getOutputFormat());
                    this.mediaMuxer.start();
                } else if (dequeueOutputBuffer < 0) {
                    Log.e(TAG, "unexpected result from encoder.dequeueOutputBuffer: " + dequeueOutputBuffer);
                } else if (bufferInfo.size != 0) {
                    ByteBuffer outputBuffer = this.mediaCodec.getOutputBuffer(dequeueOutputBuffer);
                    if (outputBuffer == null) {
                        Log.e(TAG, "encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                    } else {
                        outputBuffer.position(bufferInfo.offset);
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
                        this.mediaMuxer.writeSampleData(this.mTrackIndex, outputBuffer, bufferInfo);
                        this.mediaCodec.releaseOutputBuffer(dequeueOutputBuffer, false);
                    }
                }
            }
        }
        release();
        if (this.mAbort) {
            this.mOutputFile.delete();
        } else {
            this.mCallback.onEncodingComplete(this.mOutputFile);
        }
    }

    private void release() {
        MediaCodec mediaCodec = this.mediaCodec;
        if (mediaCodec != null) {
            mediaCodec.stop();
            this.mediaCodec.release();
            this.mediaCodec = null;
            Log.d(TAG, "RELEASE CODEC");
        }
        MediaMuxer mediaMuxer = this.mediaMuxer;
        if (mediaMuxer != null) {
            mediaMuxer.stop();
            this.mediaMuxer.release();
            this.mediaMuxer = null;
            Log.d(TAG, "RELEASE MUXER");
        }
    }

    private byte[] getNV21(int i, int i2, Bitmap bitmap) {
        int i3 = i * i2;
        int[] iArr = new int[i3];
        bitmap.getPixels(iArr, 0, i, 0, 0, i, i2);
        byte[] bArr = new byte[(i3 * 3) / 2];
        encodeYUV420SP(bArr, iArr, i, i2);
        bitmap.recycle();
        return bArr;
    }

    private void encodeYUV420SP(byte[] bArr, int[] iArr, int i, int i2) {
        int i3 = i * i2;
        int i4 = 0;
        int i5 = 0;
        for (int i6 = 0; i6 < i2; i6++) {
            for (int i7 = 0; i7 < i; i7++) {
                int i8 = iArr[i5];
                int i9 = (iArr[i5] & 16711680) >> 16;
                int i10 = (iArr[i5] & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8;
                int i11 = 255;
                int i12 = (iArr[i5] & 255) >> 0;
                int i13 = (((((i9 * 66) + (i10 * 129)) + (i12 * 25)) + 128) >> 8) + 16;
                int i14 = (((((i9 * (-38)) - (i10 * 74)) + (i12 * 112)) + 128) >> 8) + 128;
                int i15 = (((((i9 * 112) - (i10 * 94)) - (i12 * 18)) + 128) >> 8) + 128;
                i4++;
                if (i13 < 0) {
                    i13 = 0;
                } else if (i13 > 255) {
                    i13 = 255;
                }
                bArr[i4] = (byte) i13;
                if (i6 % 2 == 0 && i5 % 2 == 0) {
                    int i16 = i3 + 1;
                    if (i14 < 0) {
                        i14 = 0;
                    } else if (i14 > 255) {
                        i14 = 255;
                    }
                    bArr[i3] = (byte) i14;
                    i3 = i16 + 1;
                    if (i15 < 0) {
                        i11 = 0;
                    } else if (i15 <= 255) {
                        i11 = i15;
                    }
                    bArr[i16] = (byte) i11;
                }
                i5++;
            }
        }
    }

    private long computePresentationTime(long j, int i) {
        return ((j * 1000000) / i) + 132;
    }

    public File getOutputFile() {
        return this.mOutputFile;
    }
}
