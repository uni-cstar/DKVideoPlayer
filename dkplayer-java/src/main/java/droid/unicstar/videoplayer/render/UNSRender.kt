package droid.unicstar.videoplayer.render

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.Surface
import android.view.View
import androidx.annotation.IntRange
import droid.unicstar.videoplayer.player.UNSPlayer
import droid.unicstar.videoplayer.PartialFunc

interface UNSRender {

    companion object {

        /**
         * 创建截图的Bitmap容器
         */
        @JvmStatic
        fun createShotBitmap(
            context: Context,
            width: Int,
            height: Int,
            highQuality: Boolean
        ): Bitmap {
            val config = if (highQuality) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
            return if (Build.VERSION.SDK_INT >= 17) {
                Bitmap.createBitmap(
                    context.resources.displayMetrics,
                    width, height, config
                )
            } else {
                Bitmap.createBitmap(width, height, config)
            }
        }

        /**
         * 创建截图的Bitmap容器
         */
        @JvmStatic
        fun createShotBitmap(render: UNSRender, highQuality: Boolean): Bitmap {
            val view = render.view
            return createShotBitmap(view!!.context, view.width, view.height, highQuality)
        }

    }

    interface SurfaceListener {
        /**
         * Invoked when a [UNSRender]'s Surface is ready for use.
         *
         * @param surface The surface returned by getSurfaceTexture()
         */
        fun onSurfaceAvailable(surface: Surface?)

        /**
         * Invoked when the [SurfaceTexture]'s buffers size changed.
         *
         * @param surface The surface returned by
         * [android.view.TextureView.getSurfaceTexture]
         * @param width   The new width of the surface
         * @param height  The new height of the surface
         */
        fun onSurfaceSizeChanged(surface: Surface?, width: Int, height: Int)
        fun onSurfaceDestroyed(surface: Surface?): Boolean
        fun onSurfaceUpdated(surface: Surface?)
    }

    /**
     * 截图回调
     */
    fun interface ScreenShotCallback {
        /**
         * 截图结果
         *
         * @param bmp
         */
        fun onScreenShotResult(bmp: Bitmap?)
    }

    /**
     * 获取真实的RenderView:用于挂在view tree上
     */
    val view: View?

    /**
     * 绑定播放器
     * @param player 播放器，如果为null，则会解除之前的播放器持有
     */
    fun bindPlayer(player: UNSPlayer?)

    /**
     * 释放资源
     */
    fun release()

    /**************以下为Render的功能能力 */
    /**
     * 设置Surface监听
     *
     * @param listener
     */
    fun setSurfaceListener(listener: SurfaceListener?)

    /**
     * 设置界面比例模式
     *
     * @param aspectRatioType 比例类型
     */
    fun setAspectRatioType(@AspectRatioType aspectRatioType: Int)

    /**
     * 设置视频旋转角度
     *
     * @param degree 角度值
     */
    @PartialFunc(message = "TextureView才支持")
    fun setVideoRotation(@IntRange(from = 0, to = 360) degree: Int){
        //默认不支持镜像旋转;只有TextureView才支持
    }

    /**
     * 设置镜像旋转，暂不支持SurfaceView
     *
     * @param enable
     */
    @PartialFunc(message = "TextureView才支持")
    fun setMirrorRotation(enable: Boolean) {
        //默认不支持镜像旋转;只有TextureView才支持
    }

    /**
     * 设置视频宽高：用于测量控件的尺寸和比例（通常是Player的回调中调用该方法设置）
     *
     * @param videoWidth  宽
     * @param videoHeight 高
     */
    fun setVideoSize(videoWidth: Int, videoHeight: Int)

    /**
     * 截图
     */
    fun screenshot(callback: ScreenShotCallback) {
        screenshot(false, callback)
    }

    /**
     * 截图
     *
     * @param highQuality 是否采用高质量，默认false；
     * 如果设置为true，则[UNSRender.ScreenShotCallback]返回的[Bitmap]采用[Bitmap.Config.ARGB_8888]配置，相反则采用[Bitmap.Config.RGB_565]
     * @param callback    回调
     * @see Bitmap.Config
     */
    @PartialFunc(message = "SurfaceView在Android 7.0（24）版本及以后才支持")
    fun screenshot(highQuality: Boolean, callback: ScreenShotCallback)

}