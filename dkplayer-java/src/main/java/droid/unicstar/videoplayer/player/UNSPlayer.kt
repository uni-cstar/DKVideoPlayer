package droid.unicstar.videoplayer.player

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.SurfaceHolder
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import droid.unicstar.videoplayer.PartialFunc

/**
 * 抽象的播放器，继承此接口扩展自己的播放器
 * 备注：本类的职责应该完全定位在播放器的“能力”上，因此只考虑播放相关的逻辑（不包括UI层面）
 * Created by Doikki on 2017/12/21.
 * update by luochao on 2022/9/16. 调整部分代码及结构
 * @see BaseUNSPlayer
 */
interface UNSPlayer {

    companion object {
        /**
         * 视频/音频开始渲染
         */
        const val MEDIA_INFO_VIDEO_RENDERING_START = MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START

        /**
         * 缓冲开始
         */
        const val MEDIA_INFO_BUFFERING_START = MediaPlayer.MEDIA_INFO_BUFFERING_START

        /**
         * 缓冲结束
         */
        const val MEDIA_INFO_BUFFERING_END = MediaPlayer.MEDIA_INFO_BUFFERING_END

        /**
         * 视频旋转信息
         */
        const val MEDIA_INFO_VIDEO_ROTATION_CHANGED = 10001

        /**
         * 未知错误
         */
        const val MEDIA_ERROR_UNKNOWN = MediaPlayer.MEDIA_ERROR_UNKNOWN


        /**
         * 播放出错
         */
        const val STATE_ERROR = -1

        /**
         * 闲置中
         */
        const val STATE_IDLE = 0

        /**
         * 准备中：处于已设置了播放数据源，但是播放器还未回调[UNSPlayer.EventListener.onPrepared]
         */
        const val STATE_PREPARING = 1

        /**
         * 已就绪
         */
        const val STATE_PREPARED = 2

        /**
         * 已就绪但终止状态
         * 播放过程中停止继续播放：比如手机不允许在手机流量的时候进行播放（此时播放器处于已就绪未播放中状态）
         */
        const val STATE_PREPARED_BUT_ABORT = 8

        /**
         * 播放中
         */
        const val STATE_PLAYING = 3

        /**
         * 暂停中
         */
        const val STATE_PAUSED = 4

        /**
         * 播放结束
         */
        const val STATE_PLAYBACK_COMPLETED = 5

        /**
         * 缓冲中
         */
        const val STATE_BUFFERING = 6

        /**
         * 缓冲结束
         */
        const val STATE_BUFFERED = 7
    }

    /**
     * 播放器状态
     */
    @IntDef(
        //出错
        STATE_ERROR,
        //闲置
        STATE_IDLE,
        //准备数据源中：setDatasource与onPrepared之间
        STATE_PREPARING,
        //数据源已准备：onPrepared回调
        STATE_PREPARED,
        //开始播放：调用start()之后
        STATE_PLAYING,
        //暂停
        STATE_PAUSED,
        //播放结束
        STATE_PLAYBACK_COMPLETED,
        //缓冲中
        STATE_BUFFERING,
        //缓冲结束
        STATE_BUFFERED,
        //已准备但因为用户设置不允许移动网络播放而中断：onPrepared回调之后并没有调用start
        STATE_PREPARED_BUT_ABORT
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class PlayState

    /**
     * 事件监听器
     */
    interface EventListener {

        /**
         * 播放就绪
         */
        fun onPrepared() {}

        /**
         * 播放信息
         */
        fun onInfo(what: Int, extra: Int) {}

        /**
         * 视频大小发生变化
         */
        fun onVideoSizeChanged(width: Int, height: Int) {}

        /**
         * 播放完成
         */
        fun onCompletion() {}

        /**
         * 播放错误
         */
        fun onError(e: Throwable) {
            println("播放器出错了：${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * 播放器状态发生变化监听
     */
    fun interface OnPlayStateChangeListener {
        /**
         * 播放器播放状态发生了变化
         *
         * @param playState
         */
        fun onPlayStateChanged(@UNSPlayer.PlayState playState: Int)
    }

    /**
     * 初始化，一个特殊的方法，在[UNSPlayer]创建之后调用，避免
     * todo 如何规避该方法？
     */
    fun onInit(){

    }

    /**
     * 设置播放地址
     *
     * @param path    播放地址
     */
    fun setDataSource(context: Context, path: String) = setDataSource(context, path, null)

    /**
     * 设置播放地址
     *
     * @param path    播放地址
     * @param headers 播放地址请求头
     */
    fun setDataSource(context: Context, path: String, headers: Map<String, String>?) =
        setDataSource(context, Uri.parse(path), headers)

    /**
     * 设置播放地址
     *
     * @param uri    the Content URI of the data you want to play
     */
    fun setDataSource(context: Context, uri: Uri) = setDataSource(context, uri, null)

    /**
     * 设置播放地址
     *
     * @param uri    the Content URI of the data you want to play
     * @param headers 播放地址请求头
     */
    fun setDataSource(context: Context, uri: Uri, headers: Map<String, String>?)

    /**
     * 用于播放raw和asset里面的视频文件
     */
    fun setDataSource(fd: AssetFileDescriptor)

    /**
     * 异步准备
     */
    fun prepareAsync()

    /**
     * 开始播放
     */
    fun start()

    /**
     * 是否正在播放
     */
    fun isPlaying(): Boolean

    /**
     * 获取当前播放的位置,单位 msec
     */
    fun getCurrentPosition(): Long

    /**
     * 获取视频总时长,单位 msec
     */
    fun getDuration(): Long

    /**
     * 获取缓冲百分比
     */
    @IntRange(from = 0, to = 100)
    fun getBufferedPercentage(): Int

    /**
     * 设置循环播放
     */
    fun setLooping(isLooping: Boolean)

    /**
     * 设置音量 ；0.0f-1.0f 之间
     *
     * @param leftVolume  左声道音量
     * @param rightVolume 右声道音量
     */
    fun setVolume(
        @FloatRange(from = 0.0, to = 1.0) leftVolume: Float,
        @FloatRange(from = 0.0, to = 1.0) rightVolume: Float
    )

    /**
     * 获取播放速度 0.5f：表示0.5倍数 2f:表示2倍速
     * 注意：使用系统播放器时，只有6.0及以上系统才支持，6.0以下默认返回1
     */
    @PartialFunc(message = "使用系统播放器时，只有6.0及以上系统才支持")
    fun getSpeed(): Float

    /**
     * 设置播放速度 0.5f：表示0.5倍数 2f:表示2倍速
     * 注意：使用系统播放器时，只有6.0及以上系统才支持
     */
    @PartialFunc(message = "使用系统播放器时，只有6.0及以上系统才支持")
    fun setSpeed(speed: Float)

    /**
     * 调整进度
     *
     * @param msec the offset in milliseconds from the start to seek to;偏移位置（毫秒）
     */
    fun seekTo(msec: Long)

    /**
     * 暂停
     */
    fun pause()

    /**
     * 停止
     */
    fun stop()

    /**
     * todo：重置播放器 此方法按道理是重置播放器之后可以重复使用
     */
    fun reset()

    /**
     * 释放播放器
     * 释放之后此播放器就不能再被使用
     */
    fun release()

    /**
     * 设置渲染视频的View,主要用于TextureView
     */
    fun setSurface(surface: Surface?)

    /**
     * 设置渲染视频的View,主要用于SurfaceView
     */
    fun setDisplay(holder: SurfaceHolder?)

    /**
     * 设置播放器事件监听
     */
    fun setEventListener(eventListener: EventListener?)

    /**
     * 获取当前缓冲的网速
     */
    @PartialFunc(message = "IJK播放器才支持")
    fun getTcpSpeed(): Long {
        return 0
    }

}