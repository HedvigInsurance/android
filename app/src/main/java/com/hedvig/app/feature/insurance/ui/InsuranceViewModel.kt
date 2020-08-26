package com.hedvig.app.feature.insurance.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    abstract val data: MutableLiveData<Result<InsuranceQuery.Data>>
    abstract fun load()
}

class InsuranceViewModelImpl(
    private val insuranceRepository: InsuranceRepository
) : InsuranceViewModel() {

    override val data = MutableLiveData<Result<InsuranceQuery.Data>>()

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val dashboardResponse = runCatching {
                insuranceRepository
                    .dashboardAsync()
                    .await()
            }
            if (dashboardResponse.isFailure) {
                dashboardResponse.exceptionOrNull()?.let { exception ->
                    e(exception)
                    data.postValue(Result.failure(exception))
                }
                return@launch
            }

            if (dashboardResponse.getOrNull()?.hasErrors() == true) {
                data.postValue(Result.failure(Error()))
                return@launch
            }

            dashboardResponse.getOrNull()?.data?.let { data.postValue(Result.success(it)) }
        }
    }
}
