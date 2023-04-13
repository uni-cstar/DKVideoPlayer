package xyz.doikki.videoplayer.ijk

import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer.OnNativeInvokeListener
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import droid.unicstar.videoplayer.player.BaseUNSPlayer
import droid.unicstar.videoplayer.player.UNSPlayer
import droid.unicstar.videoplayer.player.CSPlayerException
import droid.unicstar.videoplayer.orDefault

open class IjkDKPlayer(private val appContext: Context) : BaseUNSPlayer(),
    IMediaPlayer.OnErrorListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnInfoListener,
    IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnPreparedListener,
    IMediaPlayer.OnVideoSizeChangedListener, OnNativeInvokeListener {

    @JvmField
    protected var kernel: IjkMediaPlayer? = null

    private var bufferedPercent = 0

    private fun createKernel(): IjkMediaPlayer {
        return IjkMediaPlayer().also {
            it.setOnErrorListener(this)
            it.setOnCompletionListener(this)
            it.setOnInfoListener(this)
            it.setOnBufferingUpdateListener(this)
            it.setOnPreparedListener(this)
            it.setOnVideoSizeChangedListener(this)
            it.setOnNativeInvokeListener(this)
        }
    }

    override fun init() {
        //native日志 todo  java.lang.UnsatisfiedLinkError: No implementation found for void tv.danmaku.ijk.media.player.IjkMediaPlayer.native_setLogLevel(int)
//        IjkMediaPlayer.native_setLogLevel(if (isDebuggable) IjkMediaPlayer.IJK_LOG_INFO else IjkMediaPlayer.IJK_LOG_SILENT)
        kernel = createKernel()
        IjkMediaPlayer.native_setLogLevel(IjkMediaPlayer.IJK_LOG_WARN)
    }

    override fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?) {
        try {
            if (ContentResolver.SCHEME_ANDROID_RESOURCE == uri.scheme) {
                val rawDataSourceProvider = RawDataSourceProvider.create(appContext, uri)
                kernel!!.setDataSource(rawDataSourceProvider)
            } else {
                if (headers != null && headers.containsKey("User-Agent")) {
                    //处理UA问题
                    //update by luochao: 直接在Map参数中移除字段，可能影响调用者的逻辑
                    val clonedHeaders: MutableMap<String, String> = HashMap(headers)
                    // 移除header中的User-Agent，防止重复
                    val userAgent = clonedHeaders.remove("User-Agent")
                    //                    if (TextUtils.isEmpty(userAgent)) {
//                        userAgent = "";
//                    }
                    kernel!!.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "user_agent", userAgent)
                    kernel!!.setDataSource(appContext, uri, clonedHeaders)
                } else {
                    //不包含UA，直接设置
                    kernel!!.setDataSource(appContext, uri, headers)
                }
            }
        } catch (e: Throwable) {
            mEventListener!!.onError(e)
        }
    }

    override fun setDataSource(fd: AssetFileDescriptor) {
        try {
            kernel!!.setDataSource(RawDataSourceProvider(fd))
        } catch (e: Exception) {
            mEventListener!!.onError(e)
        }
    }

    override fun pause() {
        try {
            kernel!!.pause()
        } catch (e: IllegalStateException) {
            mEventListener!!.onError(e)
        }
    }

    override fun start() {
        try {
            kernel!!.start()
        } catch (e: IllegalStateException) {
            mEventListener!!.onError(e)
        }
    }

    override fun stop() {
        try {
            kernel!!.stop()
        } catch (e: IllegalStateException) {
            mEventListener!!.onError(e)
        }
    }

    override fun prepareAsync() {
        try {
            //需要手动调用开始播放
            kernel!!.applyPreferredOptions()
            kernel!!.setAutoPlayOnPrepared(false)
            kernel!!.prepareAsync()
        } catch (e: IllegalStateException) {
            mEventListener!!.onError(e)
        }
    }

    override fun reset() {
        kernel?.let {
            it.reset()
            it.setOnVideoSizeChangedListener(this)
        }
    }

    override fun isPlaying(): Boolean {
        return kernel!!.isPlaying
    }

    override fun seekTo(msec: Long) {
        try {
            kernel!!.seekTo(msec.toInt().toLong())
        } catch (e: IllegalStateException) {
            mEventListener!!.onError(e)
        }
    }

    override fun release() {
        kernel?.let {
            it.setOnErrorListener(null)
            it.setOnCompletionListener(null)
            it.setOnInfoListener(null)
            it.setOnBufferingUpdateListener(null)
            it.setOnPreparedListener(null)
            it.setOnVideoSizeChangedListener(null)
            object : Thread() {
                val temp = it
                override fun run() {
                    try {
                        temp.release()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.start()
        }
        kernel = null
    }

    override fun getCurrentPosition(): Long {
        return kernel!!.currentPosition
    }

    override fun getDuration(): Long {
        return kernel!!.duration
    }

    override fun getBufferedPercentage(): Int {
        return bufferedPercent
    }

    override fun setSurface(surface: Surface?) {
        kernel!!.setSurface(surface)
    }

    override fun setDisplay(holder: SurfaceHolder?) {
        kernel?.setDisplay(holder)
    }

    override fun setVolume(v1: Float, v2: Float) {
        kernel!!.setVolume(v1, v2)
    }

    override fun setLooping(isLooping: Boolean) {
        kernel!!.isLooping = isLooping
    }

    override fun setSpeed(speed: Float) {
        kernel!!.setSpeed(speed)
    }

    override fun getSpeed(): Float {
        return kernel!!.getSpeed(0f)
    }

    override fun getTcpSpeed(): Long {
        return kernel?.tcpSpeed.orDefault()
    }

    override fun onError(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
        mEventListener!!.onError(
            CSPlayerException(
                what,
                extra
            )
        )
        return true
    }

    override fun onCompletion(mp: IMediaPlayer) {
        mEventListener!!.onCompletion()
    }

    override fun onInfo(mp: IMediaPlayer, what: Int, extra: Int): Boolean {
        mEventListener!!.onInfo(what, extra)
        return true
    }

    override fun onBufferingUpdate(mp: IMediaPlayer, percent: Int) {
        bufferedPercent = percent
    }

    override fun onPrepared(mp: IMediaPlayer) {
        mEventListener!!.onPrepared()
        // 修复播放纯音频时状态出错问题
        if (!isVideo) {
            mEventListener!!.onInfo(UNSPlayer.MEDIA_INFO_VIDEO_RENDERING_START, 0)
        }
    }

    private val isVideo: Boolean
        private get() {
            val trackInfo = kernel!!.trackInfo ?: return false
            for (info in trackInfo) {
                if (info.trackType == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                    return true
                }
            }
            return false
        }

    override fun onVideoSizeChanged(
        mp: IMediaPlayer,
        width: Int,
        height: Int,
        sar_num: Int,
        sar_den: Int
    ) {
        val videoWidth = mp.videoWidth
        val videoHeight = mp.videoHeight
        //todo 发现某些视频回调为0
//        if (videoWidth != 0 && videoHeight != 0) {
        mEventListener!!.onVideoSizeChanged(videoWidth, videoHeight)
//        }

    }

    override fun onNativeInvoke(what: Int, args: Bundle): Boolean {
        return true
    }


}