package com.hedvig.app.feature.dashboard.ui.contractdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import com.hedvig.app.feature.dashboard.ui.Contract
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {
    abstract val data: LiveData<Contract>

    abstract fun loadContract(id: String)
}

class ContractDetailViewModelImpl(
    private val dashboardRepository: DashboardRepository
) : ContractDetailViewModel() {
    override val data = MutableLiveData<Contract>()

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val response = dashboardRepository
                .dashboardAsync()
                .await()
        }
    }
}
