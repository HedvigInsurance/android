package com.hedvig.android.feature.claimhistory

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.HedvigText

@Composable
internal fun ClaimHistoryDestination(navigateToClaimDetails: (String) -> Unit) {
  HedvigText("Claim History")
}
