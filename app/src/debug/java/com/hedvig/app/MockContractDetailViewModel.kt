package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.dashboard.ui.Contract
import com.hedvig.app.feature.dashboard.ui.ContractDetailViewModel

class MockContractDetailViewModel : ContractDetailViewModel() {
    override val data = MutableLiveData<Contract>()

    override fun loadContract(id: String) {
        data.value = MockDashboardViewModel.DATA.contracts.find { it.id == id }
    }
}
