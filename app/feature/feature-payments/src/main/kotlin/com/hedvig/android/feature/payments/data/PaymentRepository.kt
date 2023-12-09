package com.hedvig.android.feature.payments.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.contract.ContractGroup
import kotlinx.datetime.LocalDate

internal interface PaymentRepository {
  suspend fun getChargeHistory(): Either<ErrorMessage, ChargeHistory>

  suspend fun getPaymentData(): Either<ErrorMessage, PaymentData>

  suspend fun redeemReferralCode(campaignCode: CampaignCode): Either<RedeemFailure, Unit>
}

data class ChargeHistory(
  val charges: List<Charge>,
) {
  data class Charge(
    val amount: UiMoney,
    val date: LocalDate,
    val paymentStatus: PaymentStatus,
  ) {
    enum class PaymentStatus {
      PENDING,
      SUCCESSFUL,
      FAILED,
      UNKNOWN,
    }
  }
}

@JvmInline
internal value class CampaignCode(val code: String)

internal data class PaymentData(
  val id: String,
  val upcomingChargeNet: UiMoney?,
  val upcomingChargeDate: LocalDate?,
  val monthlyCostNet: UiMoney,
  val monthlyCostDiscount: UiMoney?,
  val agreements: List<Agreement>,
  val paymentConnectionStatus: PaymentConnectionStatus,
  val paymentDisplayName: String?,
  val paymentDescriptor: String?,
  val redeemedCampaigns: List<RedeemedCampaign>,
) {
  data class Agreement(
    val contractGroup: ContractGroup,
    val displayName: String,
    val grossCost: UiMoney,
  )

  enum class PaymentConnectionStatus {
    ACTIVE,
    PENDING,
    NEEDS_SETUP,
    UNKNOWN,
  }

  data class RedeemedCampaign(
    val id: String,
    val displayName: String,
    val campaignCode: CampaignCode,
//    val discount: Discount,
  ) {
    // For new UI, differentiate betweent the discount types
    // https://studio.apollographql.com/graph/Octopus-fxevz/variant/staging/schema/reference/enums/CampaignDiscountType
//    sealed interface Discount
  }
}

sealed interface RedeemFailure {
  data object GenericFailure : RedeemFailure

  data class UserFailure(val errorMessage: ErrorMessage) : RedeemFailure, ErrorMessage by errorMessage
}
