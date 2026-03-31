package com.hedvig.android.feature.purchase.apartment.navigation

import com.hedvig.android.navigation.common.Destination
import com.hedvig.android.navigation.common.DestinationNavTypeAware
import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlinx.serialization.Serializable

@Serializable
data class ApartmentPurchaseGraphDestination(
  val productName: String,
) : Destination

internal sealed interface ApartmentPurchaseDestination {
  @Serializable
  data object Form : ApartmentPurchaseDestination, Destination

  @Serializable
  data class SelectTier(
    val params: SelectTierParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SelectTierParameters>())
    }
  }

  @Serializable
  data class Summary(
    val params: SummaryParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SummaryParameters>())
    }
  }

  @Serializable
  data class Signing(
    val params: SigningParameters,
  ) : ApartmentPurchaseDestination, Destination {
    companion object : DestinationNavTypeAware {
      override val typeList: List<KType> = listOf(typeOf<SigningParameters>())
    }
  }

  @Serializable
  data class Success(
    val startDate: String?,
  ) : ApartmentPurchaseDestination, Destination

  @Serializable
  data object Failure : ApartmentPurchaseDestination, Destination
}

@Serializable
internal data class TierOfferData(
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
internal data class SelectTierParameters(
  val shopSessionId: String,
  val offers: List<TierOfferData>,
  val productDisplayName: String,
)

@Serializable
internal data class SummaryParameters(
  val shopSessionId: String,
  val selectedOffer: TierOfferData,
  val productDisplayName: String,
)

@Serializable
internal data class SigningParameters(
  val signingId: String,
  val autoStartToken: String,
  val startDate: String?,
)
