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
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.extensions.showErrorDialog
import com.hedvig.app.util.extensions.startChat
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CrossSellFaqActivity : BaseActivity() {
    private val model: CrossSellFaqViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crossSell = intent.getParcelableExtra<CrossSellData>(CROSS_SELL)
            ?: throw IllegalArgumentException("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")

        model.events
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    CrossSellFaqViewModel.Event.Error -> showErrorDialog(getString(R.string.component_error)) {}
                    CrossSellFaqViewModel.Event.StartChat -> startChat()
                }
            }
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
                    onCtaClick = {
                        handleAction(
                            context = this,
                            action = crossSell.action
                        )
                    },
                    items = crossSell.faq,
                )
            }
        }
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
    private val chatRepository: ChatRepository,
) : ViewModel() {

    sealed class Event {
        object StartChat : Event()
        object Error : Event()
    }

    private val _events = Channel<Event>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    suspend fun triggerOpenChat() {
        viewModelScope.launch {
            val event = when (chatRepository.triggerFreeTextChat()) {
                is Either.Left -> Event.Error
                is Either.Right -> Event.StartChat
            }
            _events.trySend(event)
        }
    }
}
