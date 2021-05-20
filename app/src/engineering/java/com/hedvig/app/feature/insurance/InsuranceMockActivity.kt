package com.hedvig.app.feature.insurance

import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.MockActivity
import com.hedvig.app.MockContractDetailViewModel
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.tab.InsuranceViewModel
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.feature.loggedin.service.TabNotificationService
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.insuranceModule
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ACTIVE_AND_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_DANISH_ACCIDENT
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_DANISH_HOME_CONTENTS
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_STUDENT
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_TERMINATED
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_WITH_CROSS_SELL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_DANISH_TRAVEL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_MULTIPLE_DANISH_CONTRACTS
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_TRAVEL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_PENDING_CONTRACT
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT_NO_RENEWAL
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class InsuranceMockActivity : MockActivity() {
    private val dataStore: DataStore<Preferences> by inject()
    override val original = listOf(
        insuranceModule
    )
    override val mocks = listOf(
        module {
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
                insuranceMockData = INSURANCE_DATA_SWEDISH_HOUSE
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
                insuranceMockData = InsuranceQuery.Data(
                    contracts = listOf(
                        INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0],
                        INSURANCE_DATA_NORWEGIAN_TRAVEL.contracts[0],
                    ),
                    activeContractBundles = emptyList(),
                )
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
                insuranceMockData = INSURANCE_DATA_NORWEGIAN_TRAVEL
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
                insuranceMockData = INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
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
        clickableItem("Terminated") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_TERMINATED
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
                insuranceMockData = INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
                shouldError = true
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Danish Home Contents") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_DANISH_HOME_CONTENTS
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Danish Travel") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_DANISH_TRAVEL
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Danish Accident") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_DANISH_ACCIDENT
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        clickableItem("Danish Home + Danish Travel + Danish Accident") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_MULTIPLE_DANISH_CONTRACTS
                shouldError = false
            }
            startActivity(
                LoggedInActivity.newInstance(
                    context,
                    initialTab = LoggedInTabs.INSURANCE
                )
            )
        }
        header("Cross-Sells")
        clickableItem("Cross-Sell") {
            MockInsuranceViewModel.apply {
                insuranceMockData = INSURANCE_DATA_WITH_CROSS_SELL
                shouldError = false
            }
            startActivity(LoggedInActivity.newInstance(context, initialTab = LoggedInTabs.INSURANCE))
        }
        clickableItem("Reset cross-sell tab notification") {
            lifecycleScope.launch {
                dataStore.edit { preferences ->
                    preferences[TabNotificationService.SEEN_CROSS_SELLS_KEY] = emptySet()
                }
            }
        }
        header("Detail Screen")
        clickableItem("Swedish Apartment") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_SWEDISH_APARTMENT
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_SWEDISH_APARTMENT.contracts.first().id)
            )
        }
        clickableItem("Swedish House") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_SWEDISH_HOUSE
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_SWEDISH_HOUSE.contracts.first().id)
            )
        }
        clickableItem("Norwegian Home Contents") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts.first().id)
            )
        }
        clickableItem("Norwegian Travel") {
            MockContractDetailViewModel.mockData = INSURANCE_DATA_NORWEGIAN_TRAVEL
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_NORWEGIAN_TRAVEL.contracts.first().id)
            )
        }
        clickableItem("Swedish Apartment Error") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_SWEDISH_APARTMENT
                shouldError = true
            }
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_SWEDISH_APARTMENT.contracts.first().id)
            )
        }
        clickableItem("Danish Home Contents") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_DANISH_HOME_CONTENTS
                shouldError = false
            }
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_DANISH_HOME_CONTENTS.contracts.first().id)
            )
        }
        clickableItem("Danish Travel") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_DANISH_TRAVEL
                shouldError = false
            }
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_DANISH_TRAVEL.contracts.first().id)
            )
        }
        clickableItem("Danish Accident") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_DANISH_ACCIDENT
                shouldError = false
            }
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_DANISH_ACCIDENT.contracts.first().id)
            )
        }
        clickableItem("Pending contract") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_PENDING_CONTRACT
                shouldError = false
            }
            startActivity(
                ContractDetailActivity.newInstance(context, INSURANCE_DATA_PENDING_CONTRACT.contracts.first().id)
            )
        }
        clickableItem("Terminated contract") {
            MockContractDetailViewModel.apply {
                mockData = INSURANCE_DATA_TERMINATED
                shouldError = false
            }
            startActivity(ContractDetailActivity.newInstance(context, INSURANCE_DATA_TERMINATED.contracts.first().id))
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
        header("Deep Links")
        clickableItem("`/insurance`-Deep Link") {
            startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data =
                        Uri.parse("https://${getString(R.string.FIREBASE_LINK_DOMAIN)}/insurance")
                }
            )
        }
    }
}
