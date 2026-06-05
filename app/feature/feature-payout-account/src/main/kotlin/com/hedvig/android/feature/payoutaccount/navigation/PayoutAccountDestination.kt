package com.hedvig.android.feature.payoutaccount.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object PayoutAccountKey : HedvigNavKey

@Serializable
internal data class SelectPayoutMethodKey(
  val availableProviders: List<String>,
) : HedvigNavKey

@Serializable
internal data object EditBankAccountKey : HedvigNavKey

@Serializable
internal data object SetupSwishPayoutKey : HedvigNavKey

@Serializable
internal data object SetupInvoicePayoutKey : HedvigNavKey
