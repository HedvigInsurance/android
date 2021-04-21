package com.hedvig.app.feature.changeaddress

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.MockActivity
import com.hedvig.app.R
import com.hedvig.app.changeAddressModule
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressViewModel
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.home.ui.changeaddress.ViewState
import com.hedvig.app.genericDevelopmentAdapter
import org.koin.android.viewmodel.dsl.viewModel
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
                    GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement(
                        address = GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement.Address(
                            "Test street 1 ",
                            postalCode = "Test 123 Postal code",
                            city = "City of Test"
                        ),
                        squareMeters = 123,
                        activeFrom = LocalDate.of(2021, 3, 15),
                        addressType = R.string.NORWEIGIAN_HOME_CONTENT_LOB_RENT
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
