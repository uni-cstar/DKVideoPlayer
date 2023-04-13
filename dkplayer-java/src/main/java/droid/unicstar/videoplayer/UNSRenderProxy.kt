package droid.unicstar.videoplayer

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.IntRange
import droid.unicstar.videoplayer.controller.UNSRenderControl
import droid.unicstar.videoplayer.player.UNSPlayer
import droid.unicstar.videoplayer.render.AspectRatioType
import droid.unicstar.videoplayer.render.UNSRender
import droid.unicstar.videoplayer.render.UNSRenderFactory
import xyz.doikki.videoplayer.DKManager

/**
 * 在使用之前，必须调用[bindContainer]指定容器
 */
class UNSRenderProxy() : UNSRender, UNSRenderControl {

    /**
     * Render所在的容器，必须指定
     */
    private lateinit var mContainer: FrameLayout

    private inline val mContext: Context get() = mContainer.context

    //关联的播放器
    private var mAttachedPlayer: UNSPlayer? = null

    //真正的渲染视图
    private var mRender: UNSRender? = null

    //自定义Render工厂
    private var mRenderFactory: UNSRenderFactory = DKManager.renderFactory

    //render是否可以重用：如果不重用，则每次播放的时候会重新创建一个新的视图层，否则将会使用同一个render
    private var mRenderReusable = DKManager.isRenderReusable

    //渲染视图纵横比
    @AspectRatioType
    private var mScreenAspectRatioType = DKManager.screenAspectRatioType

    //视频画面大小
    private val mVideoSize = intArrayOf(0, 0)
    private var mSurfaceListener: UNSRender.SurfaceListener? = null
    private var mMirrorRotation: Boolean = false

    @IntRange(from = 0, to = 360)
    private var mVideoRotation: Int = 0

    /**
     * 获取渲染视图的名字
     * @return
     */
    val renderName: String
        get() {
            return if (mRender != null) {
                val className = mRender!!.javaClass.name
                className.substring(className.lastIndexOf(".") + 1)
            } else {
                val className = mRenderFactory.javaClass.name
                className.substring(className.lastIndexOf(".") + 1)
            }
        }

    /**
     * 视频画面大小
     * todo 是否适合直接返回该变量,存在被外层修改的可能？是否应该 return new int[]{mVideoSize[0], mVideoSize[1]}
     */
    override fun getVideoSize(): IntArray {
        return mVideoSize
    }

    /**
     * 绑定Render所在的容器
     * @note 限定了FrameLayout，更适合放render的位置
     */
    fun bindContainer(container: FrameLayout) {
        mContainer = container
    }

    /**
     * render是否可以重用
     */
    fun setRenderReusable(reusable: Boolean) {
        mRenderReusable = reusable
    }

    /**
     * 自定义RenderView，继承[UNSRenderFactory]实现自己的RenderView,设置为null则会使用[DKManager.renderFactory]
     */
    override fun setRenderViewFactory(factory: UNSRenderFactory?) {
        if (mRenderFactory == factory || (factory == null && mRenderFactory == DKManager.renderFactory)) {
            //与当前工厂相同或者与全局工厂相同,即当前工厂并没有发生任何变化，不作任何处理
            return
        }
        mRenderFactory = factory.orDefault(DKManager.renderFactory)

        //render工厂发生了变化
        //如果之前已存在render，则将以前的render移除释放并重新创建
        releaseCurrentRender()
        //判断是否需要立马创建render
        mAttachedPlayer?.let {
            attachRender(it)
        }
    }

    private fun attachRender(player: UNSPlayer) {
        logd("[RenderProxy]:attachRender")
        val render = mRender ?: createRender()
        logd("[RenderProxy]:attachRender render=$render")
        render.bindPlayer(player)
    }

    private fun createRender(): UNSRender {

        return mRenderFactory.create(mContext).also { render ->
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            //设置之前配置
            render.setSurfaceListener(mSurfaceListener)
            render.setAspectRatioType(mScreenAspectRatioType)
            if (mVideoRotation != 0)
                render.setVideoRotation(mVideoRotation)
            render.setMirrorRotation(mMirrorRotation)
            if (mVideoSize[0] > 0 || mVideoSize[1] > 0)
                render.setVideoSize(mVideoSize[0], mVideoSize[1])
            render.view?.let {
                //render添加到最底层
                mContainer.addView(render.view, 0, params)
            }
            mRender = render
            logd("[RenderProxy]:createRender render=$render")
        }
    }

    /**
     * 释放当前的render
     */
    private fun releaseCurrentRender() {
        mRender?.let {
            releaseRender(it)
        }
        mRender = null
    }

    private fun releaseRender(render: UNSRender) {
        render.view?.let {
            mContainer.removeView(it)
        }
        render.release()
    }

    /**
     * 重置
     */
    fun reset() {
        releaseCurrentRender()
        mScreenAspectRatioType = DKManager.screenAspectRatioType
        mVideoSize[0] = 0
        mVideoSize[1] = 0
        mMirrorRotation = false
        mVideoRotation = 0
    }

    override val view: View?
        get() = mRender?.view

    override fun bindPlayer(player: UNSPlayer?) {
        logd("[RenderProxy]:bindPlayer")
        if (!mRenderReusable) {//不重用render，则立即释放当前的render
            logd("[RenderProxy]:releaseCurrentRender renderReusable=$mRenderReusable")
            releaseCurrentRender()
        }
        mAttachedPlayer = player
        if (player != null)
            attachRender(player)
    }

    override fun setSurfaceListener(listener: UNSRender.SurfaceListener?) {
        this.mSurfaceListener = listener
        mRender?.setSurfaceListener(listener)
    }

    /**
     * 旋转视频画面
     *
     * @param degree 旋转角度
     */
    override fun setVideoRotation(@IntRange(from = 0, to = 360) degree: Int) {
        mVideoRotation = degree
        mRender?.setVideoRotation(degree)
    }

    override fun setMirrorRotation(enable: Boolean) {
        mMirrorRotation = enable
        mRender?.setMirrorRotation(enable)
    }

    override fun setAspectRatioType(@AspectRatioType aspectRatioType: Int) {
        mScreenAspectRatioType = aspectRatioType
        mRender?.setAspectRatioType(aspectRatioType)
    }

    /**
     * 视频大小发生变化：用于[UNSVideoView]或者持有播放器[UNSPlayer]的对象在[UNSPlayer.EventListener.onVideoSizeChanged]回调时进行调用
     */
    override fun setVideoSize(videoWidth: Int, videoHeight: Int) {
        mVideoSize[0] = videoWidth
        mVideoSize[1] = videoHeight
        mRender?.setVideoSize(videoWidth, videoHeight)
    }

    override fun screenshot(callback: UNSRender.ScreenShotCallback) {
        super<UNSRender>.screenshot(callback)
    }

    override fun screenshot(highQuality: Boolean, callback: UNSRender.ScreenShotCallback) {
        val render = mRender
        if (render != null) {
            render.screenshot(highQuality, callback)
            return
        }
        Log.w("DKPlayer", "render is null , screenshot is ignored.")
        callback.onScreenShotResult(null)
    }

    /**
     * 释放资源
     */
    override fun release() {
        mAttachedPlayer = null
        releaseCurrentRender()
        //关闭屏幕常亮
        mContainer.keepScreenOn = false
    }
}