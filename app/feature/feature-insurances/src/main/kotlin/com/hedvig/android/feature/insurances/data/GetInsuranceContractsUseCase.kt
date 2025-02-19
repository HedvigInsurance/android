package com.hedvig.android.feature.insurances.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.formatName
import com.hedvig.android.core.common.formatSsn
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import octopus.InsuranceContractsQuery
import octopus.fragment.ContractFragment
import octopus.type.AgreementCreationCause

internal interface GetInsuranceContractsUseCase {
  fun invoke(): Flow<Either<ErrorMessage, List<InsuranceContract>>>
}

internal class GetInsuranceContractsUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetInsuranceContractsUseCase {
  override fun invoke(): Flow<Either<ErrorMessage, List<InsuranceContract>>> {
    return combine(
      featureManager.isFeatureEnabled(Feature.TRAVEL_ADDON).flatMapLatest { areAddonsEnabled ->
        flow {
          while (currentCoroutineContext().isActive) {
            emitAll(
              apolloClient
                .query(InsuranceContractsQuery(areAddonsEnabled))
                .fetchPolicy(FetchPolicy.CacheAndNetwork)
                .safeFlow(::ErrorMessage),
            )
            delay(3.seconds)
          }
        }
      },
      featureManager.isFeatureEnabled(Feature.EDIT_COINSURED),
      featureManager.isFeatureEnabled(Feature.MOVING_FLOW),
    ) { insuranceQueryResponse, isEditCoInsuredEnabled, isMovingFlowFlagEnabled ->
      either {
        val insuranceQueryData = insuranceQueryResponse.bind()
        val contractHolderDisplayName = insuranceQueryData.getContractHolderDisplayName()
        val contractHolderSSN = insuranceQueryData.currentMember.ssn?.let { formatSsn(it) }
        val isMovingEnabledForMember =
          insuranceQueryData.currentMember.memberActions?.isMovingEnabled == true && isMovingFlowFlagEnabled
        val terminatedContracts = insuranceQueryData.currentMember.terminatedContracts.map {
          it.toContract(
            isTerminated = true,
            contractHolderDisplayName = contractHolderDisplayName,
            contractHolderSSN = contractHolderSSN,
            isEditCoInsuredEnabled = isEditCoInsuredEnabled,
            isMovingFlowEnabled = isMovingEnabledForMember,
          )
        }

        val activeContracts = insuranceQueryData.currentMember.activeContracts.map {
          it.toContract(
            isTerminated = false,
            contractHolderDisplayName = contractHolderDisplayName,
            contractHolderSSN = contractHolderSSN,
            isEditCoInsuredEnabled = isEditCoInsuredEnabled,
            isMovingFlowEnabled = isMovingEnabledForMember,
          )
        }
        terminatedContracts + activeContracts
      }
    }
  }
}

private fun InsuranceContractsQuery.Data.getContractHolderDisplayName(): String = formatName(
  currentMember.firstName,
  currentMember.lastName,
)

private fun ContractFragment.toContract(
  isTerminated: Boolean,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
  isEditCoInsuredEnabled: Boolean,
  isMovingFlowEnabled: Boolean,
): InsuranceContract {
  return InsuranceContract(
    id = id,
    tierName = currentAgreement.productVariant.displayNameTier,
    displayName = currentAgreement.productVariant.displayName,
    contractHolderDisplayName = contractHolderDisplayName,
    contractHolderSSN = contractHolderSSN,
    exposureDisplayName = exposureDisplayName,
    inceptionDate = masterInceptionDate,
    renewalDate = upcomingChangedAgreement?.activeFrom,
    terminationDate = terminationDate,
    currentInsuranceAgreement = InsuranceAgreement(
      activeFrom = currentAgreement.activeFrom,
      activeTo = currentAgreement.activeTo,
      displayItems = currentAgreement.displayItems.map {
        InsuranceAgreement.DisplayItem(
          it.displayTitle,
          it.displayValue,
        )
      },
      productVariant = currentAgreement.productVariant.toProductVariant(),
      certificateUrl = currentAgreement.certificateUrl,
      coInsured = coInsured?.map { it.toCoInsured() } ?: listOf(),
      creationCause = currentAgreement.creationCause.toCreationCause(),
      addons = currentAgreement.addons?.map {
        Addon(it.addonVariant.toAddonVariant())
      },
    ),
    upcomingInsuranceAgreement = upcomingChangedAgreement?.let {
      InsuranceAgreement(
        activeFrom = it.activeFrom,
        activeTo = it.activeTo,
        displayItems = it.displayItems.map {
          InsuranceAgreement.DisplayItem(
            it.displayTitle,
            it.displayValue,
          )
        },
        productVariant = it.productVariant.toProductVariant(),
        certificateUrl = it.certificateUrl,
        coInsured = coInsured?.map { it.toCoInsured() } ?: listOf(),
        creationCause = it.creationCause.toCreationCause(),
        addons = it.addons?.map { addon ->
          Addon(addon.addonVariant.toAddonVariant())
        },
      )
    },
    supportsAddressChange = supportsMoving && isMovingFlowEnabled,
    supportsEditCoInsured = supportsCoInsured && isEditCoInsuredEnabled,
    isTerminated = isTerminated,
    supportsTierChange = supportsChangeTier,
  )
}

private fun AgreementCreationCause.toCreationCause() = when (this) {
  AgreementCreationCause.NEW_CONTRACT -> InsuranceAgreement.CreationCause.NEW_CONTRACT
  AgreementCreationCause.RENEWAL -> InsuranceAgreement.CreationCause.RENEWAL
  AgreementCreationCause.MIDTERM_CHANGE -> InsuranceAgreement.CreationCause.MIDTERM_CHANGE
  AgreementCreationCause.UNKNOWN,
  AgreementCreationCause.UNKNOWN__,
  -> InsuranceAgreement.CreationCause.UNKNOWN
}

private fun ContractFragment.CoInsured.toCoInsured(): InsuranceAgreement.CoInsured = InsuranceAgreement.CoInsured(
  firstName = firstName,
  lastName = lastName,
  ssn = ssn,
  birthDate = birthdate,
  activatesOn = activatesOn,
  terminatesOn = terminatesOn,
  hasMissingInfo = hasMissingInfo,
)
