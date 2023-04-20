package com.hedvig.app.feature.crossselling.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.util.extensions.startChat
import org.koin.android.ext.android.inject
import java.time.Clock
import java.time.format.DateTimeFormatter

class CrossSellingResultActivity : AppCompatActivity() {

  private val clock: Clock by inject()
  private val crossSellingResult: CrossSellingResult
    get() = intent.parcelableExtra(CROSS_SELLING_RESULT)
      ?: error("Programmer error: CROSS_SELLING_RESULT not provided to ${this.javaClass.name}")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    onBackPressedDispatcher.addCallback(this) {
      startActivity(
        LoggedInActivity.newInstance(
          context = this@CrossSellingResultActivity,
          withoutHistory = true,
          initialTab = LoggedInTabs.INSURANCE,
        ),
      )
    }

    setContent {
      CrossSellingResultScreen(
        crossSellingResult = crossSellingResult,
        clock = clock,
        dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE,
        openChat = { startChat() },
        closeResultScreen = { onBackPressedDispatcher.onBackPressed() },
      )
    }
  }

  companion object {
    fun newInstance(context: Context, crossSellingResult: CrossSellingResult): Intent =
      Intent(context, CrossSellingResultActivity::class.java).apply {
        putExtra(CROSS_SELLING_RESULT, crossSellingResult)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
      }

    private const val CROSS_SELLING_RESULT = "CROSS_SELLING_RESULT"
  }
}
