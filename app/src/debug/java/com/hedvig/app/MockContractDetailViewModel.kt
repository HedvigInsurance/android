package com.hedvig.app

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.ContractDetailViewModel
import com.hedvig.app.feature.dashboard.ui.DashboardData

class MockContractDetailViewModel(context: Context) : ContractDetailViewModel() {
    override val data = MutableLiveData<Contract>()

    private val mockData: DashboardData

    init {
        val mockPersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)

        mockData = when (mockPersona) {
            2 -> MockDashboardViewModel.NORWEGIAN_HOME_CONTENTS
            3 -> MockDashboardViewModel.NORWEGIAN_TRAVEL
            4 -> MockDashboardViewModel.NORWEGIAN_HOME_CONTENTS_AND_TRAVEL
            else -> MockDashboardViewModel.NORWEGIAN_HOME_CONTENTS
        }
    }

    override fun loadContract(id: String) {
        data.value = mockData.contracts.find { it.id == id }
    }
}
