package com.hedvig.app.feature.claimdetail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.LanguageService
import com.hedvig.app.feature.claimdetail.ui.ClaimDetailScreen
import com.hedvig.app.feature.claimdetail.ui.ClaimDetailViewModel
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startChat
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ClaimDetailActivity : AppCompatActivity() {
  private val languageService: LanguageService by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    window.compatSetDecorFitsSystemWindows(false)

    val claimId = intent.getStringExtra(CLAIM_ID)
      ?: throw IllegalArgumentException("Programmer error: Missing claimId in ${this.javaClass.name}")
    val viewModel: ClaimDetailViewModel by viewModel { parametersOf(claimId) }

    viewModel
      .events
      .flowWithLifecycle(lifecycle)
      .onEach { event ->
        when (event) {
          ClaimDetailViewModel.Event.StartChat -> startChat()
          ClaimDetailViewModel.Event.Error -> showErrorDialog(
            getString(hedvig.resources.R.string.NETWORK_ERROR_ALERT_MESSAGE),
          ) {}
        }
      }
      .launchIn(lifecycleScope)

    val locale = languageService.getLocale()
    setContent {
      val viewState by viewModel.viewState.collectAsState()

      HedvigTheme {
        ClaimDetailScreen(
          viewState = viewState,
          locale = locale,
          retry = viewModel::retry,
          onUpClick = ::finish,
          onChatClick = viewModel::onChatClick,
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
