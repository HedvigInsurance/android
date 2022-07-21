package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.designsystem.theme.HedvigTheme
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.util.extensions.showErrorDialog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class CrossSellFaqActivity : BaseActivity() {

  val crossSell by lazy {
    intent.getParcelableExtra<CrossSellData>(CROSS_SELL)
      ?: throw IllegalArgumentException("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")
  }

  private val model: CrossSellFaqViewModel by viewModel { parametersOf(crossSell) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    model.viewState
      .flowWithLifecycle(lifecycle)
      .onEach(::handleViewState)
      .launchIn(lifecycleScope)

    setContent {
      HedvigTheme {
        FaqScreen(
          ctaLabel = crossSell.callToAction,
          onUpClick = ::finish,
          openSheet = { faq ->
            FAQBottomSheet
              .newInstance(faq)
              .show(supportFragmentManager, FAQBottomSheet.TAG)
          },
          openChat = ::openChat,
          onCtaClick = { model.onCtaClick() },
          items = crossSell.faq,
        )
      }
    }
  }

  private fun handleViewState(viewState: CrossSellFaqViewModel.ViewState) = with(viewState) {
    errorMessage?.let {
      showErrorDialog(getString(R.string.component_error)) {
        model.dismissError()
      }
    }

    navigateChat
      ?.navigate(this@CrossSellFaqActivity)
      ?.also { model.actionOpened() }

    navigateEmbark
      ?.navigate(this@CrossSellFaqActivity)
      ?.also { model.actionOpened() }
  }

  private fun openChat() {
    lifecycleScope.launch {
      model.triggerOpenChat()
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
  private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {

  private val _viewState = MutableStateFlow(ViewState())
  val viewState = _viewState.asStateFlow()

  data class ViewState(
    val navigateEmbark: NavigateEmbark? = null,
    val navigateChat: NavigateChat? = null,
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
      when (val action = crossSell.action) {
        CrossSellData.Action.Chat -> _viewState.value = ViewState(navigateChat = NavigateChat)
        is CrossSellData.Action.Embark -> _viewState.value = action.toViewState()
      }
    }
  }

  private suspend fun CrossSellData.Action.Embark.toViewState(): ViewState {
    return when (val result = createQuoteCartUseCase.invoke()) {
      is Either.Left -> ViewState(errorMessage = result.value.message)
      is Either.Right -> {
        val embarkStoryId = appendQuoteCartId(embarkStoryId, result.value.id)
        val navigateEmbark = NavigateEmbark(embarkStoryId, title)
        ViewState(navigateEmbark = navigateEmbark)
      }
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
