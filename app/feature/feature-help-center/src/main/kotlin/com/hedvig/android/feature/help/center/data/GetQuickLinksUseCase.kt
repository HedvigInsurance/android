package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.help.center.data.QuickLinkDestination.InnerHelpCenterDestination.ChooseInsuranceForEditCoInsured
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.feature.help.center.model.QuickAction.StandaloneQuickLink
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.emergency.FirstVetSection
import hedvig.resources.R
import kotlinx.coroutines.flow.first
import octopus.AvailableSelfServiceOnContractsQuery

internal class GetQuickLinksUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val getMemberActionsUseCase: GetMemberActionsUseCase,
) {
  suspend fun invoke(): Either<ErrorMessage, List<QuickAction>> = either {
    val memberActionOptions = getMemberActionsUseCase.invoke().bind()

    buildList {
      val linksToExpand = buildList {
        if (memberActionOptions.isEditCoInsuredEnabled) {
          val contracts = apolloClient.query(AvailableSelfServiceOnContractsQuery())
            .safeExecute(::ErrorMessage)
            .onLeft { logcat(LogPriority.ERROR) { "Could not fetch contracts ${it.message}" } }
            .bind()
            .currentMember
            .activeContracts
          contracts
            .filter { it.supportsCoInsured }
            .takeIf { featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).first() }
            ?.createCoInsuredQuickLink()
            ?.let { quickAction ->
              add(quickAction)
            }
        }
        if (memberActionOptions.isTierChangeEnabled) {
          add(
            StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeTier,
              titleRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE,
              hintTextRes = R.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE,
            ),
          )
        }
        if (memberActionOptions.isCancelInsuranceEnabled) {
          add(
            StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTermination,
              titleRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
              hintTextRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
            ),
          )
        }
      }
      if (linksToExpand.isNotEmpty()) {
        add(
          QuickAction.MultiSelectExpandedLink(
            links = linksToExpand,
            titleRes = R.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isMovingEnabled) {
        add(
          StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeAddress,
            titleRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isConnectPaymentEnabled) {
        add(
          StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkConnectPayment,
            titleRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isTravelCertificateEnabled) {
        add(
          StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate,
            titleRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.firstVetAction?.sections?.isNotEmpty() == true) {
        add(
          StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.InnerHelpCenterDestination.FirstVet(
              sections = memberActionOptions.firstVetAction.sections,
            ),
            titleRes = R.string.HC_QUICK_ACTIONS_FIRSTVET_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_FIRSTVET_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.sickAbroadAction?.partners?.isNotEmpty() == true) {
        add(
          StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.InnerHelpCenterDestination.QuickLinkSickAbroad(
              emergencyNumber = memberActionOptions.sickAbroadAction.partners[0].phoneNumber,
              emergencyUrl = memberActionOptions.sickAbroadAction.partners[0].url,
            ),
            titleRes = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE,
          ),
        )
      }
    }
  }
}

private fun List<AvailableSelfServiceOnContractsQuery.Data.CurrentMember.ActiveContract>.createCoInsuredQuickLink():
  StandaloneQuickLink? {
  if (this.size > 1) {
    return StandaloneQuickLink(
      titleRes = R.string.HC_QUICK_ACTIONS_EDIT_COINSURED,
      hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
      quickLinkDestination = ChooseInsuranceForEditCoInsured,
    )
  } else if (this.size == 1) {
    val contract = this.first()
    return if (contract.coInsured?.any { it.hasMissingInfo } == true) {
      StandaloneQuickLink(
        quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo(this.first().id),
        titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
        hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
      )
    } else {
      StandaloneQuickLink(
        quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove(this.first().id),
        titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
        hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
      )
    }
  } else {
    return null
  }
}

sealed interface QuickLinkDestination {
  sealed interface OuterDestination : QuickLinkDestination {
    data class QuickLinkCoInsuredAddInfo(val contractId: String) : OuterDestination

    data class QuickLinkCoInsuredAddOrRemove(val contractId: String) : OuterDestination

    data object QuickLinkTermination : OuterDestination

    data object QuickLinkTravelCertificate : OuterDestination

    data object QuickLinkChangeAddress : OuterDestination

    data object QuickLinkConnectPayment : OuterDestination

    data object QuickLinkChangeTier : OuterDestination
  }

  sealed interface InnerHelpCenterDestination : QuickLinkDestination {
    data object ChooseInsuranceForEditCoInsured : InnerHelpCenterDestination

    data class QuickLinkSickAbroad(
      val emergencyNumber: String?,
      val emergencyUrl: String?,
    ) : InnerHelpCenterDestination

    data class FirstVet(
      val sections: List<FirstVetSection>,
    ) : InnerHelpCenterDestination
  }
}
