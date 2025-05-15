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
import com.hedvig.android.data.display.items.DisplayItem
import com.hedvig.android.data.productvariant.toAddonVariant
import com.hedvig.android.data.productvariant.toProductVariant
import com.hedvig.android.feature.insurances.data.InsuranceContract.EstablishedInsuranceContract
import com.hedvig.android.feature.insurances.data.InsuranceContract.PendingInsuranceContract
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
import octopus.fragment.AgreementDisplayItemFragment
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

        val pendingContracts = insuranceQueryData.currentMember.pendingContracts.map {
          it.toPendingContract(
            contractHolderDisplayName = contractHolderDisplayName,
            contractHolderSSN = contractHolderSSN,
          )
        }
        terminatedContracts + activeContracts + pendingContracts
      }
    }
  }
}

private fun InsuranceContractsQuery.Data.getContractHolderDisplayName(): String = formatName(
  currentMember.firstName,
  currentMember.lastName,
)

private fun InsuranceContractsQuery.Data.CurrentMember.PendingContract.toPendingContract(
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
): PendingInsuranceContract {
  return PendingInsuranceContract(
    id = this.id,
    tierName = this.productVariant.displayNameTier,
    displayName = this.productVariant.displayName,
    backgroundImageUrl = this.id.backgroundImageUrl(),
    contractHolderDisplayName = contractHolderDisplayName,
    contractHolderSSN = contractHolderSSN,
    exposureDisplayName = exposureDisplayName,
    productVariant = this.productVariant.toProductVariant(),
    displayItems = this.displayItems.map { it.toDisplayItem() },
  )
}

private fun ContractFragment.toContract(
  isTerminated: Boolean,
  contractHolderDisplayName: String,
  contractHolderSSN: String?,
  isEditCoInsuredEnabled: Boolean,
  isMovingFlowEnabled: Boolean,
): EstablishedInsuranceContract {
  return EstablishedInsuranceContract(
    id = id,
    tierName = currentAgreement.productVariant.displayNameTier,
    displayName = currentAgreement.productVariant.displayName,
    backgroundImageUrl = this.id.backgroundImageUrl(),
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
        it.toDisplayItem()
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
        displayItems = it.displayItems.map { it.toDisplayItem() },
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

private fun String.backgroundImageUrl(): String? = when (this) {
  "25ab525c-746f-4f96-8087-e8213d28ab97" -> {
    @Suppress("ktlint:standard:max-line-length")
    """https://sdmntprpolandcentral.oaiusercontent.com/files/00000000-c7b8-620a-9870-2a0e979c07fe/raw?se=2025-05-15T17%3A44%3A04Z&sp=r&sv=2024-08-04&sr=b&scid=00000000-0000-0000-0000-000000000000&skoid=eb780365-537d-4279-a878-cae64e33aa9c&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-05-15T16%3A17%3A51Z&ske=2025-05-16T16%3A17%3A51Z&sks=b&skv=2024-08-04&sig=xSXxr21lU9Ee3I1BWem1LmmSJzDZHVUCkByCH0Ht38Q%3D"""
  }
  "ab70963c-beb7-4150-b67d-8f8ea849c51d" -> {
    @Suppress("ktlint:standard:max-line-length")
    """https://sdmntprpolandcentral.oaiusercontent.com/files/00000000-9ff8-620a-b4e4-4e0374363e38/raw?se=2025-05-15T17%3A44%3A04Z&sp=r&sv=2024-08-04&sr=b&scid=00000000-0000-0000-0000-000000000000&skoid=eb780365-537d-4279-a878-cae64e33aa9c&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-05-15T08%3A07%3A49Z&ske=2025-05-16T08%3A07%3A49Z&sks=b&skv=2024-08-04&sig=1HaJRlD4f2jGRnzO0YNd488adiyFX1usych60Zu6tlE%3D"""
  }
  else -> {
    null
  }
}

private fun AgreementDisplayItemFragment.toDisplayItem(): DisplayItem {
  return DisplayItem.fromStrings(displayTitle, displayValue)
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
