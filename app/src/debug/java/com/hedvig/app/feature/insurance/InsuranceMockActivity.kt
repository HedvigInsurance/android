package com.hedvig.app.feature.insurance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.loggedin.ui.LoggedInViewModel
import com.hedvig.app.feature.referrals.MockLoggedInViewModel
import com.hedvig.app.insuranceModule
import com.hedvig.app.loggedInModule
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

    }

    companion object {
        private val MOCK_MODULE = module {
            viewModel<LoggedInViewModel> { MockLoggedInViewModel() }
            viewModel<InsuranceViewModel> { MockInsuranceViewModel() }
        }
    }
}
