package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.termination.data.GetTerminatableContractsUseCase
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import hedvig.resources.R
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import octopus.AvailableSelfServiceOnContractsQuery

internal class GetQuickLinksUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
  private val getTerminatableContractsUseCase: GetTerminatableContractsUseCase,
) {
  suspend fun invoke(): Either<ErrorMessage, PersistentList<QuickAction>> = either {
    val contracts = apolloClient.query(AvailableSelfServiceOnContractsQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .onLeft { logcat(LogPriority.ERROR) { it.message ?: "Could not fetch common claims" } }
      .bind()
      .currentMember
      .activeContracts

    buildList {
      contracts
        .filter { it.supportsCoInsured }
        .takeIf { featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).first() }
        ?.let {
          if (it.size > 1) {
            val links = it.map { contract ->
              if (contract.coInsured?.any { it.hasMissingInfo } == true) {
                QuickAction.MultiSelectQuickLink.QuickLinkForMultiSelect(
                  quickLinkDestination = QuickLinkDestination.QuickLinkCoInsuredAddInfo(contract.id),
                  displayName = contract.currentAgreement.productVariant.displayName,
                )
              } else {
                QuickAction.MultiSelectQuickLink.QuickLinkForMultiSelect(
                  quickLinkDestination = QuickLinkDestination.QuickLinkCoInsuredAddOrRemove(contract.id),
                  displayName = contract.currentAgreement.productVariant.displayName,
                )
              }
            }

            add(
              QuickAction.MultiSelectQuickLink(
                titleRes = R.string.HC_QUICK_ACTIONS_EDIT_COINSURED,
                hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                links = links,
              ),
            )
          } else if (it.isNotEmpty()) {
            val contract = it.first()
            if (contract.coInsured?.any { it.hasMissingInfo } == true) {
              add(
                QuickAction.StandaloneQuickLink(
                  quickLinkDestination = QuickLinkDestination.QuickLinkCoInsuredAddInfo(it.first().id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                ),
              )
            } else {
              add(
                QuickAction.StandaloneQuickLink(
                  quickLinkDestination = QuickLinkDestination.QuickLinkCoInsuredAddOrRemove(it.first().id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                ),
              )
            }
          } else {
          }
        }

      contracts
        .takeIf { getTerminatableContractsUseCase.invoke().first().getOrNull() != null }
        ?.let {
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.QuickLinkTermination,
              titleRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_TITLE,
              hintTextRes = R.string.HC_QUICK_ACTIONS_CANCELLATION_SUBTITLE,
            ),
          )
        }

      contracts
        .firstOrNull { it.supportsMoving }
        .takeIf { featureManager.isFeatureEnabled(Feature.MOVING_FLOW).first() }
        ?.let {
          add(
            QuickAction.StandaloneQuickLink(
              quickLinkDestination = QuickLinkDestination.QuickLinkChangeAddress,
              titleRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
              hintTextRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
            ),
          )
        }

      val travelCertificateAvailable = checkTravelCertificateDestinationAvailabilityUseCase.invoke().isRight()
      if (travelCertificateAvailable) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.QuickLinkTravelCertificate,
            titleRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
          ),
        )
      }

      if (featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN).first()) {
        add(
          QuickAction.StandaloneQuickLink(
            quickLinkDestination = QuickLinkDestination.QuickLinkConnectPayment,
            titleRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
          ),
        )
      }
    }.toPersistentList()
  }
}

sealed interface QuickLinkDestination {
  data class QuickLinkCoInsuredAddInfo(val contractId: String) : QuickLinkDestination

  data class QuickLinkCoInsuredAddOrRemove(val contractId: String) : QuickLinkDestination

  data object QuickLinkTermination : QuickLinkDestination

  data object QuickLinkTravelCertificate : QuickLinkDestination

  data object QuickLinkChangeAddress : QuickLinkDestination

  data object QuickLinkConnectPayment : QuickLinkDestination
}
