package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.hedvig.app.BaseActivity
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.ui.compose.theme.HedvigTheme
import com.hedvig.app.util.coroutines.runSuspendCatching
import com.hedvig.app.util.extensions.startChat
import e
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CrossSellFaqActivity : BaseActivity() {
    private val model: CrossSellFaqViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crossSell = intent.getParcelableExtra<CrossSellData>(CROSS_SELL)
            ?: throw IllegalArgumentException("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")

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
            startChat()
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
    suspend fun triggerOpenChat() {
        val result = runSuspendCatching {
            chatRepository.triggerFreeTextChat()
        }

        result.exceptionOrNull()?.let { e(it) }
    }
}
