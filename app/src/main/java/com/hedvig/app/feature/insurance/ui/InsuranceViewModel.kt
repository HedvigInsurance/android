package com.hedvig.app.feature.insurance.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<InsuranceQuery.Data>>()
    val data: MutableLiveData<Result<InsuranceQuery.Data>> = _data
    abstract fun load()
}

class InsuranceViewModelImpl(
    private val insuranceRepository: InsuranceRepository
) : InsuranceViewModel() {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val dashboardResponse = runCatching {
                insuranceRepository
                    .insurance()
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
