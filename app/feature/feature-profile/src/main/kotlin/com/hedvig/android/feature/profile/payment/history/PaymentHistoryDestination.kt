package com.hedvig.android.feature.profile.payment.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.core.designsystem.animation.FadeAnimatedContent
import com.hedvig.android.core.designsystem.component.error.HedvigErrorSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBar
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.hedvigSecondaryDateTimeFormatter
import com.hedvig.android.core.ui.plus
import hedvig.resources.R
import java.time.LocalDate
import java.util.Locale

@Composable
internal fun PaymentHistoryDestination(viewModel: PaymentHistoryViewModel, onBackPressed: () -> Unit) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PaymentHistoryScreen(
    uiState = uiState,
    locale = getLocale(),
    navigateUp = onBackPressed,
  )
}

@Composable
private fun PaymentHistoryScreen(
  uiState: PaymentHistoryViewModel.PaymentHistoryUiState,
  locale: Locale,
  navigateUp: () -> Unit,
) {
  val topAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
  val topAppBarColors = TopAppBarDefaults.topAppBarColors(
    containerColor = MaterialTheme.colorScheme.background,
    scrolledContainerColor = MaterialTheme.colorScheme.surface,
  )
  Surface(color = MaterialTheme.colorScheme.background) {
    Column(Modifier.fillMaxSize()) {
      TopAppBar(
        title = stringResource(R.string.PAYMENTS_PAYMENT_HISTORY_BUTTON_LABEL),
        onClick = navigateUp,
        actionType = TopAppBarActionType.BACK,
        colors = topAppBarColors,
        scrollBehavior = topAppBarScrollBehavior,
      )
      FadeAnimatedContent(uiState) { uiState ->
        if (uiState.isLoading) {
          HedvigFullScreenCenterAlignedProgressDebounced()
        } else if (uiState.charges.isNotEmpty()) {
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Top),
            contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues() +
              WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues() +
              PaddingValues(horizontal = 16.dp),
            modifier = Modifier
              .fillMaxSize()
              .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
          ) {
            itemsIndexed(
              items = uiState.charges,
              contentType = { _, _ -> "Charge" },
            ) { index: Int, charge: PaymentHistoryViewModel.PaymentHistoryUiState.Payment ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(charge.date.format(hedvigSecondaryDateTimeFormatter(locale)))
                Text(
                  text = charge.amount.format(locale),
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              if (index != uiState.charges.lastIndex) {
                Divider()
              }
            }
          }
        } else {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            HedvigErrorSection(
              // todo string resource
              title = "No charge history",
              subTitle = null,
              // todo string resource
              buttonText = "Go back",
              retry = { navigateUp() },
            )
          }
        }
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewPaymentHistoryScreen(
  @PreviewParameter(PaymentHistoryUiStateCollectionPreviewProvider::class)
  uiState: PaymentHistoryViewModel.PaymentHistoryUiState,
) {
  HedvigTheme {
    Surface {
      PaymentHistoryScreen(
        uiState = uiState,
        locale = Locale.ENGLISH,
        navigateUp = {},
      )
    }
  }
}

private class PaymentHistoryUiStateCollectionPreviewProvider :
  CollectionPreviewParameterProvider<PaymentHistoryViewModel.PaymentHistoryUiState>(
    listOf(
      PaymentHistoryViewModel.PaymentHistoryUiState(
        charges = listOf(
          PaymentHistoryViewModel.PaymentHistoryUiState.Payment(
            amount = "350kr",
            date = LocalDate.now(),
          ),
          PaymentHistoryViewModel.PaymentHistoryUiState.Payment(
            amount = "250kr",
            date = LocalDate.now().minusDays(1),
          ),
          PaymentHistoryViewModel.PaymentHistoryUiState.Payment(
            amount = "300kr",
            date = LocalDate.now().minusDays(2),
          ),
        ),
        errorMessage = null,
        isLoading = false,
      ),
      PaymentHistoryViewModel.PaymentHistoryUiState(charges = emptyList(), errorMessage = "error", isLoading = false),
      PaymentHistoryViewModel.PaymentHistoryUiState(charges = emptyList(), errorMessage = null, isLoading = true),
    ),
  )
