package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
internal data class SelectPayinMethodKey(
  val availableProviders: List<String>,
  val currentProviders: List<String>,
) : HedvigNavKey

@Serializable
internal data object SetupInvoicePayinKey : HedvigNavKey

@Serializable
internal data class SelectPrimaryPayinMethodKey(
  val currentMethods: List<PayinAccount>,
) : HedvigNavKey
