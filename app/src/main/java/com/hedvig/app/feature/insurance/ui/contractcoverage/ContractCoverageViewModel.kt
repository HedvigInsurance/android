package com.hedvig.app.feature.insurance.ui.contractcoverage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.launch

abstract class ContractCoverageViewModel : ViewModel() {
    abstract val data: LiveData<InsuranceQuery.Contract>

    abstract fun loadContract(id: String)
}

class ContractCoverageViewModelImpl(
    private val repository: InsuranceRepository
) : ContractCoverageViewModel() {
    override val data = MutableLiveData<InsuranceQuery.Contract>()

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                repository
                    .dashboardAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            val contract = response
                .getOrNull()
                ?.data
                ?.contracts
                ?.first { it.id == id }

            data.postValue(contract)
        }
    }
}
