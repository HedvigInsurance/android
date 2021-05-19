package com.hedvig.app.feature.changeaddress

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.MockActivity
import com.hedvig.app.changeAddressModule
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModel
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement.*
import com.hedvig.app.feature.home.ui.changeaddress.ViewState
import com.hedvig.app.genericDevelopmentAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.time.LocalDate

class ChangeAddressMockActivity : MockActivity() {
    override val original = listOf(changeAddressModule)
    override val mocks = listOf(
        module { viewModel<ChangeAddressViewModel> { MockChangeAddressViewModel() } }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Change address")
        clickableItem("Eligible") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(ViewState.SelfChangeAddress("embarkTestId"))
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Error") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(
                ViewState.SelfChangeError(
                    GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult.Error(
                        message = "Test error message, this can happen because of no internet connection, backend errors etc."
                    )
                )
            )
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Blocked (manual change, chat)") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(ViewState.ManualChangeAddress)
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Upcoming address change") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(
                ViewState.ChangeAddressInProgress(
                    UpcomingAgreement(
                        activeFrom = LocalDate.of(2021, 1, 13),
                        address = "Test Address 12",
                        table = UpcomingAgreementTable(
                            title = "Mock Upcoming Agreement",
                            sections = listOf(
                                UpcomingAgreementTable.Section(
                                    title = "Details",
                                    rows = listOf(
                                        UpcomingAgreementTable.Row(
                                            title = "Address",
                                            value = "Test Address 12",
                                            subTitle = null
                                        ),
                                        UpcomingAgreementTable.Row(
                                            title = "Postal code",
                                            value = "11234",
                                            subTitle = null
                                        ),
                                        UpcomingAgreementTable.Row(
                                            title = "City",
                                            value = "Test city",
                                            subTitle = null
                                        )
                                    )
                                ),
                                UpcomingAgreementTable.Section(
                                    title = "Extra buildings",
                                    rows = listOf(
                                        UpcomingAgreementTable.Row(
                                            title = "Garage",
                                            value = "22 sqm",
                                            subTitle = null
                                        ),
                                        UpcomingAgreementTable.Row(
                                            title = "Attefall",
                                            value = "15 sqm",
                                            subTitle = null
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Loading") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(ViewState.Loading)
            startActivity(ChangeAddressActivity.newInstance(context))
        }
    }
}
