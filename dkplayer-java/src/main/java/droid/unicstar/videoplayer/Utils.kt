package droid.unicstar.videoplayer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import droid.unicstar.videoplayer.player.UNSPlayer
import xyz.doikki.videoplayer.DKManager.isDebuggable
import xyz.doikki.videoplayer.util.L

@PublishedApi
internal const val TAG = "UCSPlayer"

var isDebug = isDebuggable

internal inline fun logd(msg: String) {
    logd(TAG, msg)
}

internal inline fun logd(tag: String, msg: String) {
    if (isDebug) {
        Log.d(tag, msg)
    }
}

@JvmOverloads
inline fun loge(tag: String = TAG, msg: String) {
    if (isDebug) {
        Log.e(tag, msg)
    }
}

@JvmOverloads
inline fun logi(tag: String = TAG, msg: String) {
    if (isDebug) {
        Log.i(tag, msg)
    }
}

inline fun logw(msg: String) {
    logw(TAG, msg)
}

inline fun logw(tag: String, msg: String) {
    if (isDebug) {
        Log.w(tag, msg)
    }
}

/**
 * 等同于[trySilent]，只是本方法没有对结果进行装箱处理（即没有产生[Result]中间对象）
 */
internal inline fun <T> T.tryIgnore(action: T.() -> Unit): Throwable? {
    return try {
        action(this)
        null
    } catch (e: Throwable) {
        L.w("error on ${Thread.currentThread().stackTrace[2].methodName} method invoke.but throwable is ignored.")
        e.printStackTrace()
        e
    }
}

internal inline fun Boolean?.orDefault(def: Boolean = false): Boolean {
    return this ?: def
}

internal inline fun Int?.orDefault(def: Int = 0) = this ?: def
internal inline fun Float?.orDefault(def: Float = 0f) = this ?: def
internal inline fun Long?.orDefault(def: Long = 0) = this ?: def
internal inline fun Double?.orDefault(def: Double = 0.0) = this ?: def
internal inline fun <T> T?.orDefault(default: T): T = this ?: default
internal inline fun <T> T?.orDefault(initializer: () -> T): T = this ?: initializer()
internal inline fun <reified K> Map<*, *>.loopKeyWhen(block: (K) -> Unit) {
    for ((key) in this) {
        if (key is K) {
            block(key)
        }
    }
}

internal inline fun <reified V> Map<*, *>.loopValueWhen(block: (V) -> Unit) {
    for ((_, value) in this) {
        if (value is V) {
            block(value)
        }
    }
}

internal inline fun <K> MutableMap<K, *>.removeAllByKey(block: (K) -> Boolean) {
    val it: MutableIterator<Map.Entry<K, *>> = this.iterator()
    while (it.hasNext()) {
        val (key, _) = it.next()
        if (block(key)) {
            it.remove()
        }
    }
}

internal inline fun <V> MutableMap<*, V>.removeAllByValue(filter: (V) -> Boolean) {
    val it: MutableIterator<Map.Entry<*, V>> = this.iterator()
    while (it.hasNext()) {
        val (_, value) = it.next()
        if (filter(value)) {
            it.remove()
        }
    }
}

internal inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

/**
 * 从Parent中移除自己
 */
internal inline fun View.removeFromParent() {
    (parent as? ViewGroup)?.removeView(this)
}

internal inline val Activity.decorView: ViewGroup? get() = window.decorView as? ViewGroup
internal inline val Activity.contentView: ViewGroup? get() = findViewById(android.R.id.content)

/**
 * 从Context中获取Activity上下文
 */
internal fun Context.getActivityContext(): Activity? {
    if (this is Activity) {
        return this
    } else if (this is ContextWrapper) {
        return this.baseContext.getActivityContext()
    }
    return null
}

internal inline val Context.layoutInflater: LayoutInflater get() = LayoutInflater.from(this)
internal inline val View.layoutInflater: LayoutInflater get() = context.layoutInflater

@PublishedApi
internal inline fun Context.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

internal inline fun Context.toast(@StringRes messageId: Int, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, messageId, length).show()
}

@PublishedApi
internal inline fun View.toast(message: String, length: Int = Toast.LENGTH_SHORT) {
    context.toast(message, length)
}

internal inline fun View.toast(@StringRes messageId: Int, length: Int = Toast.LENGTH_SHORT) {
    context.toast(messageId, length)
}

internal fun TextView.setTextOrGone(message: CharSequence?) {
    visibility = if (message.isNullOrEmpty()) {
        View.GONE
    } else {
        View.VISIBLE
    }
    text = message
}

/**
 * 是否是第一次按下按键
 */
internal val KeyEvent.isUniqueDown: Boolean get() = action == KeyEvent.ACTION_DOWN && repeatCount == 0
internal const val INVALIDATE_SEEK_POSITION = -1

/**
 * 能否获取焦点
 */
internal val View.canTakeFocus: Boolean
    get() = isFocusable && this.visibility == View.VISIBLE && isEnabled

/**
 * Returns a string containing player state debugging information.
 */
internal fun screenMode2str(@UNSVideoView.ScreenMode mode: Int): String? {
    val playerStateString: String = when (mode) {
        UNSVideoView.SCREEN_MODE_NORMAL -> "normal"
        UNSVideoView.SCREEN_MODE_FULL -> "full screen"
        UNSVideoView.SCREEN_MODE_TINY -> "tiny screen"
        else -> "normal"
    }
    return String.format("screenMode: %s", playerStateString)
}

/**
 * Returns a string containing player state debugging information.
 */
internal fun playState2str(state: Int): String? {
    val playStateString: String = when (state) {
        UNSPlayer.STATE_IDLE -> "idle"
        UNSPlayer.STATE_PREPARING -> "preparing"
        UNSPlayer.STATE_PREPARED -> "prepared"
        UNSPlayer.STATE_PLAYING -> "playing"
        UNSPlayer.STATE_PAUSED -> "pause"
        UNSPlayer.STATE_BUFFERING -> "buffering"
        UNSPlayer.STATE_BUFFERED -> "buffered"
        UNSPlayer.STATE_PLAYBACK_COMPLETED -> "playback completed"
        UNSPlayer.STATE_ERROR -> "error"
        else -> "idle"
    }
    return String.format("playState: %s", playStateString)
}