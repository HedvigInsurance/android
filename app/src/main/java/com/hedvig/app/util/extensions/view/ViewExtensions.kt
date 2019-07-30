package com.hedvig.app.util.extensions.view

import android.graphics.Rect
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.appcompat.app.AppCompatActivity
import android.view.HapticFeedbackConstants
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.RelativeLayout
import com.hedvig.app.util.extensions.compatFont
import com.hedvig.app.util.whenApiVersion
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.app_bar.view.*

fun View.show(): View {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
    }
    return this
}

fun View.hide(): View {
    if (visibility != View.INVISIBLE) {
        visibility = View.INVISIBLE
    }

    return this
}

fun View.remove(): View {
    if (visibility != View.GONE) {
        this.visibility = View.GONE
    }

    return this
}

fun View.disable() {
    isEnabled = false
    alpha = 0.2f
}

fun View.enable() {
    isEnabled = true
    alpha = 1f
}

fun View.increaseTouchableArea(additionalArea: Int): View {
    val parent = (this.parent as View)
    parent.post {
        val touchableArea = Rect()
        getHitRect(touchableArea)
        touchableArea.top -= additionalArea
        touchableArea.left -= additionalArea
        touchableArea.right += additionalArea
        touchableArea.bottom += additionalArea
        parent.touchDelegate = TouchDelegate(touchableArea, this)
    }

    return this
}

inline fun View.doOnLayout(crossinline action: () -> Unit) =
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    })

fun View.setHapticClickListener(onClickListener: (View) -> Unit) {
    setOnClickListener { view ->
        performOnTapHapticFeedback()
        onClickListener(view)
    }
}

fun View.performOnTapHapticFeedback() = performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)

fun View.updatePadding(
    @Dimension start: Int? = null,
    @Dimension top: Int? = null,
    @Dimension end: Int? = null,
    @Dimension bottom: Int? = null
) = setPaddingRelative(
    start ?: paddingStart,
    top ?: paddingTop,
    end ?: paddingEnd,
    bottom ?: paddingBottom
)

fun View.updateMargin(
    @Dimension start: Int? = null,
    @Dimension top: Int? = null,
    @Dimension end: Int? = null,
    @Dimension bottom: Int? = null
) {
    val prevLayoutParams = layoutParams as? ViewGroup.MarginLayoutParams
    when (parent) {
        is ConstraintLayout -> ConstraintLayout.LayoutParams(
            layoutParams.width,
            layoutParams.height
        )
        is RelativeLayout -> RelativeLayout.LayoutParams(layoutParams.width, layoutParams.height)
        else -> ViewGroup.MarginLayoutParams(layoutParams.width, layoutParams.height)
    }.apply {
        (start ?: prevLayoutParams?.marginStart)?.let { marginStart = it }
        (top ?: prevLayoutParams?.topMargin)?.let { topMargin = it }
        (end ?: prevLayoutParams?.marginEnd)?.let { marginEnd = it }
        (bottom ?: prevLayoutParams?.bottomMargin)?.let { bottomMargin = it }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.setSize(
    @Dimension width: Int? = null,
    @Dimension height: Int? = null
) {
    layoutParams = T::class.java
        .getConstructor(Int::class.java, Int::class.java)
        .newInstance(
            width ?: layoutParams.width,
            height ?: layoutParams.height
        )
}

fun View.setScaleXY(scale: Float) {
    scaleX = scale
    scaleY = scale
}

fun View.setupLargeTitle(
    title: String,
    @FontRes font: Int,
    activity: AppCompatActivity,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    activity.setSupportActionBar(toolbar)
    toolbar.title = null // Always remove the underlying toolbar title
    collapsingToolbar.title = title
    val resolvedFont = activity.compatFont(font)
    collapsingToolbar.setExpandedTitleTypeface(resolvedFont)
    collapsingToolbar.setCollapsedTitleTypeface(resolvedFont)

    backgroundColor?.let { color ->
        toolbar.setBackgroundColor(color)
        collapsingToolbar.setBackgroundColor(color)
        whenApiVersion(Build.VERSION_CODES.M) {
            val flags = activity.window.decorView.systemUiVisibility
            activity.window.decorView.systemUiVisibility = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.statusBarColor = backgroundColor
        }
    }

    icon?.let { toolbar.setNavigationIcon(it) }
    backAction?.let { toolbar.setNavigationOnClickListener { it() } }
}

fun View.fadeIn(endAction: (() -> Unit)? = null) {
    alpha = 0f
    show()
    val animation = animate().setDuration(225).alpha(1f)
    endAction?.let { animation.withEndAction(it) }
    animation.start()
}

fun View.fadeOut(endAction: (() -> Unit)? = null, removeOnEnd: Boolean = true) {
    alpha = 1f
    show()
    val animation = animate().setDuration(225).alpha(0f)
    animation.withEndAction {
        if (removeOnEnd) {
            this.remove()
        }
        endAction?.invoke()
    }
    animation.start()
}
