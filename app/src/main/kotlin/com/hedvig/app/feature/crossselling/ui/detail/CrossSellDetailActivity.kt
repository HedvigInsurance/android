package com.hedvig.app.feature.crossselling.ui.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import coil.ImageLoader
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.common.android.parcelableExtra
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.offer.quotedetail.QuoteDetailActivity
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.util.extensions.compatSetDecorFitsSystemWindows
import com.hedvig.app.util.extensions.openWebBrowser
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.core.parameter.parametersOf

class CrossSellDetailActivity : AppCompatActivity() {

  private val crossSell: CrossSellData
    get() = intent.parcelableExtra(CROSS_SELL)
      ?: error("Programmer error: CROSS_SELL not passed to ${this.javaClass.name}")

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())

    val viewModel = getViewModel<CrossSellDetailViewModel> {
      parametersOf(crossSell)
    }
    val imageLoader: ImageLoader = get()

    window.compatSetDecorFitsSystemWindows(false)

    setContent {
      val viewState by viewModel.viewState.collectAsState()
      LaunchedEffect(viewState) {
        viewState.navigateChat
          ?.navigate(this@CrossSellDetailActivity)
          ?.also { viewModel.actionOpened() }

        viewState.navigateEmbark
          ?.navigate(this@CrossSellDetailActivity)
          ?.also { viewModel.actionOpened() }

        viewState.navigateWeb
          ?.let(::openWebBrowser)
          ?.also { viewModel.actionOpened() }
      }

      HedvigTheme {
        CrossSellDetailScreen(
          onCtaClick = viewModel::onCtaClick,
          onUpClick = { finish() },
          onCoverageClick = { openCoverage(crossSell) },
          onFaqClick = { openFaq(crossSell) },
          onDismissError = viewModel::dismissError,
          data = crossSell,
          errorMessage = viewState.errorMessage,
          imageLoader = imageLoader,
        )
      }
    }
  }

  private fun openCoverage(crossSell: CrossSellData) {
    val perils = crossSell.perils.map { PerilItem.Peril(it) }
    startActivity(
      QuoteDetailActivity.newInstance(
        context = this,
        title = getString(hedvig.resources.R.string.cross_sell_info_full_coverage_row),
        perils = perils,
        insurableLimits = crossSell.insurableLimits,
        documents = crossSell.terms,
      ),
    )
  }

  private fun openFaq(crossSell: CrossSellData) {
    startActivity(CrossSellFaqActivity.newInstance(this, crossSell))
  }

  companion object {
    private const val CROSS_SELL = "CROSS_SELL"
    fun newInstance(
      context: Context,
      crossSell: CrossSellData,
    ) = Intent(
      context,
      CrossSellDetailActivity::class.java,
    ).apply {
      putExtra(CROSS_SELL, crossSell)
    }
  }
}
