package com.hedvig.android.odyssey.data

import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.LocationOption
import kotlinx.datetime.LocalDate
import octopus.fragment.ClaimFlowStepFragment
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

/**
 * A local, exhaustive list of the supported FlowSteps in Android for the Claim Flow.
 */
internal sealed interface ClaimFlowStep {
  val flowId: FlowId

  data class ClaimAudioRecordingStep(override val flowId: FlowId, val questions: List<String>) :
    ClaimFlowStep

  data class ClaimDateOfOccurrenceStep(
    override val flowId: FlowId,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
  ) : ClaimFlowStep

  data class ClaimLocationStep(
    override val flowId: FlowId,
    val location: String?,
    val options: List<ClaimFlowStepFragment.FlowClaimLocationStepCurrentStep.Option>,
  ) : ClaimFlowStep

  data class ClaimDateOfOccurrencePlusLocationStep(
    override val flowId: FlowId,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val location: String?,
    val options: List<ClaimFlowStepFragment.FlowClaimDateOfOccurrencePlusLocationStepCurrentStep.LocationStep.Option>,
  ) : ClaimFlowStep

  data class ClaimPhoneNumberStep(override val flowId: FlowId, val phoneNumber: String) : ClaimFlowStep
  data class ClaimSingleItemStep(
    override val flowId: FlowId,
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.PurchasePrice?,
    val availableItemBrands: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowStep

  data class ClaimResolutionSingleItemStep(
    override val flowId: FlowId,
    val price: MoneyFragment,
    val depreciation: MoneyFragment,
    val deductible: MoneyFragment,
    val payoutAmount: MoneyFragment,
    val availableCheckoutMethods: List<ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep.AvailableCheckoutMethod>,
  ) : ClaimFlowStep

  data class ClaimFailedStep(override val flowId: FlowId) : ClaimFlowStep
  data class ClaimSuccessStep(override val flowId: FlowId) : ClaimFlowStep

  /**
   * When the client does not know how to parse a step, probably due to having an old Schema, it defaults to this
   * screen
   */
  data class UnknownStep(override val flowId: FlowId) : ClaimFlowStep
}

internal fun ClaimFlowStepFragment.CurrentStep.toClaimFlowStep(): ClaimFlowStep {
  return when (this) {
    is ClaimFlowStepFragment.FlowClaimAudioRecordingStepCurrentStep -> {
      ClaimFlowStep.ClaimAudioRecordingStep(FlowId(id), questions)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrenceStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrenceStep(FlowId(id), dateOfOccurrence, maxDate)
    }
    is ClaimFlowStepFragment.FlowClaimLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimLocationStep(FlowId(id), location, options)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrencePlusLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrencePlusLocationStep(
        FlowId(id),
        dateOfOccurrenceStep.dateOfOccurrence,
        dateOfOccurrenceStep.maxDate,
        locationStep.location,
        locationStep.options,
      )
    }
    is ClaimFlowStepFragment.FlowClaimPhoneNumberStepCurrentStep -> {
      ClaimFlowStep.ClaimPhoneNumberStep(FlowId(id), phoneNumber)
    }
    is ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep -> {
      ClaimFlowStep.ClaimSingleItemStep(
        FlowId(id),
        preferredCurrency,
        purchaseDate,
        purchasePrice,
        availableItemBrands,
        selectedItemBrand,
        availableItemModels,
        selectedItemModel,
        availableItemProblems,
        selectedItemProblems,
      )
    }
    is ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep -> {
      ClaimFlowStep.ClaimResolutionSingleItemStep(
        FlowId(id),
        price,
        depreciation,
        deductible,
        payoutAmount,
        availableCheckoutMethods,
      )
    }
    is ClaimFlowStepFragment.FlowClaimFailedStepCurrentStep -> ClaimFlowStep.ClaimFailedStep(FlowId(id))
    is ClaimFlowStepFragment.FlowClaimSuccessStepCurrentStep -> ClaimFlowStep.ClaimSuccessStep(FlowId(id))
    else -> ClaimFlowStep.UnknownStep(FlowId(id))
  }
}

internal fun ClaimFlowStep.toClaimFlowDestination(): ClaimFlowDestination {
  return when (this) {
    is ClaimFlowStep.ClaimAudioRecordingStep -> {
      ClaimFlowDestination.AudioRecording(flowId, questions)
    }
    is ClaimFlowStep.ClaimDateOfOccurrenceStep -> {
      ClaimFlowDestination.DateOfOccurrence(dateOfOccurrence, maxDate)
    }
    is ClaimFlowStep.ClaimLocationStep -> {
      ClaimFlowDestination.Location(
        selectedLocation = location,
        locationOptions = options.map { LocationOption(it.value, it.displayName) },
      )
    }
    is ClaimFlowStep.ClaimDateOfOccurrencePlusLocationStep -> {
      ClaimFlowDestination.DateOfOccurrencePlusLocation(
        dateOfOccurrence = dateOfOccurrence,
        maxDate = maxDate,
        selectedLocation = location,
        locationOptions = options.map { LocationOption(it.value, it.displayName) },
      )
    }
    is ClaimFlowStep.ClaimPhoneNumberStep -> ClaimFlowDestination.PhoneNumber(phoneNumber)
    is ClaimFlowStep.ClaimSingleItemStep -> {
      ClaimFlowDestination.SingleItem(
        preferredCurrency = preferredCurrency,
        purchaseDate = purchaseDate,
        purchasePrice = purchasePrice,
        availableItemBrands = availableItemBrands,
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels,
        selectedItemModel = selectedItemModel,
        availableItemProblems = availableItemProblems,
        selectedItemProblems = selectedItemProblems,
      )
    }
    is ClaimFlowStep.ClaimResolutionSingleItemStep -> {
      ClaimFlowDestination.SingleItemCheckout(
        price,
        depreciation,
        deductible,
        payoutAmount,
        availableCheckoutMethods,
      )
    }
    is ClaimFlowStep.ClaimSuccessStep -> ClaimFlowDestination.SingleItemPayout
    is ClaimFlowStep.ClaimFailedStep -> ClaimFlowDestination.Failure
    is ClaimFlowStep.UnknownStep -> ClaimFlowDestination.UnknownScreen
  }
}
