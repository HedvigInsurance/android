package com.hedvig.android.app

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * The Activity-bound capabilities the Compose app shell needs from its host. Implemented by
 * [MainActivity] and handed to the shell as a single dependency instead of four loose callbacks.
 * Mirrors the [com.hedvig.android.navigation.activity.ExternalNavigator] pattern.
 */
internal interface AndroidAppHost {
  fun finishApp()

  fun applyEdgeToEdgeStyle(systemBarStyle: SystemBarStyle)

  fun shouldShowPermissionRationale(permission: String): Boolean

  fun tryShowAppStoreReviewDialog()
}

internal class AndroidAppHostImpl(private val activity: ComponentActivity) : AndroidAppHost {
  override fun finishApp() = activity.finish()

  override fun applyEdgeToEdgeStyle(systemBarStyle: SystemBarStyle) {
    activity.enableEdgeToEdge(
      statusBarStyle = systemBarStyle,
      navigationBarStyle = systemBarStyle,
    )
  }

  override fun shouldShowPermissionRationale(permission: String): Boolean =
    activity.shouldShowRequestPermissionRationale(permission)

  override fun tryShowAppStoreReviewDialog() = activity.tryShowPlayStoreReviewDialog()
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
