package com.hedvig.android.odyssey.data

import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.AvailableItemBrand
import com.hedvig.android.odyssey.navigation.AvailableItemModel
import com.hedvig.android.odyssey.navigation.AvailableItemProblem
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.LocationOption
import com.hedvig.android.odyssey.navigation.UiMoney
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
    val purchasePrice: MoneyFragment?,
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

internal fun ClaimFlowStepFragment.CurrentStep.toClaimFlowStep(flowId: FlowId): ClaimFlowStep {
  return when (this) {
    is ClaimFlowStepFragment.FlowClaimAudioRecordingStepCurrentStep -> {
      ClaimFlowStep.ClaimAudioRecordingStep(flowId, questions)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrenceStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrenceStep(flowId, dateOfOccurrence, maxDate)
    }
    is ClaimFlowStepFragment.FlowClaimLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimLocationStep(flowId, location, options)
    }
    is ClaimFlowStepFragment.FlowClaimDateOfOccurrencePlusLocationStepCurrentStep -> {
      ClaimFlowStep.ClaimDateOfOccurrencePlusLocationStep(
        flowId,
        dateOfOccurrenceStep.dateOfOccurrence,
        dateOfOccurrenceStep.maxDate,
        locationStep.location,
        locationStep.options,
      )
    }
    is ClaimFlowStepFragment.FlowClaimPhoneNumberStepCurrentStep -> {
      ClaimFlowStep.ClaimPhoneNumberStep(flowId, phoneNumber)
    }
    is ClaimFlowStepFragment.FlowClaimSingleItemStepCurrentStep -> {
      ClaimFlowStep.ClaimSingleItemStep(
        flowId,
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
        flowId,
        price,
        depreciation,
        deductible,
        payoutAmount,
        availableCheckoutMethods,
      )
    }
    is ClaimFlowStepFragment.FlowClaimFailedStepCurrentStep -> ClaimFlowStep.ClaimFailedStep(flowId)
    is ClaimFlowStepFragment.FlowClaimSuccessStepCurrentStep -> ClaimFlowStep.ClaimSuccessStep(flowId)
    else -> ClaimFlowStep.UnknownStep(flowId)
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
        purchasePrice = UiMoney.fromMoneyFragment(purchasePrice),
        availableItemBrands = availableItemBrands?.map {
          AvailableItemBrand(it.displayName, it.itemTypeId, it.itemBrandId)
        },
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels?.map {
          AvailableItemModel(it.displayName, it.imageUrl, it.itemTypeId, it.itemBrandId, it.itemModelId)
        },
        selectedItemModel = selectedItemModel,
        availableItemProblems = availableItemProblems?.map { AvailableItemProblem(it.displayName, it.itemProblemId) },
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
