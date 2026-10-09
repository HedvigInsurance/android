package com.hedvig.android.feature.payments.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.cache.normalized.FetchPolicy.NetworkFirst
import com.apollographql.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.paying.member.PayinAccount
import com.hedvig.android.data.paying.member.toPayinAccount
import com.hedvig.android.feature.payments.data.PaymentDetails.PaymentsInfo
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.PaymentHistoryWithDetailsQuery

internal interface GetChargeDetailsUseCase {
  suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetChargeDetailsUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetChargeDetailsUseCase {
  override suspend fun invoke(id: String?): Either<ErrorMessage, PaymentDetails> = either {
    val currentMember = apolloClient.query(PaymentHistoryWithDetailsQuery())
      .fetchPolicy(NetworkFirst)
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember

    val pastCharges = currentMember.pastCharges.map {
      it.toMemberCharge(currentMember.referralInformation)
    }.reversed()
    val futureMemberCharge = currentMember.futureCharge?.toMemberCharge(currentMember.referralInformation)
    val ongoingChargeWithThisId = currentMember
      .ongoingCharges
      .firstOrNull { it.id == id }
      ?.toMemberCharge(currentMember.referralInformation)
    val futureMemberChargeWithThisId = futureMemberCharge.takeIf { it?.id == id }
    val pastMemberChargeWithThisId = pastCharges.firstOrNull { it.id == id }
    val defaultPayinMethod = currentMember.paymentMethods.payinMethods
      .mapNotNull { it.toPayinAccount() }
      .firstOrNull { it.isDefault }
    val charge = when {
      // An upcoming charge goes out on whatever method is the default by then, so that is the one shown,
      // rather than the provider the charge was drafted with.
      futureMemberChargeWithThisId != null -> {
        val defaultChargeMethod = defaultPayinMethod?.toChargeMethod()
        if (defaultChargeMethod != null) {
          futureMemberChargeWithThisId.copy(chargeMethod = defaultChargeMethod)
        } else {
          futureMemberChargeWithThisId
        }
      }

      else -> {
        pastMemberChargeWithThisId ?: ongoingChargeWithThisId ?: raise(ErrorMessage())
      }
    }
    val paymentsInfo = run {
      if (futureMemberChargeWithThisId == null && ongoingChargeWithThisId == null) {
        // Only show payment connection information if the charge is a future charge or ongoing charge.
        // Otherwise, the payment connection info we get is not reliably correct.
        return@run PaymentsInfo.NoPresentableInfo
      }
      // The connection describes the default method, so it is only shown for a charge made with that method.
      if (defaultPayinMethod == null || defaultPayinMethod.toChargeMethod() != charge.chargeMethod) {
        return@run PaymentsInfo.NoPresentableInfo
      }
      PaymentsInfo.Active(defaultPayinMethod)
    }
    PaymentDetails(
      memberCharge = charge,
      pastCharges = pastCharges,
      upComingCharge = futureMemberCharge,
      paymentsInfo = paymentsInfo,
    )
  }
}

internal data class PaymentDetails(
  val memberCharge: MemberCharge,
  val pastCharges: List<MemberCharge>?,
  val paymentsInfo: PaymentsInfo,
  val upComingCharge: MemberCharge?,
) {
  fun getNextCharge(selectedMemberCharge: MemberCharge): MemberCharge? {
    val index = (pastCharges?.indexOf(selectedMemberCharge) ?: 0) + 1
    return if (pastCharges != null && index > pastCharges.size - 1) {
      null
    } else {
      pastCharges?.get(index)
    }
  }

  sealed interface PaymentsInfo {
    data class Active(
      val account: PayinAccount,
    ) : PaymentsInfo

    data object NoPresentableInfo : PaymentsInfo
  }
}

private fun PayinAccount.toChargeMethod(): MemberPaymentChargeMethod = when (this) {
  is PayinAccount.Trustly -> MemberPaymentChargeMethod.TRUSTLY
  is PayinAccount.SwishPayin -> MemberPaymentChargeMethod.SWISH
  is PayinAccount.Invoice -> MemberPaymentChargeMethod.INVOICE
}
