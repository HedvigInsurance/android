package com.hedvig.android.feature.payin.account.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.paying.member.InvoiceDelivery
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.sortedForDisplay
import com.hedvig.android.data.paying.member.toPayinAccount
import com.hedvig.android.feature.payin.account.navigation.PayinMethodId
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.GetPayinMethodsQuery
import octopus.type.MemberPaymentProvider

internal data class PayinAccountData(
  val currentMethods: List<PayinAccount>,
  val availablePayinMethods: List<MemberPaymentProvider>,
  /** Day of the month the member is charged on. Set per member, so it is the same for every method. */
  val chargingDay: Int?,
)

@SingleIn(AppScope::class)
@Inject
internal class GetPayinAccountUseCase(
  private val apolloClient: ApolloClient,
) {
  suspend fun invoke(): Either<ErrorMessage, PayinAccountData> = either {
    val result = apolloClient
      .query(GetPayinMethodsQuery())
      .fetchPolicy(FetchPolicy.NetworkOnly)
      .safeExecute(::ErrorMessage)
      .bind()

    val paymentMethods = result.currentMember.paymentMethods

    val currentMethods = paymentMethods.payinMethods.mapNotNull { it.toPayinAccount() }.sortedForDisplay()
    val availablePayinMethods = paymentMethods.availableMethods
      .filter { it.supportsPayin }
      .map { it.provider }
    PayinAccountData(
      currentMethods = currentMethods,
      availablePayinMethods = availablePayinMethods,
      chargingDay = paymentMethods.chargingDay,
    )
  }
}

internal val PayinAccount.id: PayinMethodId
  get() = when (this) {
    is PayinAccount.Trustly -> PayinMethodId.Trustly
    is PayinAccount.SwishPayin -> PayinMethodId.Swish
    is PayinAccount.Invoice -> PayinMethodId.Invoice
  }

internal fun InvoiceDelivery?.toDeliveryString(): String? {
  return when (this) {
    // TODO: Add "Kivra" / "Kivra" to Lokalise
    InvoiceDelivery.Kivra -> "Kivra"

    // TODO: Add "Email" / "E-post" to Lokalise
    InvoiceDelivery.Mail -> "Email"

    null -> null
  }
}
