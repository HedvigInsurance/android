package com.hedvig.app.util.extensions.view

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInFragmentViewModel
import com.hedvig.app.util.extensions.compatColor
import com.hedvig.app.util.extensions.compatDrawable
import com.hedvig.app.util.extensions.fontAttr
import com.hedvig.app.util.extensions.isDarkThemeActive
import com.hedvig.app.util.whenApiVersion
import dev.chrisbanes.insetter.doOnApplyWindowInsets
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
fun View.performOnLongPressHapticFeedback() =
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

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
    start: Int? = null,
    top: Int? = null,
    end: Int? = null,
    bottom: Int? = null
) {
    val lp = layoutParams as? ViewGroup.MarginLayoutParams
        ?: return

    lp.setMargins(
        start ?: lp.marginStart,
        top ?: lp.topMargin,
        end ?: lp.marginEnd,
        bottom ?: lp.bottomMargin
    )

    layoutParams = lp
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

fun Toolbar.setupToolbar(
    activity: AppCompatActivity,
    usingEdgeToEdge: Boolean = false,
    @DrawableRes icon: Int? = null,
    rootLayout: View?,
    backAction: (() -> Unit)? = null
) {
    activity.setSupportActionBar(this)
    activity.supportActionBar?.setDisplayShowTitleEnabled(false)
    icon?.let { icon ->
        this.navigationIcon = this.context.getDrawable(icon)
    }
    backAction?.let {
        this.setNavigationOnClickListener { it() }
    }
    if (usingEdgeToEdge) {
        this.doOnApplyWindowInsets { view, insets, initialState ->
            view.updatePadding(top = initialState.paddings.top + insets.systemWindowInsetTop)
        }
        rootLayout?.let { root ->
            root.doOnApplyWindowInsets { view, insets, initialState ->
                view.updatePadding(
                    top = initialState.paddings.top + this.measuredHeight,
                    bottom = initialState.paddings.bottom + insets.systemWindowInsetBottom
                )
            }

            if (root is NestedScrollView) {
                root.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
                    val dy = oldScrollY - scrollY
                    val toolbarHeight = this.height.toFloat()
                    val offset = root.computeVerticalScrollOffset().toFloat()
                    val percentage = if (offset < toolbarHeight) {
                        offset / toolbarHeight
                    } else {
                        1f
                    }
                    if (dy < 0) {
                        // Scroll up
                        this.elevation = percentage * 10
                    } else {
                        // scroll down
                        this.elevation = percentage * 10
                    }

                }
            } else if (root is RecyclerView) {
                val toolbar = this
                root.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val toolbarHeight = toolbar.height.toFloat()
                        val offset = root.computeVerticalScrollOffset().toFloat()
                        val percentage = if (offset < toolbarHeight) {
                            offset / toolbarHeight
                        } else {
                            1f
                        }
                        if (dy < 0) {
                            // Scroll up
                            toolbar.elevation = percentage * 10
                        } else {
                            // scroll down
                            toolbar.elevation = percentage * 10
                        }
                    }
                })
            }
        }
    }
}

fun View.setupLargeTitle(
    title: String,
    activity: AppCompatActivity,
    @DrawableRes icon: Int? = null,
    @ColorInt backgroundColor: Int? = null,
    backAction: (() -> Unit)? = null
) {
    activity.setSupportActionBar(hedvigToolbar)
    collapsingToolbar.title = title

    val font = context.fontAttr(android.R.attr.fontFamily)
    font?.let { f ->
        collapsingToolbar.setExpandedTitleTypeface(f)
        collapsingToolbar.setCollapsedTitleTypeface(f)
    }

    backgroundColor?.let { color ->
        hedvigToolbar.setBackgroundColor(color)
        collapsingToolbar.setBackgroundColor(color)
        whenApiVersion(Build.VERSION_CODES.M) {
            val flags = activity.window.decorView.systemUiVisibility
            activity.window.decorView.systemUiVisibility =
                flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            activity.window.statusBarColor = backgroundColor
        }
    }

    icon?.let {
        hedvigToolbar.navigationIcon = hedvigToolbar.context.compatDrawable(it)?.apply {
            if (context.isDarkThemeActive) {
                setTint(
                    context.compatColor(
                        R.color.icon_tint
                    )
                )
            }
        }
    }
    backAction?.let { hedvigToolbar.setNavigationOnClickListener { it() } }
}

fun NestedScrollView.setupToolbarScrollListener(
    loggedInFragmentViewModel: LoggedInFragmentViewModel
) {
    this.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
        val dy = oldScrollY - scrollY
        val maxElevationScroll = 200
        val offset = this.computeVerticalScrollOffset().toFloat()
        val percentage = if (offset < maxElevationScroll) {
            offset / maxElevationScroll
        } else {
            1f
        }
        loggedInFragmentViewModel.scroll.postValue(percentage * 10)
    }
}

fun RecyclerView.setupToolbarScrollListener(loggedInFragmentViewModel: LoggedInFragmentViewModel) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val maxElevationScroll = 200
            val offset = this@setupToolbarScrollListener.computeVerticalScrollOffset().toFloat()
            val percentage = if (offset < maxElevationScroll) {
                offset / maxElevationScroll
            } else {
                1f
            }
            loggedInFragmentViewModel.scroll.postValue(percentage * 10)
        }
    })
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

fun View.dismissKeyboard() =
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
        windowToken,
        0
    )

fun View.openKeyboard() =
    (context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
        this,
        0
    )

val View.centerX: Int
    get() = (x + width / 2).toInt()

val View.centerY: Int
    get() = (y + height / 2).toInt()
