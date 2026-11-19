package com.hedvig.android.app

import android.app.Activity
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.UriHandler
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.app.urihandler.SafeAndroidUriHandler
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * The [AppHost] of [MainActivity]. [splashIsRemovedSignal] fires when the splash screen is gone, so the
 * system bars are styled again against the app's own background rather than the splash's.
 */
internal class AndroidAppHostImpl(
  private val activity: ComponentActivity,
  private val splashIsRemovedSignal: Channel<Unit>,
) : AppHost {
  override val uriHandler: UriHandler = SafeAndroidUriHandler(activity)

  fun finishApp() = activity.finish()

  override fun shouldShowPermissionRationale(permission: String): Boolean =
    activity.shouldShowRequestPermissionRationale(permission)

  override fun tryShowAppStoreReviewDialog() = activity.tryShowPlayStoreReviewDialog()

  @Composable
  override fun SystemBarsEffect(darkTheme: Boolean) {
    val splashIsRemovedIndex by produceState(0) {
      splashIsRemovedSignal.receiveAsFlow().collectLatest { value = value + 1 }
    }
    DisposableEffect(darkTheme, splashIsRemovedIndex) {
      val systemBarStyle = when (darkTheme) {
        true -> SystemBarStyle.dark(Color.TRANSPARENT)
        false -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
      }
      activity.enableEdgeToEdge(
        statusBarStyle = systemBarStyle,
        navigationBarStyle = systemBarStyle,
      )
      onDispose {}
    }
  }
}

private fun Activity.tryShowPlayStoreReviewDialog() {
  val tag = "PlayStoreReview"
  val manager = ReviewManagerFactory.create(this)
  logcat(LogPriority.INFO) { "$tag: requestReviewFlow" }
  manager.requestReviewFlow().apply {
    addOnFailureListener { logcat(LogPriority.INFO, it) { "$tag: requestReviewFlow failed:${it.message}" } }
    addOnCanceledListener { logcat(LogPriority.INFO) { "$tag: requestReviewFlow cancelled" } }
    addOnCompleteListener { task ->
      if (task.isSuccessful) {
        logcat(LogPriority.INFO) { "$tag: requestReviewFlow completed" }
        val reviewInfo = task.result
        logcat(LogPriority.INFO) { "$tag: launchReviewFlow with ReviewInfo:$reviewInfo" }
        manager.launchReviewFlow(this@tryShowPlayStoreReviewDialog, reviewInfo).apply {
          addOnFailureListener { logcat(LogPriority.INFO, it) { "$tag: launchReviewFlow failed:${it.message}" } }
          addOnCanceledListener { logcat(LogPriority.INFO) { "$tag: launchReviewFlow canceled" } }
          addOnCompleteListener { logcat(LogPriority.INFO) { "$tag: launchReviewFlow completed" } }
        }
      } else {
        val exception = task.exception
        val errorMessage = if (exception != null && exception is ReviewException) {
          "ReviewException:${exception.message}. ReviewException::errorCode:${exception.errorCode}"
        } else {
          "Unknown error with message: ${exception?.message}"
        }
        logcat(LogPriority.INFO, exception) { "$tag: requestReviewFlow failed. Error:$errorMessage" }
      }
    }
  }
}
