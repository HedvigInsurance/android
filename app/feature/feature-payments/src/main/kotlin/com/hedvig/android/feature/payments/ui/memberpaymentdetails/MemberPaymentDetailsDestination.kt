package com.hedvig.android.feature.payments.ui.memberpaymentdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.design.system.hedvig.ButtonDefaults
import com.hedvig.android.design.system.hedvig.HedvigButton
import com.hedvig.android.design.system.hedvig.HedvigErrorSection
import com.hedvig.android.design.system.hedvig.HedvigFullScreenCenterAlignedProgress
import com.hedvig.android.design.system.hedvig.HedvigScaffold
import com.hedvig.android.design.system.hedvig.HedvigShortMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigText
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.HorizontalDivider
import com.hedvig.android.design.system.hedvig.HorizontalItemsWithMaximumSpaceTaken
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.datepicker.getLocale
import com.hedvig.android.design.system.hedvig.placeholder.hedvigPlaceholder
import com.hedvig.android.feature.payments.data.MemberPaymentsDetails
import com.hedvig.android.placeholder.PlaceholderHighlight
import com.hedvig.android.placeholder.shimmer
import hedvig.resources.R

@Composable
internal fun MemberPaymentDetailsDestination(
  viewModel: MemberPaymentDetailsViewModel,
  navigateUp: () -> Unit,
  onChangeBankAccount: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  MemberPaymentDetailsScreen(
    uiState,
    retry = {
      viewModel.emit(MemberPaymentDetailsEvent.Retry)
    },
    navigateUp,
    onChangeBankAccount
  )
}

@Composable
private fun MemberPaymentDetailsScreen(
  uiState: MemberPaymentDetailsUiState,
  retry: () -> Unit,
  navigateUp: () -> Unit,
  onChangeBankAccount: () -> Unit
) {
  HedvigScaffold(
    topAppBarText = stringResource(R.string.PAYMENTS_PAYMENT_DETAILS_INFO_TITLE),
    navigateUp = navigateUp,
  ) {
    when (uiState) {
      MemberPaymentDetailsUiState.Failure -> HedvigErrorSection(
        modifier = Modifier.weight(1f),
        onButtonClick = retry,
        buttonText = stringResource(R.string.GENERAL_RETRY),
      )

      MemberPaymentDetailsUiState.Loading -> HedvigFullScreenCenterAlignedProgress(Modifier.weight(1f))
      is MemberPaymentDetailsUiState.Success ->
        MemberPaymentDetailsSuccessScreen(
          uiState,
          onChangeBankAccount
        )
    }
  }
}

@Composable
private fun MemberPaymentDetailsSuccessScreen(
  uiState: MemberPaymentDetailsUiState.Success,
  onChangeBankAccount: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier.padding(horizontal = 16.dp)) {
    HorizontalItemsWithMaximumSpaceTaken(
      startSlot = {
        HedvigText(stringResource(id = R.string.PAYMENTS_PAYMENT_METHOD))
      },
      endSlot = {
        HedvigText(
          text = uiState.paymentDetails.paymentMethod,
          textAlign = TextAlign.End,
          modifier = Modifier.fillMaxWidth(),
          color = HedvigTheme.colorScheme.textSecondary,
        )
      },
      modifier = Modifier.padding(vertical = 16.dp),
      spaceBetween = 8.dp,
    )
    HorizontalDivider()

    val dayOfMonth = uiState.paymentDetails.chargingDayInTheMonth
    if (dayOfMonth != null) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(id = R.string.PAYMENTS_PAYMENT_DUE))
        },
        endSlot = {
          HedvigText(
            text = stringResource(
              R.string.PAYMENTS_DUE_DESCRIPTION,
              dayOfMonth.format(),
            ),
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        },
        modifier = Modifier.padding(vertical = 16.dp),
        spaceBetween = 8.dp,
      )
      HorizontalDivider()
    }
    if (uiState.paymentDetails.displayName != null) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = { HedvigText(stringResource(id = R.string.PAYMENTS_BANK_LABEL)) },
        endSlot = {
          HedvigText(
            text = uiState.paymentDetails.displayName,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        },
        spaceBetween = 8.dp,
        modifier = Modifier.padding(vertical = 16.dp),
      )
      HorizontalDivider()
    }
    if (uiState.paymentDetails.descriptor != null) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(id = R.string.PAYMENTS_ACCOUNT))
        },
        endSlot = {
          HedvigText(
            text = uiState.paymentDetails.descriptor,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        },
        modifier = Modifier.padding(vertical = 16.dp),
        spaceBetween = 8.dp,
      )
    }
    if (uiState.paymentDetails.mandate != null) {
      HorizontalItemsWithMaximumSpaceTaken(
        startSlot = {
          HedvigText(stringResource(id = R.string.PAYMENTS_MANDATE))
        },
        endSlot = {
          HedvigText(
            text = uiState.paymentDetails.mandate,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth(),
            color = HedvigTheme.colorScheme.textSecondary,
          )
        },
        modifier = Modifier.padding(vertical = 16.dp),
        spaceBetween = 8.dp,
      )
    }
    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(16.dp))
    HedvigButton(
      text = stringResource(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT),
      onClick = onChangeBankAccount,
      enabled = true,
      buttonStyle = ButtonDefaults.ButtonStyle.Secondary,
      modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
        .hedvigPlaceholder(
          uiState.isRetrying,
          shape = HedvigTheme.shapes.cornerSmall,
          highlight = PlaceholderHighlight.shimmer(),
        ),
    )
  }

}

@Composable
private fun Int.format(): String {
  val day = this
  val lastDigit = day % 10

  val locale = getLocale()

  val suffix = when (locale.language) {
    "en" -> when (lastDigit) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }

    "sv" -> when (day) {
      11, 12 -> ":e"
      else -> when (lastDigit) {
        1, 2 -> ":a"
        else -> ":e"
      }
    }

    else -> when (lastDigit) {
      1 -> "st"
      2 -> "nd"
      3 -> "rd"
      else -> "th"
    }
  }

  return "$day$suffix"
}


@Composable
@HedvigShortMultiScreenPreview
internal fun MemberPaymentDetailsScreenPreview(
  @PreviewParameter(
    MemberPaymentDetailsUiStatePreviewParameterProvider::class,
  ) uiState: MemberPaymentDetailsUiState,
) {
  HedvigTheme {
    Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
      MemberPaymentDetailsScreen(uiState,
        {},
        {},
        {})
    }
  }
}

private class MemberPaymentDetailsUiStatePreviewParameterProvider() :
  CollectionPreviewParameterProvider<MemberPaymentDetailsUiState>(
    listOf(
      MemberPaymentDetailsUiState.Failure,
      MemberPaymentDetailsUiState.Loading,
      MemberPaymentDetailsUiState.Success(
        paymentDetails = MemberPaymentsDetails(
          chargingDayInTheMonth = 28,
          descriptor = "description",
          displayName = "displayName",
          mandate = "hedvig mandate",
          paymentMethod = "bankgiro",
        ),
      ),
    ),
  )
