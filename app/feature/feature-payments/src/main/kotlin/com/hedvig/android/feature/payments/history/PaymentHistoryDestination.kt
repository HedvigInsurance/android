package com.hedvig.android.feature.payments.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.hedvig.android.core.designsystem.component.information.HedvigInformationSection
import com.hedvig.android.core.designsystem.component.progress.HedvigFullScreenCenterAlignedProgressDebounced
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.m3.TopAppBar
import com.hedvig.android.core.ui.appbar.m3.TopAppBarActionType
import com.hedvig.android.core.ui.getLocale
import com.hedvig.android.core.ui.hedvigSecondaryDateTimeFormatter
import com.hedvig.android.core.ui.plus
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.ChargeHistory
import hedvig.resources.R
import kotlin.time.Duration.Companion.days
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime
import octopus.type.CurrencyCode

@Composable
internal fun PaymentHistoryDestination(
  viewModel: PaymentHistoryViewModel,
  onNavigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  PaymentHistoryScreen(
    uiState = uiState,
    navigateUp = onNavigateUp,
    onNavigateBack = onNavigateBack,
    onRetry = { viewModel.emit(PaymentHistoryEvent.Retry) },
  )
}

@Composable
private fun PaymentHistoryScreen(
  uiState: PaymentHistoryUiState,
  navigateUp: () -> Unit,
  onNavigateBack: () -> Unit,
  onRetry: () -> Unit,
) {
  val locale = getLocale()
  val dateTimeFormatter = remember(locale) { hedvigSecondaryDateTimeFormatter(locale) }
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
        when (uiState) {
          PaymentHistoryUiState.Loading -> HedvigFullScreenCenterAlignedProgressDebounced()
          is PaymentHistoryUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              HedvigErrorSection(retry = onRetry)
            }
          }

          is PaymentHistoryUiState.NoChargeHistory -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              HedvigInformationSection(
                title = stringResource(R.string.PAYMENTS_NO_HISTORY_DATA),
                buttonText = stringResource(R.string.general_back_button),
                onButtonClick = onNavigateBack,
              )
            }
          }

          is PaymentHistoryUiState.Content -> {
            LazyColumn(
              contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal).asPaddingValues() +
                WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom).asPaddingValues() +
                PaddingValues(horizontal = 16.dp),
              modifier = Modifier
                .fillMaxSize()
                .nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
            ) {
              itemsIndexed(
                items = uiState.chargeHistory.charges,
                contentType = { _, _ -> "Charge" },
              ) { index: Int, charge: ChargeHistory.Charge ->
                if (index != 0) {
                  Divider()
                }
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(56.dp)
                    .padding(vertical = 16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(charge.date.toJavaLocalDate().format(dateTimeFormatter))
                  Spacer(Modifier.width(16.dp))
                  Text(text = charge.amount.toString())
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
@HedvigPreview
private fun PreviewPaymentHistoryScreen(
  @PreviewParameter(PaymentHistoryUiStateCollectionPreviewProvider::class) uiState: PaymentHistoryUiState,
) {
  HedvigTheme {
    Surface {
      PaymentHistoryScreen(
        uiState = uiState,
        navigateUp = {},
        onRetry = {},
        onNavigateBack = {},
      )
    }
  }
}

private class PaymentHistoryUiStateCollectionPreviewProvider :
  CollectionPreviewParameterProvider<PaymentHistoryUiState>(
    listOf(
      PaymentHistoryUiState.Content(
        chargeHistory = ChargeHistory(
          charges = listOf(
            ChargeHistory.Charge(
              amount = UiMoney(350.0, CurrencyCode.SEK),
              date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
              paymentStatus = ChargeHistory.Charge.PaymentStatus.PENDING,
            ),
            ChargeHistory.Charge(
              amount = UiMoney(250.0, CurrencyCode.SEK),
              date = Clock.System.now().minus(1.days).toLocalDateTime(TimeZone.currentSystemDefault()).date,
              paymentStatus = ChargeHistory.Charge.PaymentStatus.FAILED,
            ),
            ChargeHistory.Charge(
              amount = UiMoney(300.0, CurrencyCode.SEK),
              date = Clock.System.now().minus(2.days).toLocalDateTime(TimeZone.currentSystemDefault()).date,
              paymentStatus = ChargeHistory.Charge.PaymentStatus.SUCCESSFUL,
            ),
          ),
        ),
        isLoading = false,
      ),
      PaymentHistoryUiState.Error("Error message"),
      PaymentHistoryUiState.Loading,
    ),
  )
