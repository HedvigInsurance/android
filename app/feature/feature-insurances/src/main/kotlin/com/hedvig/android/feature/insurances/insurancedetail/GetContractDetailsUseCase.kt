package com.hedvig.android.feature.insurances.insurancedetail

import android.net.Uri
import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.fx.coroutines.parZip
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.feature.insurances.data.toContractType
import com.hedvig.android.feature.insurances.insurancedetail.coverage.ContractCoverage
import com.hedvig.android.feature.insurances.insurancedetail.coverage.GetContractCoverageUseCase
import com.hedvig.android.hanalytics.featureflags.FeatureManager
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.language.LanguageService
import giraffe.InsuranceQuery
import giraffe.type.TypeOfContract
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

internal class GetContractDetailsUseCase(
  private val apolloClient: ApolloClient,
  private val getContractCoverageUseCase: GetContractCoverageUseCase,
  private val languageService: LanguageService,
  private val featureManager: FeatureManager,
) {
  suspend fun invoke(contractId: String): Either<ContractDetailError, ContractDetails> {
    return either {
      parZip(
        {
          val data = apolloClient
            .query(InsuranceQuery(languageService.getGraphQLLocale()))
            .safeExecute()
            .toEither(ContractDetailError.NetworkError)
            .bind()
          val contract = data.contracts.firstOrNull { it.id == contractId }
          ensureNotNull(contract) { ContractDetailError.ContractNotFoundError }
          contract
        },
        {
          getContractCoverageUseCase.invoke(contractId).mapLeft { ContractDetailError.NetworkError }.bind()
        },
        {
          featureManager.isFeatureEnabled(Feature.TERMINATION_FLOW)
        },
      ) {
          contract: InsuranceQuery.Contract,
          contractCoverage: ContractCoverage,
          isTerminationFlowEnabled: Boolean,
        ->

        val isContractTerminated = run {
          val status = contract.fragments.upcomingAgreementFragment.status
          val isTerminatedInTheFuture = status.asTerminatedInFutureStatus != null
          val isTerminatedToday = status.asTerminatedTodayStatus != null
          isTerminatedInTheFuture || isTerminatedToday
        }
        val cancelInsuranceData = if (isTerminationFlowEnabled && !isContractTerminated) {
          ContractDetails.CancelInsuranceData(contract.id, contract.displayName)
        } else {
          null
        }

        ContractDetails(
          contractCardData = ContractDetails.ContractCardData(
            contractId = contract.id,
            backgroundImageUrl = null, // Fill when we get image from backend
            chips = contract.statusPills.toPersistentList(),
            title = contract.displayName,
            subtitle = contract.detailPills.joinToString(" ∙ "),
            contractType = contract.typeOfContract.toContractType(),
          ),
          overviewItems = contract.currentAgreementDetailsTable.fragments.tableFragment.sections.flatMap {
            it.rows.map { it.title to it.value }
          }.toPersistentList(),
          cancelInsuranceData = cancelInsuranceData,
          allowEditCoInsured = contract.typeOfContract.canChangeCoInsured(),
          insurableLimits = contractCoverage.insurableLimits,
          perils = contractCoverage.contractPerils,
          documents = listOfNotNull(
            contract.currentAgreement?.asAgreementCore?.certificateUrl?.let { certificateUrl ->
              ContractDetails.Document.InsuranceCertificate(Uri.parse(certificateUrl))
            },
            ContractDetails.Document.TermsAndConditions(Uri.parse(contract.termsAndConditions.url)),
          ).toPersistentList(),
        )
      }
    }
  }
}

internal sealed class ContractDetailError {
  object NetworkError : ContractDetailError()
  object ContractNotFoundError : ContractDetailError()
}

internal data class ContractDetails(
  val contractCardData: ContractCardData,
  val overviewItems: ImmutableList<Pair<String, String>>,
  val cancelInsuranceData: CancelInsuranceData?,
  val allowEditCoInsured: Boolean,
  val insurableLimits: ImmutableList<ContractCoverage.InsurableLimit>,
  val perils: ImmutableList<ContractCoverage.Peril>,
  val documents: ImmutableList<Document>,
) {
  data class ContractCardData(
    val contractId: String,
    val backgroundImageUrl: String?,
    val chips: ImmutableList<String>,
    val title: String,
    val subtitle: String,
    val contractType: com.hedvig.android.core.ui.insurance.ContractType,
  )

  data class CancelInsuranceData(
    val insuranceId: String,
    val insuranceDisplayName: String,
  )

  sealed interface Document {
    val uri: Uri

    data class InsuranceCertificate(override val uri: Uri) : Document
    data class TermsAndConditions(override val uri: Uri) : Document
  }
}

private fun TypeOfContract.canChangeCoInsured() = when (this) {
  TypeOfContract.SE_HOUSE,
  TypeOfContract.SE_APARTMENT_BRF,
  TypeOfContract.SE_APARTMENT_RENT,
  TypeOfContract.SE_APARTMENT_STUDENT_BRF,
  TypeOfContract.SE_APARTMENT_STUDENT_RENT,
  TypeOfContract.SE_ACCIDENT,
  TypeOfContract.SE_ACCIDENT_STUDENT,
  TypeOfContract.NO_HOUSE,
  TypeOfContract.NO_HOME_CONTENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_RENT,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN,
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.NO_TRAVEL,
  TypeOfContract.NO_TRAVEL_YOUTH,
  TypeOfContract.NO_TRAVEL_STUDENT,
  TypeOfContract.NO_ACCIDENT,
  TypeOfContract.DK_HOME_CONTENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_RENT,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN,
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT,
  TypeOfContract.DK_HOUSE,
  TypeOfContract.DK_ACCIDENT,
  TypeOfContract.DK_ACCIDENT_STUDENT,
  TypeOfContract.DK_TRAVEL,
  TypeOfContract.DK_TRAVEL_STUDENT,
  -> true
  TypeOfContract.SE_CAR_TRAFFIC,
  TypeOfContract.SE_CAR_HALF,
  TypeOfContract.SE_CAR_FULL,
  TypeOfContract.SE_GROUP_APARTMENT_RENT,
  TypeOfContract.SE_GROUP_APARTMENT_BRF,
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL,
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL,
  TypeOfContract.SE_DOG_BASIC,
  TypeOfContract.SE_DOG_STANDARD,
  TypeOfContract.SE_DOG_PREMIUM,
  TypeOfContract.SE_CAT_BASIC,
  TypeOfContract.SE_CAT_STANDARD,
  TypeOfContract.SE_CAT_PREMIUM,
  is TypeOfContract.UNKNOWN__,
  -> false
}
