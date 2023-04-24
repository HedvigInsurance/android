package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.util.extensions.openWebBrowser
import com.hedvig.app.util.extensions.showErrorDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CrossSellFaqActivity : AppCompatActivity() {

  private val crossSell by lazy {
    intent.parcelableExtra<CrossSellData>(CROSS_SELL)
      ?: error("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")
  }

  private val viewModel: CrossSellFaqViewModel by viewModel { parametersOf(crossSell) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    viewModel.viewState
      .flowWithLifecycle(lifecycle)
      .onEach(::handleViewState)
      .launchIn(lifecycleScope)

    setContent {
      HedvigTheme {
        FaqScreen(
          onUpClick = ::finish,
          openSheet = { faq ->
            FAQBottomSheet
              .newInstance(faq)
              .show(supportFragmentManager, FAQBottomSheet.TAG)
          },
          openChat = ::openChat,
          onCtaClick = { viewModel.onCtaClick() },
          items = crossSell.faq,
        )
      }
    }
  }

  private fun handleViewState(viewState: CrossSellFaqViewModel.ViewState) = with(viewState) {
    errorMessage?.let {
      showErrorDialog(getString(com.adyen.checkout.dropin.R.string.component_error)) {
        viewModel.dismissError()
      }
    }

    navigateChat
      ?.navigate(this@CrossSellFaqActivity)
      ?.also { viewModel.actionOpened() }

    navigateEmbark
      ?.navigate(this@CrossSellFaqActivity)
      ?.also { viewModel.actionOpened() }

    navigateWeb
      ?.let(::openWebBrowser)
      ?.also { viewModel.actionOpened() }
  }

  private fun openChat() {
    lifecycleScope.launch {
      viewModel.triggerOpenChat()
    }
  }

  companion object {
    private const val CROSS_SELL = "CROSS_SELL"
    fun newInstance(
      context: Context,
      crossSell: CrossSellData,
    ) = Intent(context, CrossSellFaqActivity::class.java).apply {
      putExtra(CROSS_SELL, crossSell)
    }
  }
}

class CrossSellFaqViewModel(
  private val crossSell: CrossSellData,
  private val chatRepository: ChatRepository,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  data class ViewState(
    val navigateEmbark: NavigateEmbark? = null,
    val navigateChat: NavigateChat? = null,
    val navigateWeb: Uri? = null,
    val errorMessage: String? = null,
    val loading: Boolean = false,
  )

  suspend fun triggerOpenChat() {
    viewModelScope.launch {
      val viewState = when (chatRepository.triggerFreeTextChat()) {
        is Either.Left -> ViewState(errorMessage = null)
        is Either.Right -> ViewState(navigateChat = NavigateChat)
      }
      _viewState.value = viewState
    }
  }

  fun onCtaClick() {
    viewModelScope.launch {
      Uri.parse(crossSell.storeUrl)
    }
  }

  fun actionOpened() {
    _viewState.value = ViewState()
  }

  fun dismissError() {
    _viewState.update {
      it.copy(errorMessage = null)
    }
  }
}
