package com.hedvig.android.memberquickactions

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.memberquickactions.QuickLinkDestination.OuterDestination.ChooseInsuranceForEditCoOwners
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.emergency.FirstVetSection
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import hedvig.resources.CONTRACT_COOWNER
import hedvig.resources.EDIT_COOWNER_SUBTITLE
import hedvig.resources.EDIT_COOWNER_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CANCELLATION_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CO_INSURED_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_COINSURED
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_FIRSTVET_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_PAYMENTS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE
import hedvig.resources.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE
import hedvig.resources.HOME_QUICK_ACTIONS_CHANGE_ADDRESS
import hedvig.resources.HOME_QUICK_ACTIONS_EDIT_INSURANCE
import hedvig.resources.HOME_QUICK_ACTIONS_INVITE
import hedvig.resources.HOME_QUICK_ACTIONS_UPCOMING_PAYMENT
import hedvig.resources.Res
import hedvig.resources.insurance_details_change_amount
import hedvig.resources.insurance_details_change_amount_subtitle
import octopus.AvailableSelfServiceOnContractsQuery

interface GetMemberQuickActionsUseCase {
  suspend fun invoke(source: QuickActionsSource): Either<ErrorMessage, List<QuickAction>>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMemberQuickActionsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val getMemberActionsUseCase: GetMemberActionsUseCase,
) : GetMemberQuickActionsUseCase {
  override suspend fun invoke(source: QuickActionsSource): Either<ErrorMessage, List<QuickAction>> = either {
    val memberActionOptions = getMemberActionsUseCase.invoke().bind()

    val activeContracts = if (
      memberActionOptions.isEditCoInsuredEnabled ||
      memberActionOptions.isEditCoOwnersEnabled ||
      memberActionOptions.isTierChangeEnabled
    ) {
      apolloClient.query(AvailableSelfServiceOnContractsQuery())
        .safeExecute(::ErrorMessage)
        .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
        .map { it.currentMember.activeContracts }
    } else {
      null
    }

    buildList {
      val linksToExpand = buildList {
        if (memberActionOptions.isEditCoInsuredEnabled) {
          val coInsuredContracts = activeContracts?.bind().orEmpty().filter { it.supportsCoInsured }
          createEditCoInsuredQuickLink(coInsuredContracts)?.let { quickAction ->
            add(quickAction)
          }
        }
        if (memberActionOptions.isEditCoOwnersEnabled) {
          val coOwnerContracts = activeContracts?.bind().orEmpty().filter { it.supportsCoOwners }
          createEditCoOwnersQuickLink(coOwnerContracts)?.let { quickAction ->
            add(quickAction)
          }
        }
        if (memberActionOptions.isTierChangeEnabled) {
          // Falls back to the coverage wording if the contracts couldn't be fetched, since tier change itself
          // does not depend on them.
          val tierChangeableContracts = activeContracts?.getOrNull().orEmpty().filter { it.supportsChangeTier }
          val isPaymentProtection = tierChangeableContracts.isNotEmpty() &&
            tierChangeableContracts.all {
              it.currentAgreement.productVariant.typeOfContract.toContractGroup() == ContractGroup.PAYMENT_PROTECTION
            }
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeTier,
              titleRes = if (isPaymentProtection) {
                Res.string.insurance_details_change_amount
              } else {
                Res.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE
              },
              hintTextRes = if (isPaymentProtection) {
                Res.string.insurance_details_change_amount_subtitle
              } else {
                Res.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE
              },
            ),
          )
        }
        if (memberActionOptions.isCancelInsuranceEnabled) {
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTermination,
              titleRes = Res.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
              hintTextRes = Res.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
            ),
          )
        }
      }
      if (linksToExpand.isNotEmpty()) {
        add(
          QuickAction.MultiSelectExpandedLink(
            links = linksToExpand,
            titleRes = Res.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE,
            hintTextRes = Res.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE,
            shortTitleRes = Res.string.HOME_QUICK_ACTIONS_EDIT_INSURANCE,
          ),
        )
      }
      if (memberActionOptions.isMovingEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeAddress,
            titleRes = Res.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
            hintTextRes = Res.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
            shortTitleRes = Res.string.HOME_QUICK_ACTIONS_CHANGE_ADDRESS,
          ),
        )
      }
      if (source == QuickActionsSource.HELP_CENTER) {
        if (memberActionOptions.isConnectPaymentEnabled) {
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkConnectPayment,
              titleRes = Res.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
              hintTextRes = Res.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
            ),
          )
        }
      }

      if (memberActionOptions.isTravelCertificateEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate,
            titleRes = Res.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
            hintTextRes = Res.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
          ),
        )
      }
      if (source == QuickActionsSource.HELP_CENTER) {
        if (memberActionOptions.firstVetAction?.sections?.isNotEmpty() == true) {
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = InnerHelpCenterDestination.FirstVet(
                sections = memberActionOptions.firstVetAction.sections,
              ),
              titleRes = Res.string.HC_QUICK_ACTIONS_FIRSTVET_TITLE,
              hintTextRes = Res.string.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE,
            ),
          )
        }
      }

      if (memberActionOptions.sickAbroadAction != null) {
        val deflectData = memberActionOptions.sickAbroadAction.deflectData
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = InnerHelpCenterDestination.QuickLinkSickAbroad(
              deflectData,
            ),
            titleRes = Res.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE,
            hintTextRes = Res.string.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE,
          ),
        )
      }
      if (source == QuickActionsSource.HOME) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = OuterDestination.QuickLinkForever,
            titleRes = Res.string.HOME_QUICK_ACTIONS_INVITE,
            hintTextRes = Res.string.HOME_QUICK_ACTIONS_INVITE,
          ),
        )
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = OuterDestination.QuickLinkUpcomingPayment,
            titleRes = Res.string.HOME_QUICK_ACTIONS_UPCOMING_PAYMENT,
            hintTextRes = Res.string.HOME_QUICK_ACTIONS_UPCOMING_PAYMENT,
          ),
        )
      }
    }
  }
}

private fun createEditCoInsuredQuickLink(
  coInsuredContracts: List<AvailableSelfServiceOnContractsQuery.Data.CurrentMember.ActiveContract>,
): QuickAction.StandaloneQuickLink? {
  return when {
    coInsuredContracts.isEmpty() -> {
      null
    }

    coInsuredContracts.size == 1 -> {
      val contract = coInsuredContracts.first()
      if (contract.coInsured?.any { it.hasMissingInfo } == true) {
        QuickAction.StandaloneQuickLink(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo(contract.id),
          titleRes = Res.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
          hintTextRes = Res.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
        )
      } else {
        QuickAction.StandaloneQuickLink(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove(contract.id),
          titleRes = Res.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
          hintTextRes = Res.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
        )
      }
    }

    else -> {
      QuickAction.StandaloneQuickLink(
        titleRes = Res.string.HC_QUICK_ACTIONS_EDIT_COINSURED,
        hintTextRes = Res.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
        quickLinkDestination = ChooseInsuranceForEditCoInsured,
      )
    }
  }
}

private fun createEditCoOwnersQuickLink(
  coOwnerContracts: List<AvailableSelfServiceOnContractsQuery.Data.CurrentMember.ActiveContract>,
): QuickAction.StandaloneQuickLink? {
  return when {
    coOwnerContracts.isEmpty() -> {
      null
    }

    coOwnerContracts.size == 1 -> {
      val contract = coOwnerContracts.first()
      if (contract.coOwners?.any { it.hasMissingInfo } == true) {
        QuickAction.StandaloneQuickLink(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddInfo(contract.id),
          titleRes = Res.string.CONTRACT_COOWNER,
          hintTextRes = Res.string.EDIT_COOWNER_SUBTITLE,
        )
      } else {
        QuickAction.StandaloneQuickLink(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoOwnerAddOrRemove(contract.id),
          titleRes = Res.string.CONTRACT_COOWNER,
          hintTextRes = Res.string.EDIT_COOWNER_SUBTITLE,
        )
      }
    }

    else -> {
      QuickAction.StandaloneQuickLink(
        titleRes = Res.string.EDIT_COOWNER_TITLE,
        hintTextRes = Res.string.EDIT_COOWNER_SUBTITLE,
        quickLinkDestination = ChooseInsuranceForEditCoOwners,
      )
    }
  }
}

sealed interface QuickLinkDestination {
  sealed interface OuterDestination : QuickLinkDestination {
    data class QuickLinkCoInsuredAddInfo(val contractId: String) : OuterDestination

    data class QuickLinkCoInsuredAddOrRemove(val contractId: String) : OuterDestination

    data class QuickLinkCoOwnerAddInfo(val contractId: String) : OuterDestination

    data class QuickLinkCoOwnerAddOrRemove(val contractId: String) : OuterDestination

    data object QuickLinkTermination : OuterDestination

    data object QuickLinkTravelCertificate : OuterDestination

    data object QuickLinkChangeAddress : OuterDestination

    data object QuickLinkConnectPayment : OuterDestination

    data object QuickLinkChangeTier : OuterDestination

    data object ChooseInsuranceForEditCoInsured : OuterDestination

    data object ChooseInsuranceForEditCoOwners : OuterDestination

    data object QuickLinkForever : OuterDestination

    data object QuickLinkUpcomingPayment : OuterDestination
  }
}

sealed interface InnerHelpCenterDestination : QuickLinkDestination {
  data class QuickLinkSickAbroad(
    val deflectData: DeflectData,
  ) : InnerHelpCenterDestination

  data class FirstVet(
    val sections: List<FirstVetSection>,
  ) : InnerHelpCenterDestination
}

enum class QuickActionsSource {
  HELP_CENTER,
  HOME,
}
