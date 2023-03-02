package com.hedvig.android.odyssey.resolution.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.odyssey.remote.money.MonetaryAmount
import com.hedvig.odyssey.remote.money.MonetaryAmountFormatterOptions
import com.hedvig.odyssey.remote.money.MonetaryAmountFormatterStyle
import com.hedvig.odyssey.remote.money.format

@Composable
fun SingleItemPayout(
  resolution: Resolution.SingleItemPayout,
  onPayout: (MonetaryAmount) -> Unit,
  isLoadingPayout: Boolean,
  isCompleted: Boolean,
  onFinish: () -> Unit,
) {
  if (isLoadingPayout || isCompleted) {
    PayoutProgress(
      title = stringResource(hedvig.resources.R.string.claims_payout_progress_title),
      onSuccessTitle = resolution.payoutAmount.amount ?: "", // TODO
      onSuccessMessage = stringResource(hedvig.resources.R.string.claims_payout_success_message),
      onContinueMessage = stringResource(hedvig.resources.R.string.claims_payout_done_label),
      onContinue = onFinish,
      isCompleted = isCompleted,
    )
  } else {
    PayoutSummary(
      resolution = resolution,
      onPayout = onPayout,
    )
  }
}
