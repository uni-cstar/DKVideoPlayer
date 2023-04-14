package xyz.doikki.dkplayer.widget.controller

import android.content.Context
import android.content.pm.ActivityInfo
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import droid.unicstar.videoplayer.UNSVideoView
import xyz.doikki.dkplayer.R
import xyz.doikki.videocontroller.StandardVideoController
import xyz.doikki.videocontroller.component.VodControlView
import droid.unicstar.videoplayer.controller.UNSVideoViewControl

class PortraitWhenFullScreenController @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : StandardVideoController(context, attrs, defStyleAttr), View.OnClickListener {

    private val mFullScreen: View

    init {
        val vodControlView = VodControlView(context)
        vodControlView.showBottomProgress = false
        mFullScreen = vodControlView.findViewById(R.id.fullscreen)
        mFullScreen.setOnClickListener { toggleFullScreen() }
        addControlComponent(vodControlView)
    }

    override fun setMediaPlayer(mediaPlayer: UNSVideoViewControl) {
        super.setMediaPlayer(mediaPlayer)
        //不监听设备方向
        mediaPlayer.setEnableOrientationSensor(false)
    }

    override fun toggleFullScreen(): Boolean {
        val activity = mActivity ?: return false
        val o = activity.requestedOrientation
        if (o == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        mFullScreen.isSelected = o != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        adjustView()
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        if (!mPlayer!!.isFullScreen()) {
            mPlayer!!.startVideoViewFullScreen()
            return true
        }
        toggleShowState()
        return true
    }

    override fun onScreenModeChanged(screenMode: Int) {
        super.onScreenModeChanged(screenMode)
        if (screenMode == UNSVideoView.SCREEN_MODE_FULL) {
            mFullScreen.isSelected = false
        } else {
            hide()
        }
        adjustView()
    }

    private fun adjustView() {

        invokeOnPlayerAttached {
            if (mActivity != null && it.hasCutout()) {
                val orientation = mActivity!!.requestedOrientation
                val cutoutHeight = it.getCutoutHeight()
                if (orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    setPadding(0, cutoutHeight, 0, 0)
                } else if (orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setPadding(cutoutHeight, 0, 0, 0)
                }
            }
        }

    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.fullscreen) {
            toggleFullScreen()
        } else if (i == R.id.lock) {
            toggleLock()
        } else if (i == R.id.iv_play) {
            togglePlay()
        } else if (i == R.id.back) {
            stopFullScreen()
        } else if (i == R.id.thumb) {
            mPlayer!!.start()
            mPlayer!!.startVideoViewFullScreen()
        } else if (i == R.id.iv_replay) {
            mPlayer!!.replay(true)
            mPlayer!!.startVideoViewFullScreen()
        }
    }
}