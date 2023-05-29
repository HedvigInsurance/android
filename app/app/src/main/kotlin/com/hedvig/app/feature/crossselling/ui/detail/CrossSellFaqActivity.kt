package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.faq.FAQBottomSheet
import com.hedvig.app.util.extensions.startChat

class CrossSellFaqActivity : AppCompatActivity() {

  private val crossSell by lazy {
    intent.parcelableExtra<CrossSellData>(CROSS_SELL)
      ?: error("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    setContent {
      HedvigTheme {
        FaqScreen(
          onUpClick = ::finish,
          openSheet = { faq ->
            FAQBottomSheet
              .newInstance(faq)
              .show(supportFragmentManager, FAQBottomSheet.TAG)
          },
          openChat = { startChat() },
          items = crossSell.faq,
        )
      }
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
