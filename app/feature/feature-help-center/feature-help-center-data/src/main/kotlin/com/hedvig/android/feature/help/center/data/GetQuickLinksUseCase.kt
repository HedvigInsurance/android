package com.hedvig.android.feature.help.center.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.travelcertificate.CheckTravelCertificateDestinationAvailabilityUseCase
import com.hedvig.android.feature.help.center.model.QuickAction
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.core.AppDestination
import hedvig.resources.R
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.first
import octopus.AvailableSelfServiceOnContractsQuery

class GetQuickLinksUseCase(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
  private val checkTravelCertificateDestinationAvailabilityUseCase:
    CheckTravelCertificateDestinationAvailabilityUseCase,
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
                QuickAction.QuickLink(
                  destination = AppDestination.CoInsuredAddInfo(contract.id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                  displayName = contract.currentAgreement.productVariant.displayName,
                )
              } else {
                QuickAction.QuickLink(
                  destination = AppDestination.CoInsuredAddOrRemove(contract.id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
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
                QuickAction.QuickLink(
                  destination = AppDestination.CoInsuredAddInfo(it.first().id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                  displayName = contract.currentAgreement.productVariant.displayName,
                ),
              )
            } else {
              add(
                QuickAction.QuickLink(
                  destination = AppDestination.CoInsuredAddOrRemove(it.first().id),
                  titleRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_TITLE,
                  hintTextRes = R.string.HC_QUICK_ACTIONS_CO_INSURED_SUBTITLE,
                  displayName = contract.currentAgreement.productVariant.displayName,
                ),
              )
            }
          } else {
          }
        }

      contracts
        .firstOrNull { it.supportsMoving }
        .takeIf { featureManager.isFeatureEnabled(Feature.MOVING_FLOW).first() }
        ?.let {
          add(
            QuickAction.QuickLink(
              destination = AppDestination.ChangeAddress,
              titleRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
              hintTextRes = R.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
              displayName = null,
            ),
          )
        }

      val travelCertificateAvailable = checkTravelCertificateDestinationAvailabilityUseCase.invoke().isRight()
      if (travelCertificateAvailable) {
        add(
          QuickAction.QuickLink(
            destination = AppDestination.TravelCertificate,
            titleRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_TRAVEL_CERTIFICATE_SUBTITLE,
            displayName = null,
          ),
        )
      }

      if (featureManager.isFeatureEnabled(Feature.PAYMENT_SCREEN).first()) {
        add(
          QuickAction.QuickLink(
            destination = AppDestination.ConnectPayment,
            titleRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
            hintTextRes = R.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
            displayName = null,
          ),
        )
      }
    }.toPersistentList()
  }
}
