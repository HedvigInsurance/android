package com.hedvig.android.data.claimflow

import com.hedvig.android.data.claimflow.model.FlowId
import kotlinx.datetime.LocalDate
import octopus.fragment.AudioContentFragment
import octopus.fragment.CheckoutMethodFragment
import octopus.fragment.ClaimFlowStepFragment
import octopus.fragment.CompensationFragment
import octopus.fragment.FlowClaimContractSelectStepFragment
import octopus.fragment.FlowClaimDeflectPartnerFragment
import octopus.fragment.FlowClaimFileUploadFragment
import octopus.fragment.FlowClaimLocationStepFragment
import octopus.fragment.FlowClaimSingleItemStepFragment
import octopus.fragment.MoneyFragment
import octopus.type.CurrencyCode

/**
 * A local, exhaustive list of the supported FlowSteps in Android for the Claim Flow.
 */
sealed interface ClaimFlowStep {
  val flowId: FlowId

  data class ClaimAudioRecordingStep(
    override val flowId: FlowId,
    val questions: List<String>,
    val audioContent: AudioContentFragment?,
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

  data class ClaimPhoneNumberStep(
    override val flowId: FlowId,
    val phoneNumber: String,
  ) : ClaimFlowStep

  data class ClaimSelectContractStep(
    override val flowId: FlowId,
    val options: List<FlowClaimContractSelectStepFragment.Option>,
  ) : ClaimFlowStep

  data class ClaimSingleItemStep(
    override val flowId: FlowId,
    val preferredCurrency: CurrencyCode,
    val purchaseDate: LocalDate?,
    val purchasePrice: MoneyFragment?,
    val availableItemBrands: List<FlowClaimSingleItemStepFragment.AvailableItemBrand>?,
    val selectedItemBrand: String?,
    val availableItemModels: List<FlowClaimSingleItemStepFragment.AvailableItemModel>?,
    val selectedItemModel: String?,
    val customName: String?,
    val availableItemProblems: List<FlowClaimSingleItemStepFragment.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
  ) : ClaimFlowStep

  data class ClaimResolutionSingleItemStep(
    override val flowId: FlowId,
    val compensation: CompensationFragment,
    val availableCheckoutMethods: List<CheckoutMethodFragment>,
    val singleItemStep: ClaimFlowStepFragment.FlowClaimSingleItemCheckoutStepCurrentStep.SingleItemStep?,
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
    val customName: String?,
    val availableItemProblems: List<FlowClaimSingleItemStepFragment.AvailableItemProblem>?,
    val selectedItemProblems: List<String>?,
    val signedAudioUrl: String?,
    val fileUploads: List<ClaimFlowStepFragment.FlowClaimSummaryStepCurrentStep.FileUploadStep.Upload>?,
  ) : ClaimFlowStep

  data class ClaimDeflectGlassDamageStep(
    override val flowId: FlowId,
    val partners: List<FlowClaimDeflectPartnerFragment>,
  ) : ClaimFlowStep

  data class ClaimDeflectTowingStep(
    override val flowId: FlowId,
    val partners: List<FlowClaimDeflectPartnerFragment>,
  ) : ClaimFlowStep

  data class ClaimDeflectEirStep(
    override val flowId: FlowId,
    val partners: List<FlowClaimDeflectPartnerFragment>,
  ) : ClaimFlowStep

  data class ClaimConfirmEmergencyStep(
    override val flowId: FlowId,
    val text: String,
    val confirmEmergency: Boolean?,
    val options: List<ClaimFlowStepFragment.FlowClaimConfirmEmergencyStepCurrentStep.Option>,
  ) : ClaimFlowStep

  data class ClaimDeflectEmergencyStep(
    override val flowId: FlowId,
    val partners: List<FlowClaimDeflectPartnerFragment>,
  ) : ClaimFlowStep

  data class ClaimDeflectPestsStep(
    override val flowId: FlowId,
    val partners: List<FlowClaimDeflectPartnerFragment>,
  ) : ClaimFlowStep

  data class ClaimFileUploadStep(
    override val flowId: FlowId,
    val title: String,
    val targetUploadUrl: String,
    val uploads: List<FlowClaimFileUploadFragment.Upload>,
  ) : ClaimFlowStep

  data class ClaimFailedStep(override val flowId: FlowId) : ClaimFlowStep

  data class ClaimSuccessStep(override val flowId: FlowId) : ClaimFlowStep

  /**
   * When the client does not know how to parse a step, probably due to having an old Schema, it defaults to this
   * screen
   */
  data class UnknownStep(override val flowId: FlowId) : ClaimFlowStep
}
