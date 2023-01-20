package com.hedvig.android.odyssey.resolution.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.odyssey.model.Resolution
import com.hedvig.common.remote.money.MonetaryAmount

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
      title = "Transferring funds...",
      onSuccessTitle = "Payout completed",
      onSuccessMessage = "We have transferred 1500 SEK",
      onContinueMessage = "Continue",
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
