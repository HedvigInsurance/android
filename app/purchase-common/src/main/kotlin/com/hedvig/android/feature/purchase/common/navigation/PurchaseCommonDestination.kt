package com.hedvig.android.feature.purchase.common.navigation

import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

sealed interface PurchaseCommonDestination {
  @Serializable
  data class SelectTier(
    val params: SelectTierParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SelectTierParameters>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class Signing(
    val params: SigningParameters,
  ) : PurchaseCommonDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SigningParameters>())
    }
  }

  @Serializable
  data class Success(
    val startDate: String?,
  ) : PurchaseCommonDestination, Destination

  @Serializable
  data object Failure : PurchaseCommonDestination, Destination
}

@Serializable
data class TierOfferData(
  val offerId: String,
  val tierDisplayName: String,
  val tierDescription: String,
  val grossAmount: Double,
  val grossCurrencyCode: String,
  val netAmount: Double,
  val netCurrencyCode: String,
  val usps: List<String>,
  val exposureDisplayName: String,
  val deductibleDisplayName: String?,
  val hasDiscount: Boolean,
)

@Serializable
data class SelectTierParameters(
  val shopSessionId: String,
  val offers: List<TierOfferData>,
  val productDisplayName: String,
  val contractGroup: ContractGroup,
)

@Serializable
data class SummaryParameters(
  val shopSessionId: String,
  val selectedOffer: TierOfferData,
  val productDisplayName: String,
  val contractGroup: ContractGroup,
)

@Serializable
data class SigningParameters(
  val signingId: String,
  val autoStartToken: String,
  val startDate: String?,
)
