package com.hedvig.app

import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
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
            mockData.contracts.find { it.id == id }?.let { _data.value = Result.success(it) } ?: run {
                val list = yourInfoListItemBuilder.createYourInfoList(
                    contract = mockData.contracts[0],
                    upcomingAgreementResult = YourInfoModel.PendingAddressChange(
                        upcomingAgreement = GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement(
                            address = GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement.Address(
                                street = "Test Address 12",
                                postalCode = "11234",
                                city = "Test City"
                            ),
                            squareMeters = 123,
                            activeFrom = LocalDate.of(2021, 4, 8),
                            addressType = R.string.SWEDISH_APARTMENT_LOB_RENT,
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
                )
                _yourInfoList.value = list
            }
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
