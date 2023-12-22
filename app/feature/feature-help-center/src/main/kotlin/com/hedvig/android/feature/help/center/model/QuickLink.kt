package com.hedvig.android.feature.help.center.model

import androidx.annotation.StringRes
import com.hedvig.android.navigation.core.AppDestination
import hedvig.resources.R
import kotlinx.collections.immutable.persistentListOf

internal enum class QuickLink(
  val destination: AppDestination,
  @StringRes val titleRes: Int,
) {
  ChangeBank(AppDestination.ConnectPayment, R.string.HC_QUICK_ACTIONS_CHANGE_BANK),
  UpdateAddress(AppDestination.ChangeAddress, R.string.HC_QUICK_ACTIONS_UPDATE_ADDRESS),
  EditCoInsured(
    AppDestination.TopLevelDestination.Insurance,
    R.string.HC_QUICK_ACTIONS_EDIT_COINSURED,
  ), // TODO go to specific contract id and start add or edit co-insured
  TravelCertificate(AppDestination.GenerateTravelCertificate, R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE),
}

internal val quickLinks = persistentListOf(
  QuickLink.ChangeBank,
  QuickLink.UpdateAddress,
  // QuickLink.EditCoInsured,
  QuickLink.TravelCertificate,
)
