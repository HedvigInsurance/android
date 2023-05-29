package com.hedvig.app.feature.claimdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.getLocale
import com.hedvig.app.feature.claimdetail.ui.ClaimDetailScreen
import com.hedvig.app.feature.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.startChat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ClaimDetailActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    window.compatSetDecorFitsSystemWindows(false)

    val claimId = intent.getStringExtra(CLAIM_ID)
      ?: error("Programmer error: Missing claimId in ${this.javaClass.name}")
    val viewModel: ClaimDetailViewModel by viewModel { parametersOf(claimId) }

    setContent {
      val viewState by viewModel.viewState.collectAsStateWithLifecycle()

      HedvigTheme {
        ClaimDetailScreen(
          viewState = viewState,
          locale = getLocale(),
          retry = viewModel::retry,
          onUpClick = ::finish,
          onChatClick = {
            viewModel.onChatClick()
            startChat()
          },
          onPlayClick = viewModel::onPlayClick,
        )
      }
    }
  }

  companion object {
    private const val CLAIM_ID = "com.hedvig.app.feature.claimdetail.CLAIM_ID"
    fun newInstance(
      context: Context,
      claimId: String,
    ) = Intent(context, ClaimDetailActivity::class.java).apply {
      putExtra(CLAIM_ID, claimId)
    }
  }
}
