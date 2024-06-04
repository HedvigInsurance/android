package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.ui.emergency.FirstVetSection
import hedvig.resources.R
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import octopus.AvailableSelfServiceOnContractsQuery

internal class GetQuickLinksUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val getMemberActionsUseCase: GetMemberActionsUseCase,
) {
  suspend fun invoke(): Either<ErrorMessage, PersistentList<QuickAction>> = either {
    val memberActionOptions = getMemberActionsUseCase.invoke().bind()

    buildList {
      if (memberActionOptions.isMovingEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeAddress,
            titleRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isConnectPaymentEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkConnectPayment,
            titleRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isEditCoInsuredEnabled) {
        val contracts = apolloClient.query(AvailableSelfServiceOnContractsQuery())
          .safeExecute()
          .toEither(::ErrorMessage)
          .onLeft { logcat(LogPriority.ERROR) { "Could not fetch common claims ${it.message}" } }
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
      if (memberActionOptions.isCancelInsuranceEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTermination,
            titleRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.isTravelCertificateEnabled) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkTravelCertificate,
            titleRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
          ),
        )
      }
      if (memberActionOptions.firstVetAction?.sections?.isNotEmpty() == true) {
        add(
          QuickAction.StandaloneQuickLink(
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
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.InnerHelpCenterDestination.QuickLinkSickAbroad(
              emergencyNumber = memberActionOptions.sickAbroadAction.partners[0].phoneNumber,
            ),
            titleRes = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE,
          ),
        )
      }
    }.toPersistentList()
  }
}

private fun List<AvailableSelfServiceOnContractsQuery.Data.CurrentMember.ActiveContract>.createCoInsuredQuickLink():
  QuickAction? {
  if (this.size > 1) {
    val links = this.map { contract ->
      if (contract.coInsured?.any { it.hasMissingInfo } == true) {
        QuickAction.MultiSelectQuickLink.QuickLinkForMultiSelect(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo(contract.id),
          displayName = contract.currentAgreement.productVariant.displayName,
        )
      } else {
        QuickAction.MultiSelectQuickLink.QuickLinkForMultiSelect(
          quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddOrRemove(contract.id),
          displayName = contract.currentAgreement.productVariant.displayName,
        )
      }
    }
    return QuickAction.MultiSelectQuickLink(
      titleRes = R.string.HC_QUICK_ACTIONS_EDIT_COINSURED,
      hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
      links = links,
    )
  } else if (this.size == 1) {
    val contract = this.first()
    return if (contract.coInsured?.any { it.hasMissingInfo } == true) {
      QuickAction.StandaloneQuickLink(
        quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkCoInsuredAddInfo(this.first().id),
        titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
        hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
      )
    } else {
      QuickAction.StandaloneQuickLink(
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
  }

  sealed interface InnerHelpCenterDestination : QuickLinkDestination {
    data class QuickLinkSickAbroad(
      val emergencyNumber: String?,
    ) : InnerHelpCenterDestination

    data class FirstVet(
      val sections: List<FirstVetSection>,
    ) : InnerHelpCenterDestination
  }
}
