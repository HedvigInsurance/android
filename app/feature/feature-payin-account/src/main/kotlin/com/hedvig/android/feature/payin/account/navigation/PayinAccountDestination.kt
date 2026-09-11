package com.hedvig.android.feature.payin.account.navigation

import com.hedvig.android.feature.payin.account.data.PayinAccount
import com.hedvig.android.navigation.common.HedvigNavKey
import kotlinx.serialization.Serializable

@Serializable
internal data class SelectPayinMethodKey(
  val availableProviders: List<String>,
  val currentProviders: List<String>,
) : HedvigNavKey

/**
 * Waiting on a Swish setup the member approves in the Swish app. Carries the phone number so a
 * failed attempt can be retried with it, without sending them back to re-enter it.
 */
@Serializable
internal data class SwishPayinStatusKey(
  val successUrl: String,
  val orderId: String,
  val phoneNumber: String,
) : HedvigNavKey

@Serializable
internal data object SetupInvoicePayinKey : HedvigNavKey

@Serializable
internal data class SelectPrimaryPayinMethodKey(
  val currentMethods: List<PayinAccount>,
) : HedvigNavKey
