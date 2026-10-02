package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.data.paying.member.PaymentProvider
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

/** The picker for connecting a new payin method, seeded with what the member can and already has. */
@Serializable
internal data class SelectPayinMethodKey(
  val availableProviders: List<PaymentProvider>,
  val currentProviders: List<PaymentProvider>,
) : HedvigNavKey
