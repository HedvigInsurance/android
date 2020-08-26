package com.hedvig.app.feature.insurance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.NORWEGIAN_TRAVEL
import com.hedvig.app.MockInsuranceViewModel.Companion.SWEDISH_HOUSE
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.MockLoggedInViewModel
import com.hedvig.app.genericDevelopmentAdapter
import com.hedvig.app.insuranceModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_NO_RENEWAL
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module


class InsuranceMockActivity : AppCompatActivity(R.layout.activity_generic_development) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        unloadKoinModules(
            listOf(
                loggedInModule,
                insuranceModule
            )
        )
        loadKoinModules(MOCK_MODULE)

        findViewById<RecyclerView>(R.id.root).adapter = genericDevelopmentAdapter {
            header("Tab Screen")
            clickableItem("Renewal /w SE apartment") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = INSURANCE_DATA
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("Renewal /w SE house") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = SWEDISH_HOUSE
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("No Renewal /w SE apartment") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = INSURANCE_DATA_NO_RENEWAL
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("Norwegian travel and home contract") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("Norwegian travel") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = NORWEGIAN_TRAVEL
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("Norwegian home") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = NORWEGIAN_HOME_CONTENTS
                    shouldError = false
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }
            clickableItem("Norwegian home Error") {
                MockInsuranceViewModel.apply {
                    insuranceMockData = NORWEGIAN_HOME_CONTENTS
                    shouldError = true
                }
                startActivity(LoggedInActivity.newInstance(this@InsuranceMockActivity, initialTab = LoggedInTabs.INSURANCE))
            }

        }
    }

    companion object {
        private val MOCK_MODULE = module {
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<InsuranceViewModel> { MockInsuranceViewModel() }
        }
    }
}
