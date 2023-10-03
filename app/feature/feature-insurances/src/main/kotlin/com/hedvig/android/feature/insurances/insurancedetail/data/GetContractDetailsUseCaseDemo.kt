package com.hedvig.android.feature.insurances.insurancedetail.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.ui.insurance.ContractType
import com.hedvig.android.feature.insurances.insurancedetail.coverage.ContractCoverage
import kotlinx.collections.immutable.persistentListOf

internal class GetContractDetailsUseCaseDemo : GetContractDetailsUseCase {
  override suspend fun invoke(contractId: String): Either<ContractDetailError, ContractDetails> {
    return either {
      ContractDetails(
        contractCardData = ContractDetails.ContractCardData(
          contractId = "1",
          backgroundImageUrl = null,
          chips = persistentListOf("Active"),
          title = "Home Insurance",
          subtitle = "Road 10",
          contractType = ContractType.HOUSE,
        ),
        overviewItems = persistentListOf(
          "Street" to "Road 10",
          "Postal code" to "12345",
          "Insured people" to "You + 2",
        ),
        upcomingChanges = null,
        cancelInsuranceData = null,
        allowChangeAddress = false,
        allowEditCoInsured = false,
        insurableLimits = persistentListOf(
          ContractCoverage.InsurableLimit(
            "Your things are insured at",
            "1 000 000 SEK",
            "All your posessions are together insured up to 1 million SEK",
          ),
          ContractCoverage.InsurableLimit(
            "The deductible is",
            "1 500 SEK",
            "Deductible is the amount you have to pay yourself in the event of a damage",
          ),
          ContractCoverage.InsurableLimit(
            "Travel insurance",
            "45 days",
            "Travel insurance covers you on your trip",
          ),
        ),
        perils = persistentListOf(
          ContractCoverage.Peril("1", "Fire", "We will cover fire damage", persistentListOf(), 0xFFFF0000),
          ContractCoverage.Peril(
            "2",
            "Water leaks",
            "We cover different types of water damages",
            persistentListOf(),
            0xFF4040FF,
          ),
        ),
        documents = persistentListOf(ContractDetails.Document.TermsAndConditions("", "Insurance Certificate")),
      )
    }
  }
}
