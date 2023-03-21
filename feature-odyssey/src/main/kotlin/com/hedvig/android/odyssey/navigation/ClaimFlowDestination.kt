package com.hedvig.android.odyssey.navigation

import com.hedvig.android.odyssey.model.FlowId
import com.kiwi.navigationcompose.typed.Destination
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import octopus.fragment.ClaimFlowStepFragment
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

internal sealed interface Destinations : Destination {
  @Serializable
  object ClaimFlow : Destinations
}

internal sealed interface ClaimFlowDestination : Destination {
  @Serializable
  object StartStep : ClaimFlowDestination

  @Serializable
  data class AudioRecording(val flowId: FlowId, val questions: List<String>) : ClaimFlowDestination

  @Serializable
  data class DateOfOccurrence(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
  ) : ClaimFlowDestination

  @Serializable
  data class Location(
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination

  @Serializable
  data class DateOfOccurrencePlusLocation(
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val selectedLocation: String?,
    val locationOptions: List<LocationOption>,
  ) : ClaimFlowDestination

  @Serializable
  data class PhoneNumber(val phoneNumber: String) : ClaimFlowDestination

  @Serializable
  data class SingleItem(
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.PurchasePrice?,
    val availableItemBrands: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowDestination

  @Serializable
  data class SingleItemCheckout(
    val price: MoneyFragment,
    val depreciation: MoneyFragment,
    val deductible: MoneyFragment,
    val payoutAmount: MoneyFragment,
    val availableCheckoutMethods: List<ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep.AvailableCheckoutMethod>,
  ) : ClaimFlowDestination

  @Serializable
  object SingleItemPayout : ClaimFlowDestination

  @Serializable
  object ManualHandling : ClaimFlowDestination // todo which step is this?

  @Serializable
  object Failure : ClaimFlowDestination

  @Serializable
  object UnknownScreen : ClaimFlowDestination
}

@Serializable
internal data class LocationOption(
  val value: String,
  val displayName: String,
)
