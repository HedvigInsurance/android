package com.hedvig.android.feature.product.inapp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.TopAppBarActionType

internal data class CrossSellProductHeader(
  val title: String,
  val subtitle: String,
  val pillowImageSrc: String,
)

@Composable
internal fun CrossSellProductDestination(
  viewModel: CrossSellProductViewModel,
  header: CrossSellProductHeader,
  close: () -> Unit,
  openStoreUrl: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  CrossSellProductScreen(
    uiState = uiState,
    header = header,
    close = close,
    retry = { viewModel.emit(CrossSellProductEvent.Retry) },
    openStoreUrl = openStoreUrl,
  )
}

// TODO: Replace with the Figma design. This only exercises the data layer end to end.
@Composable
private fun CrossSellProductScreen(
  uiState: CrossSellProductUiState,
  header: CrossSellProductHeader,
  close: () -> Unit,
  retry: () -> Unit,
  openStoreUrl: () -> Unit,
) {
  HedvigScaffold(
    navigateUp = close,
    topAppBarText = header.title,
    topAppBarActionType = TopAppBarActionType.CLOSE,
  ) {
    when (uiState) {
      CrossSellProductUiState.Loading -> {
        HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
      }

      CrossSellProductUiState.Failure -> {
        HedvigErrorSection(onButtonClick = retry, modifier = Modifier.weight(1f))
      }

      is CrossSellProductUiState.Content -> {
        Column(
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        ) {
          HedvigText(header.subtitle, color = HedvigTheme.colorScheme.textSecondary)
          uiState.productInfo.heroIntro?.let { intro ->
            Column {
              HedvigText(intro.title)
              HedvigText(intro.description, color = HedvigTheme.colorScheme.textSecondary)
            }
          }
          for (usp in uiState.productInfo.usps) {
            Column {
              HedvigText(usp.title)
              HedvigText(usp.description, color = HedvigTheme.colorScheme.textSecondary)
            }
          }
          for (item in uiState.productInfo.carousel) {
            HedvigText(item.title)
          }
        }
      }
    }
    Box(Modifier.padding(16.dp)) {
      // TODO: Add "Discover your offer" / "Upptäck ditt erbjudande" to Lokalise
      HedvigButton(
        text = "Discover your offer",
        onClick = openStoreUrl,
        enabled = true,
        modifier = Modifier.fillMaxWidth(),
      )
    }
  }
}
