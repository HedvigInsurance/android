package com.hedvig.android.odyssey.step.singleitempayout

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.android.odyssey.resolution.ui.PayoutProgress
import com.hedvig.android.odyssey.resolution.ui.PayoutSummary
import com.hedvig.odyssey.remote.money.MonetaryAmount
import hedvig.resources.R

@Composable
internal fun ClaimSuccessDestination() {
  SingleItemPayoutScreen(
    resolution = Resolution.SingleItemPayout(
      MonetaryAmount("100", "SEK"),
      MonetaryAmount("100", "SEK"),
      MonetaryAmount("100", "SEK"),
      MonetaryAmount("100", "SEK"),
    ),
    isLoadingPayout = false,
    isCompleted = false,
    onPayout = {},
  ) { }
}

@Composable
private fun SingleItemPayoutScreen(
  resolution: Resolution.SingleItemPayout,
  isLoadingPayout: Boolean,
  isCompleted: Boolean,
  onPayout: (MonetaryAmount) -> Unit,
  onFinish: () -> Unit,
) {
  if (isLoadingPayout || isCompleted) {
    PayoutProgress(
      title = stringResource(R.string.claims_payout_progress_title),
      onSuccessTitle = resolution.payoutAmount.amount ?: "", // TODO
      onSuccessMessage = stringResource(R.string.claims_payout_success_message),
      onContinueMessage = stringResource(R.string.claims_payout_done_label),
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
