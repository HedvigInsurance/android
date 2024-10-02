package com.hedvig.android.feature.change.tier.ui.sucess

import androidx.compose.runtime.Composable
import com.hedvig.android.design.system.hedvig.HedvigText
import kotlinx.datetime.LocalDate

@Composable
internal fun SuccessScreen(activationDate: LocalDate, navigateUp: () -> Unit) {
  HedvigText("Activation date: $activationDate")
}
