package com.hedvig.app

import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageModel
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsModel
import com.hedvig.app.feature.insurance.ui.detail.toModelItems
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT
import java.time.LocalDate

class MockContractDetailViewModel : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        if (shouldError) {
            shouldError = false
            _data.postValue(Result.failure(Error()))
            return
        } else {
            val contract = mockData.contracts.find { it.id == id }
            contract?.let {
                _data.value = Result.success(it)
                _yourInfoList.value = listOf(YourInfoModel.PendingAddressChange(
                    upcomingAgreement = UpcomingAgreement(
                        address = UpcomingAgreement.Address(
                            street = "Test Address 12",
                            postalCode = "11234",
                            city = "Test City"
                        ),
                        squareMeters = 123,
                        activeFrom = LocalDate.of(2021, 4, 8),
                        addressType = R.string.SWEDISH_APARTMENT_LOB_RENT,
                        nrOfCoInsured = 2,
                        yearBuilt = 2010,
                        numberOfBaths = 2,
                        partlySubleted = true,
                        ancillaryArea = 32,
                        extraBuildings = listOf(
                            UpcomingAgreement.Building(
                                name = "Garage",
                                area = 22,
                                hasWaterConnected = true
                            ),
                            UpcomingAgreement.Building(
                                name = "Attefall",
                                area = 15,
                                hasWaterConnected = false
                            )
                        )
                    )
                )) + mockData.contracts[0].toModelItems()

                _documentsList.value = listOfNotNull(
                    it.currentAgreement.asAgreementCore?.certificateUrl?.let {
                        DocumentsModel(
                            R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
                            R.string.insurance_details_view_documents_full_terms_subtitle,
                            it
                        )
                    },
                    it.termsAndConditions.url.let {
                        DocumentsModel(
                            R.string.MY_DOCUMENTS_INSURANCE_TERMS,
                            R.string.insurance_details_view_documents_insurance_letter_subtitle,
                            it
                        )
                    }
                )

                _coverageList.value = listOf(
                    CoverageModel.Header.Perils(
                        it.typeOfContract
                    )
                ) + it.perils.map {
                    CoverageModel.Peril(
                        it.fragments.perilFragment
                    )
                } + CoverageModel.Header.InsurableLimits + it.insurableLimits.map {
                    CoverageModel.InsurableLimit(
                        it.fragments.insurableLimitsFragment
                    )
                }
            }
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
