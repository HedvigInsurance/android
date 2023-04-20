package com.hedvig.android.odyssey.data

import com.hedvig.android.odyssey.model.AudioUrl
import com.hedvig.android.odyssey.model.FlowId
import com.hedvig.android.odyssey.navigation.CheckoutMethod
import com.hedvig.android.odyssey.navigation.ClaimFlowDestination
import com.hedvig.android.odyssey.navigation.ItemBrand
import com.hedvig.android.odyssey.navigation.ItemModel
import com.hedvig.android.odyssey.navigation.ItemProblem
import com.hedvig.android.odyssey.navigation.LocationOption
import com.hedvig.android.odyssey.navigation.UiMoney
import com.hedvig.android.odyssey.navigation.UiNullableMoney
import kotlinx.datetime.LocalDate
import octopus.fragment.AutomaticAutogiroPayoutFragment
import octopus.fragment.CheckoutMethodFragment
import octopus.fragment.ClaimFlowStepFragment
import octopus.fragment.FlowClaimLocationStepFragment
import octopus.fragment.FlowClaimSingleItemStepFragment
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

/**
 * A local, exhaustive list of the supported FlowSteps in Android for the Claim Flow.
 */
internal sealed interface ClaimFlowStep {
  val flowId: FlowId

  data class ClaimAudioRecordingStep(
    override val flowId: FlowId,
    val questions: List<String>,
    val signedUrl: AudioUrl?,
  ) : ClaimFlowStep

  data class ClaimDateOfOccurrenceStep(
    override val flowId: FlowId,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
  ) : ClaimFlowStep

  data class ClaimLocationStep(
    override val flowId: FlowId,
    val location: String?,
    val options: List<FlowClaimLocationStepFragment.Option>,
  ) : ClaimFlowStep

  data class ClaimDateOfOccurrencePlusLocationStep(
    override val flowId: FlowId,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val location: String?,
    val options: List<FlowClaimLocationStepFragment.Option>,
  ) : ClaimFlowStep

  data class ClaimPhoneNumberStep(override val flowId: FlowId, val phoneNumber: String) : ClaimFlowStep
  data class ClaimSingleItemStep(
    override val flowId: FlowId,
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: MoneyFragment?,
    val availableItemBrands: List<FlowClaimSingleItemStepFragment.AvailableItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<FlowClaimSingleItemStepFragment.AvailableItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<FlowClaimSingleItemStepFragment.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowStep

  data class ClaimResolutionSingleItemStep(
    override val flowId: FlowId,
    val price: MoneyFragment,
    val depreciation: MoneyFragment,
    val deductible: MoneyFragment,
    val payoutAmount: MoneyFragment,
    val availableCheckoutMethods: List<CheckoutMethodFragment>,
  ) : ClaimFlowStep

  data class ClaimSummaryStep(
    override val flowId: FlowId,
    val claimTypeTitle: String,
    val location: String?,
    val options: List<FlowClaimLocationStepFragment.Option>,
    val dateOfOccurrence: LocalDate?,
    val maxDate: LocalDate,
    val preferredCurrency: CurrencyCode?,
    val purchaseDate: LocalDate?,
    val purchasePrice: MoneyFragment?,
    val availableItemBrands: List<FlowClaimSingleItemStepFragment.AvailableItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<FlowClaimSingleItemStepFragment.AvailableItemModel>?,
    val selectedItemModel: String?,
    val availableItemProblems: List<FlowClaimSingleItemStepFragment.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
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
      ClaimFlowStep.ClaimAudioRecordingStep(flowId, questions, signedUrl?.let(::AudioUrl))
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
    is ClaimFlowStepFragment.FlowClaimSummaryStepCurrentStep -> {
      ClaimFlowStep.ClaimSummaryStep(
        flowId,
        title,
        locationStep.location,
        locationStep.options,
        dateOfOccurrenceStep.dateOfOccurrence,
        dateOfOccurrenceStep.maxDate,
        singleItemStep?.preferredCurrency,
        singleItemStep?.purchaseDate,
        singleItemStep?.purchasePrice,
        singleItemStep?.availableItemBrands,
        singleItemStep?.selectedItemBrand,
        singleItemStep?.availableItemModels,
        singleItemStep?.selectedItemModel,
        singleItemStep?.availableItemProblems,
        singleItemStep?.selectedItemProblems,
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
      ClaimFlowDestination.AudioRecording(flowId, questions, signedUrl)
    }
    is ClaimFlowStep.ClaimDateOfOccurrenceStep -> {
      ClaimFlowDestination.DateOfOccurrence(dateOfOccurrence, maxDate)
    }
    is ClaimFlowStep.ClaimLocationStep -> {
      ClaimFlowDestination.Location(
        selectedLocation = location,
        locationOptions = options.map { it.toLocationOption() },
      )
    }
    is ClaimFlowStep.ClaimDateOfOccurrencePlusLocationStep -> {
      ClaimFlowDestination.DateOfOccurrencePlusLocation(
        dateOfOccurrence = dateOfOccurrence,
        maxDate = maxDate,
        selectedLocation = location,
        locationOptions = options.map { it.toLocationOption() },
      )
    }
    is ClaimFlowStep.ClaimPhoneNumberStep -> ClaimFlowDestination.PhoneNumber(phoneNumber)
    is ClaimFlowStep.ClaimSingleItemStep -> {
      ClaimFlowDestination.SingleItem(
        preferredCurrency = preferredCurrency,
        purchaseDate = purchaseDate,
        purchasePrice = UiNullableMoney.fromMoneyFragment(purchasePrice),
        availableItemBrands = availableItemBrands?.map { it.toItemBrand() },
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels?.map { it.toItemModel() },
        selectedItemModel = selectedItemModel,
        availableItemProblems = availableItemProblems?.map { it.toItemProblem() },
        selectedItemProblems = selectedItemProblems,
      )
    }
    is ClaimFlowStep.ClaimSummaryStep -> {
      ClaimFlowDestination.Summary(
        claimTypeTitle = claimTypeTitle,
        selectedLocation = location,
        locationOptions = options.map { it.toLocationOption() },
        dateOfOccurrence = dateOfOccurrence,
        maxDate = maxDate,
        preferredCurrency = preferredCurrency,
        purchaseDate = purchaseDate,
        purchasePrice = UiNullableMoney.fromMoneyFragment(purchasePrice),
        availableItemBrands = availableItemBrands?.map { it.toItemBrand() },
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels?.map { it.toItemModel() },
        selectedItemModel = selectedItemModel,
        availableItemProblems = availableItemProblems?.map { it.toItemProblem() },
        selectedItemProblems = selectedItemProblems,
      )
    }
    is ClaimFlowStep.ClaimResolutionSingleItemStep -> {
      ClaimFlowDestination.SingleItemCheckout(
        UiMoney.fromMoneyFragment(price),
        UiMoney.fromMoneyFragment(depreciation),
        UiMoney.fromMoneyFragment(deductible),
        UiMoney.fromMoneyFragment(payoutAmount),
        availableCheckoutMethods.map(CheckoutMethodFragment::toCheckoutMethod).filterIsInstance<CheckoutMethod.Known>(),
      )
    }
    is ClaimFlowStep.ClaimSuccessStep -> ClaimFlowDestination.ClaimSuccess
    is ClaimFlowStep.ClaimFailedStep -> ClaimFlowDestination.Failure
    is ClaimFlowStep.UnknownStep -> ClaimFlowDestination.UpdateApp
  }
}

internal fun FlowClaimSingleItemStepFragment.AvailableItemModel.toItemModel(): ItemModel {
  return ItemModel.Known(displayName, imageUrl, itemTypeId, itemBrandId, itemModelId)
}

internal fun FlowClaimSingleItemStepFragment.AvailableItemProblem.toItemProblem(): ItemProblem {
  return ItemProblem(displayName, itemProblemId)
}

internal fun FlowClaimSingleItemStepFragment.AvailableItemBrand.toItemBrand(): ItemBrand {
  return ItemBrand.Known(displayName, itemTypeId, itemBrandId)
}

private fun FlowClaimLocationStepFragment.Option.toLocationOption(): LocationOption {
  return LocationOption(value, displayName)
}

private fun CheckoutMethodFragment.toCheckoutMethod(): CheckoutMethod {
  return when (this) {
    is AutomaticAutogiroPayoutFragment -> {
      CheckoutMethod.Known.AutomaticAutogiro(id, displayName, UiMoney.fromMoneyFragment(amount))
    }
    else -> CheckoutMethod.Unknown
  }
}
