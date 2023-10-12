package com.hedvig.app.util.extensions.view

import android.graphics.Rect
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.TouchDelegate
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsAnimation
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
import dev.chrisbanes.insetter.applyInsetter
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

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
  @Dimension bottom: Int? = null,
) = setPaddingRelative(
  start ?: paddingStart,
  top ?: paddingTop,
  end ?: paddingEnd,
  bottom ?: paddingBottom,
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
    bottom ?: lp.bottomMargin,
  )

  layoutParams = lp
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
    applyStatusBarInsets()
    rootLayout?.let { root ->
      root.applyStatusBarInsets()
      root.applyNavigationBarInsets()

      if (root is NestedScrollView) {
        root.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
          val dy = oldScrollY - scrollY
          val toolbarHeight = this.height.toFloat()

          @Suppress("RestrictedApi")
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
        root.addOnScrollListener(
          object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
              super.onScrolled(recyclerView, dx, dy)
              val toolbarHeight = toolbar.height.toFloat()

              @Suppress("RestrictedApi")
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
          },
        )
      }
    }
  }
}

fun RecyclerView.setupToolbarScrollListener(toolbar: Toolbar) {
  addOnScrollListener(
    object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val maxElevationScroll = 200

        @Suppress("RestrictedApi")
        val offset = computeVerticalScrollOffset().toFloat()
        val percentage = if (offset < maxElevationScroll) {
          offset / maxElevationScroll
        } else {
          1f
        }
        toolbar.elevation = percentage * 10
      }
    },
  )
}

fun View.hapticClicks(): Flow<Unit> = callbackFlow<Unit> {
  setOnClickListener {
    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    runCatching { trySend(Unit) }.getOrDefault(false)
  }
  awaitClose { setOnClickListener(null) }
}.conflate()

@RequiresApi(Build.VERSION_CODES.R)
fun View.setupInsetsForIme(root: View, vararg translatableViews: View) {
  val deferringListener = RootViewDeferringInsetsCallback(
    persistentInsetTypes = WindowInsets.Type.systemBars(),
    deferredInsetTypes = WindowInsets.Type.ime(),
    setPaddingTop = false,
  )

  root.setWindowInsetsAnimationCallback(deferringListener)
  root.setOnApplyWindowInsetsListener(deferringListener)

  translatableViews.forEach {
    it.setWindowInsetsAnimationCallback(
      TranslateDeferringInsetsAnimationCallback(
        view = it,
        persistentInsetTypes = WindowInsets.Type.systemBars(),
        deferredInsetTypes = WindowInsets.Type.ime(),
        dispatchMode = WindowInsetsAnimation.Callback.DISPATCH_MODE_CONTINUE_ON_SUBTREE,
      ),
    )
  }

  setWindowInsetsAnimationCallback(
    ControlFocusInsetsAnimationCallback(this),
  )
}

fun View.applyStatusBarInsets() = applyInsetter {
  type(statusBars = true) {
    padding()
  }
}

fun View.applyNavigationBarInsets() = applyInsetter {
  type(navigationBars = true) {
    padding()
  }
}

fun View.applyNavigationBarInsetsMargin() = applyInsetter {
  type(navigationBars = true) {
    margin()
  }
}

fun View.applyStatusBarAndNavigationBarInsets() = applyInsetter {
  type(statusBars = true, navigationBars = true) {
    padding()
  }
}
