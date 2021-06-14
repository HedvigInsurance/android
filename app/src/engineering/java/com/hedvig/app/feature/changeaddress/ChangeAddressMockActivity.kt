package com.hedvig.app.feature.changeaddress

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.MockActivity
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement
import com.hedvig.app.feature.home.ui.changeaddress.ViewState
import com.hedvig.app.feature.home.ui.changeaddress.result.ChangeAddressResultActivity
import com.hedvig.app.feature.table.Table
import com.hedvig.app.genericDevelopmentAdapter
import java.time.LocalDate
import org.koin.core.module.Module

class ChangeAddressMockActivity : MockActivity() {
    override val original = emptyList<Module>()
    override val mocks = emptyList<Module>()

    override fun adapter() = genericDevelopmentAdapter {
        header("Change address")
        clickableItem("Eligible") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(ViewState.SelfChangeAddress("embarkTestId"))
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Error") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(
                ViewState.SelfChangeError(
                    GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Error(
                        message = "Test error message, this can happen" +
                            " because of no internet connection, backend errors etc."
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
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        clickableItem("Loading") {
            MockChangeAddressViewModel.mockedState = MutableLiveData(ViewState.Loading)
            startActivity(ChangeAddressActivity.newInstance(context))
        }
        header("Result Screen")
        clickableItem("Success") {
            startActivity(
                ChangeAddressResultActivity.newInstance(
                    context,
                    ChangeAddressResultActivity.Result.Success(LocalDate.of(2021, 2, 21))
                )
            )
        }
    }
}
