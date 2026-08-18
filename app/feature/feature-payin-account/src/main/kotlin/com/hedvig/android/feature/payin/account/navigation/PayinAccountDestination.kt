package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
data object PayinAccountKey : HedvigNavKey

@Serializable
internal data class SelectPayinMethodKey(
  val availableProviders: List<String>,
) : HedvigNavKey

@Serializable
internal data object SetupSwishPayinKey : HedvigNavKey

@Serializable
internal data object SetupInvoicePayinKey : HedvigNavKey
