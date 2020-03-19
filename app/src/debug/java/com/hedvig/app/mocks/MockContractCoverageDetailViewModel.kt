package com.hedvig.app.mocks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.app.DevelopmentActivity
import com.hedvig.app.MockDashboardViewModel
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.DashboardData
import com.hedvig.app.feature.dashboard.ui.contractcoverage.ContractCoverageDetailViewModel

class MockContractCoverageDetailViewModel(context: Context) : ContractCoverageDetailViewModel() {
    override val data = MutableLiveData<Contract>()

    private val mockData: DashboardData

    init {
        val mockPersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)

        mockData = when (mockPersona) {
            0 -> MockDashboardViewModel.SWEDISH_APARTMENT
            1 -> MockDashboardViewModel.SWEDISH_HOUSE
            2 -> MockDashboardViewModel.NORWEGIAN_HOME_CONTENTS
            3 -> MockDashboardViewModel.NORWEGIAN_TRAVEL
            4 -> MockDashboardViewModel.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
            else -> MockDashboardViewModel.SWEDISH_APARTMENT
        }
    }

    override fun loadContract(id: String) {
        data.value = mockData.contracts.find { it.id == id }
    }
}
