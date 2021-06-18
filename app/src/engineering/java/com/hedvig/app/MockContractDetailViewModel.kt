package com.hedvig.app

import android.net.Uri
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageModel
import com.hedvig.app.feature.insurance.ui.detail.toModelItems
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.table.Table
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
                _yourInfoList.value = listOf(
                    YourInfoModel.PendingAddressChange(
                        upcomingAgreement = GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement(
                            activeFrom = LocalDate.of(2021, 1, 30),
                            address = "Test Address 12",
                            table = Table(
                                title = "Mock Upcoming Agreement",
                                sections = listOf(
                                    Table.Section(
                                        title = "Details",
                                        rows = listOf(
                                            Table.Row(
                                                title = "Address",
                                                value = "Test Address 12",
                                                subtitle = null
                                            ),
                                            Table.Row(
                                                title = "Postal code",
                                                value = "11234",
                                                subtitle = null
                                            ),
                                            Table.Row(
                                                title = "City",
                                                value = "Test city",
                                                subtitle = null
                                            )
                                        )
                                    ),
                                    Table.Section(
                                        title = "Extra buildings",
                                        rows = listOf(
                                            Table.Row(
                                                title = "Garage",
                                                value = "22 sqm",
                                                subtitle = null
                                            ),
                                            Table.Row(
                                                title = "Attefall",
                                                value = "15 sqm",
                                                subtitle = null
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ) + mockData.contracts[0].toModelItems()

                _documentsList.value = listOfNotNull(
                    it.currentAgreement.asAgreementCore?.certificateUrl?.let {
                        DocumentItems.Document(
                            titleRes = R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
                            subTitleRes = R.string.insurance_details_view_documents_full_terms_subtitle,
                            uri = Uri.parse(it)
                        )
                    },
                    it.termsAndConditions.url.let {
                        DocumentItems.Document(
                            titleRes = R.string.MY_DOCUMENTS_INSURANCE_TERMS,
                            subTitleRes = R.string.insurance_details_view_documents_insurance_letter_subtitle,
                            uri = Uri.parse(it)
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
