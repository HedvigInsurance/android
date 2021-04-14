package com.hedvig.app.util.extensions.view

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.annotation.Dimension
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.util.ControlFocusInsetsAnimationCallback
import com.hedvig.app.util.RootViewDeferringInsetsCallback
import com.hedvig.app.util.TranslateDeferringInsetsAnimationCallback
import com.hedvig.app.util.extensions.compatDrawable
import dev.chrisbanes.insetter.doOnApplyWindowInsets
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

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
    @Dimension bottom: Int? = null,
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
    bottom: Int? = null,
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
    @Dimension height: Int? = null,
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
    backAction: (() -> Unit)? = null,
) {
    activity.setSupportActionBar(this)
    activity.supportActionBar?.setDisplayShowTitleEnabled(false)
    icon?.let { ic ->
        this.navigationIcon = this.context.compatDrawable(ic)
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

fun NestedScrollView.setupToolbarScrollListener(
    toolbar: Toolbar,
) {
    setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
        val maxElevationScroll = 200
        val offset = this.computeVerticalScrollOffset().toFloat()
        val percentage = if (offset < maxElevationScroll) {
            offset / maxElevationScroll
        } else {
            1f
        }
        toolbar.elevation = percentage * 10
    }
}

fun RecyclerView.setupToolbarScrollListener(onScroll: (Float) -> Unit) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val maxElevationScroll = 200
            val offset = computeVerticalScrollOffset().toFloat()
            val percentage = if (offset < maxElevationScroll) {
                offset / maxElevationScroll
            } else {
                1f
            }
            onScroll(percentage)
        }
    })
}

fun RecyclerView.setupToolbarScrollListener(toolbar: Toolbar) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val maxElevationScroll = 200
            val offset = computeVerticalScrollOffset().toFloat()
            val percentage = if (offset < maxElevationScroll) {
                offset / maxElevationScroll
            } else {
                1f
            }
            toolbar.elevation = percentage * 10
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

fun View.hapticClicks(): Flow<Unit> = callbackFlow {
    setOnClickListener {
        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        runCatching { offer(Unit) }.getOrDefault(false)
    }
    awaitClose { setOnClickListener(null) }
}.conflate()

@RequiresApi(Build.VERSION_CODES.R)
fun View.setupInsetsForIme(root: View, vararg translatableViews: View) {
    val deferringListener = RootViewDeferringInsetsCallback(
        persistentInsetTypes = WindowInsets.Type.systemBars(),
        deferredInsetTypes = WindowInsets.Type.ime(),
        setPaddingTop = false
    )

    root.setWindowInsetsAnimationCallback(deferringListener)
    root.setOnApplyWindowInsetsListener(deferringListener)

    translatableViews.forEach {
        it.setWindowInsetsAnimationCallback(
            TranslateDeferringInsetsAnimationCallback(
                view = it,
                persistentInsetTypes = WindowInsets.Type.systemBars(),
                deferredInsetTypes = WindowInsets.Type.ime(),
                dispatchMode = WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE
            )
        )
    }

    setWindowInsetsAnimationCallback(
        ControlFocusInsetsAnimationCallback(this)
    )
}
