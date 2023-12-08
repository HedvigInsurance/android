package com.hedvig.android.feature.payments2.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Discount(
  val code: String,
  val displayName: String?,
  val description: String?,
  val expiresAt: LocalDate?,
  val amount: UiMoney?,
  val isReferral: Boolean,
) {
  fun isExpired(now: LocalDate) = expiresAt?.let { it < now } ?: true
}
