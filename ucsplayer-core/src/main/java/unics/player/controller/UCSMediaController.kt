package unics.player.controller

import androidx.annotation.IntRange

/**
 * 视图控制器
 */
interface UCSMediaController {
    /**
     * 控制视图是否处于显示状态
     */
    val isShowing: Boolean

    /**
     * 显示控制视图
     */
    fun show()

    /**
     * 隐藏控制视图
     */
    fun hide()

    /**
     * 启动自动隐藏控制器视图
     */
    fun startFadeOut()

    /**
     * 移除自动隐藏控制器视图
     */
    fun stopFadeOut()

    /**
     * 设置自动隐藏倒计时持续的时间
     *
     * @param timeout 默认4000，比如大于0才会生效
     */
    fun setFadeOutTime(@IntRange(from = 1) timeout: Int)

    /**
     * 锁定状态
     */
    var isLocked: Boolean

    /**
     * 是否是全屏状态
     *
     * @return
     */
    val isFullScreen: Boolean

    /**
     * 横竖屏切换:用来代理[UCSContainerControl.toggleFullScreen],即通过Controller调用VideoView的方法
     */
    fun toggleFullScreen(): Boolean

    /**
     * 开始全屏
     *
     * @return
     */
    fun startFullScreen(): Boolean {
        return startFullScreen(false)
    }

    /**
     * 开始全屏:用来代理[UCSContainerControl.startFullScreen]} ,即通过Controller调用VideoView的方法
     *
     * @return
     */
    fun startFullScreen(isLandscapeReversed: Boolean): Boolean

    /**
     * 结束全屏:用来代理[UCSContainerControl.stopFullScreen],即通过Controller调用VideoView的方法
     *
     * @return
     */
    fun stopFullScreen(): Boolean

    /**
     * 开始刷新进度
     */
    fun startUpdateProgress()

    /**
     * 停止刷新进度
     */
    fun stopUpdateProgress()
}