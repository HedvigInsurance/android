package com.hedvig.android.data.claimflow

import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiFile
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.core.uidata.UiNullableMoney
import com.hedvig.android.data.claimflow.model.AudioUrl
import com.hedvig.android.navigation.common.Destination
import com.hedvig.audio.player.data.SignedAudioUrl
import octopus.fragment.AudioContentFragment
import octopus.fragment.AutomaticAutogiroPayoutFragment
import octopus.fragment.CheckoutMethodFragment
import octopus.fragment.ClaimFlowStepFragment
import octopus.fragment.FlowClaimContractSelectStepFragment
import octopus.fragment.FlowClaimDeflectIdProtectionStepFragment
import octopus.fragment.FlowClaimDeflectPartnerFragment
import octopus.fragment.FlowClaimFileUploadFragment
import octopus.fragment.FlowClaimLocationStepFragment
import octopus.fragment.FlowClaimSingleItemStepFragment

fun ClaimFlowStep.toClaimFlowDestination(): Destination {
  return when (this) {
    is ClaimFlowStep.ClaimAudioRecordingStep -> {
      ClaimFlowDestination.AudioRecording(
        flowId = flowId,
        questions = questions,
        audioContent = audioContent?.toAudioContent(),
        freeTextAvailable = freeTextAvailable,
        freeText = freeText,
        freeTextQuestions = freeTextQuestions,
      )
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
        preferredCurrency = UiCurrencyCode.fromCurrencyCode(preferredCurrency),
        purchaseDate = purchaseDate,
        purchasePrice = UiNullableMoney.fromMoneyFragment(purchasePrice),
        purchasePriceApplicable = purchasePriceApplicable,
        availableItemBrands = availableItemBrands?.map { it.toItemBrand() },
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels?.map { it.toItemModel() },
        selectedItemModel = selectedItemModel,
        customName = customName,
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
        purchaseDate = purchaseDate,
        customName = customName,
        purchasePrice = UiNullableMoney.fromMoneyFragment(purchasePrice),
        availableItemBrands = availableItemBrands?.map { it.toItemBrand() },
        selectedItemBrand = selectedItemBrand,
        availableItemModels = availableItemModels?.map { it.toItemModel() },
        selectedItemModel = selectedItemModel,
        availableItemProblems = availableItemProblems?.map { it.toItemProblem() },
        selectedItemProblems = selectedItemProblems,
        files = fileUploads?.map {
          UiFile(
            id = it.fileId,
            name = it.name,
            mimeType = it.mimeType,
            url = it.signedUrl,
            localPath = null,
          )
        } ?: listOf(),
        submittedContent = signedAudioUrl?.let {
          SubmittedContent.Audio(SignedAudioUrl.fromSignedAudioUrlString(it))
        },
      )
    }

    is ClaimFlowStep.ClaimResolutionSingleItemStep -> {
      val modelName = singleItemStep?.availableItemModels?.firstOrNull {
        it.itemModelId == singleItemStep.selectedItemModel
      }?.displayName
      val compensation: ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep.Compensation =
        this.compensation
      ClaimFlowDestination.SingleItemCheckout(
        compensation = compensation.toCompensation(),
        availableCheckoutMethods = availableCheckoutMethods.map(CheckoutMethodFragment::toCheckoutMethod)
          .filterIsInstance<CheckoutMethod.Known>(),
        modelName = modelName,
        brandName = singleItemStep?.selectedItemBrand,
        customName = singleItemStep?.customName,
      )
    }

    is ClaimFlowStep.ClaimSuccessStep -> ClaimFlowDestination.ClaimSuccess
    is ClaimFlowStep.ClaimFailedStep -> ClaimFlowDestination.Failure
    is ClaimFlowStep.UnknownStep -> ClaimFlowDestination.UpdateApp
    is ClaimFlowStep.ClaimSelectContractStep -> ClaimFlowDestination.SelectContract(
      options = options.map { it.toLocalOptions() },
    )

    is ClaimFlowStep.ClaimDeflectGlassDamageStep -> ClaimFlowDestination.DeflectGlassDamage(
      partners.map { it.toLocalPartner() },
    )

    is ClaimFlowStep.ClaimDeflectTowingStep -> ClaimFlowDestination.DeflectTowing(
      partners.map { it.toLocalPartner() },
    )

    is ClaimFlowStep.ClaimDeflectEirStep -> ClaimFlowDestination.DeflectCarOtherDamage(
      partners.map { it.toLocalPartner() },
    )

    is ClaimFlowStep.ClaimConfirmEmergencyStep -> ClaimFlowDestination.ConfirmEmergency(
      text,
      confirmEmergency,
      options.map { it.toLocalOption() },
    )

    is ClaimFlowStep.ClaimDeflectEmergencyStep -> ClaimFlowDestination.DeflectEmergency(
      partners.map { it.toLocalPartner() },
    )

    is ClaimFlowStep.ClaimDeflectPestsStep -> ClaimFlowDestination.DeflectPests(
      partners.map { it.toLocalPartner() },
    )

    is ClaimFlowStep.ClaimFileUploadStep -> ClaimFlowDestination.FileUpload(
      title,
      targetUploadUrl,
      uploads.map { it.toLocalUpload() },
    )

    is ClaimFlowStep.ClaimDeflectIdProtectionStep -> ClaimFlowDestination.DeflectIdProtection(
      title,
      description,
      partners.map { it.toLocalIdProtectionPartner() },
    )
  }
}

private fun FlowClaimContractSelectStepFragment.Option.toLocalOptions(): LocalContractContractOption {
  return LocalContractContractOption(id, displayName)
}

internal fun FlowClaimSingleItemStepFragment.AvailableItemModel.toItemModel(): ItemModel {
  return ItemModel.Known(displayName, itemTypeId, itemBrandId, itemModelId)
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

private fun ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep.Compensation.toCompensation():
  ClaimFlowDestination.SingleItemCheckout.Compensation {
  return when (this) {
    is ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep
      .FlowClaimSingleItemCheckoutRepairCompensationCompensation,
    -> {
      ClaimFlowDestination.SingleItemCheckout.Compensation.Known.RepairCompensation(
        repairCost = UiMoney.fromMoneyFragment(repairCost),
        deductible = UiMoney.fromMoneyFragment(deductible),
        payoutAmount = UiMoney.fromMoneyFragment(payoutAmount),
      )
    }

    is ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep
      .FlowClaimSingleItemCheckoutValueCompensationCompensation,
    -> {
      ClaimFlowDestination.SingleItemCheckout.Compensation.Known.ValueCompensation(
        price = UiMoney.fromMoneyFragment(price),
        deductible = UiMoney.fromMoneyFragment(deductible),
        depreciation = UiMoney.fromMoneyFragment(depreciation),
        payoutAmount = UiMoney.fromMoneyFragment(payoutAmount),
      )
    }

    else -> ClaimFlowDestination.SingleItemCheckout.Compensation.Unknown
  }
}

private fun AudioContentFragment.toAudioContent(): AudioContent {
  return AudioContent(AudioUrl(signedUrl), AudioUrl(audioUrl))
}

private fun FlowClaimDeflectIdProtectionStepFragment.Partner.toLocalIdProtectionPartner(): IdProtectionDeflectPartner {
  return IdProtectionDeflectPartner(
    title = title,
    description = description,
    info = info,
    urlButtonTitle = urlButtonTitle,
    partner = deflectPartner.toLocalPartner(),
  )
}

private fun FlowClaimDeflectPartnerFragment.toLocalPartner(): DeflectPartner {
  return DeflectPartner(
    id = id,
    imageUrl = imageUrl ?: "",
    phoneNumber = phoneNumber,
    url = url,
  )
}

private fun ClaimFlowStepFragment.FlowClaimConfirmEmergencyStepCurrentStep.Option.toLocalOption(): EmergencyOption {
  return EmergencyOption(
    displayName = displayName,
    value = displayValue,
  )
}

private fun FlowClaimFileUploadFragment.Upload.toLocalUpload(): UiFile {
  return UiFile(
    id = fileId,
    name = name,
    mimeType = mimeType,
    url = signedUrl,
    localPath = null,
  )
}
