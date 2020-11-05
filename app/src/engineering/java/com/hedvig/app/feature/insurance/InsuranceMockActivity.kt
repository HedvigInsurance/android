package com.hedvig.app.feature.insurance

import com.hedvig.app.MockActivity
import com.hedvig.app.MockContractDetailViewModel
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.SWEDISH_HOUSE
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.MockLoggedInViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.insuranceModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ACTIVE_AND_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_STUDENT
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_TRAVEL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

class InsuranceMockActivity : MockActivity() {
    override val original = listOf(
        loggedInModule,
        insuranceModule
    )
    override val mocks = listOf(
        module {
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<InsuranceViewModel> { MockInsuranceViewModel() }
            viewModel<ContractDetailViewModel> { MockContractDetailViewModel() }
        }
    )

    override fun adapter() = genericDevelopmentAdapter {
        header("Tab Screen")
        clickableItem("Active on and Terminated on") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_ACTIVE_AND_TERMINATED
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Student") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_STUDENT
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Renewal /w SE apartment") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_SWEDISH_APARTMENT
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Renewal /w SE house") {
            MockInsuranceViewModel.apply {
                insuranceMockData = SWEDISH_HOUSE
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("No Renewal /w SE apartment") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian travel and home contract") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian travel") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_TRAVEL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian home") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("One Active + One Terminated") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Norwegian home Error") {
            MockInsuranceViewModel.apply {
                insuranceMockData = NORWEGIAN_HOME_CONTENTS
                shouldError = true
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        header("Detail Screen")
        clickableItem("Swedish Apartment") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_SWEDISH_APARTMENT
            startActivity(ContractDetailActivity.newInstance(context, ""))
        }
        clickableItem("Swedish House") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_SWEDISH_HOUSE
            startActivity(ContractDetailActivity.newInstance(context, ""))
        }
        clickableItem("Norwegian Home Contents") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
            startActivity(ContractDetailActivity.newInstance(context, ""))
        }
        clickableItem("Norwegian Travel") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_NORWEGIAN_TRAVEL
            startActivity(ContractDetailActivity.newInstance(context, ""))
        }
        clickableItem("Swedish Apartment Error") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_SWEDISH_APARTMENT
                shouldError = true
            }
            startActivity(ContractDetailActivity.newInstance(context, ""))
        }
        header("Terminated Contracts-Screen")
        clickableItem("One Active + One Terminated") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
                shouldError = false
            }
            startActivity(TerminatedContractsActivity.newInstance(context))
        }
        clickableItem("Error") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
                shouldError = true
            }
            startActivity(TerminatedContractsActivity.newInstance(context))
        }
    }
}
