package com.hedvig.app.feature.insurance.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val data: InsuranceQuery.Data) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()
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
                    _data.value = ViewState.Error
                }
                return@launch
            }

            if (dashboardResponse.getOrNull()?.hasErrors() == true) {
                _data.value = ViewState.Error
                return@launch
            }

            dashboardResponse.getOrNull()?.data?.let { _data.value = ViewState.Success(it) }
        }
    }
}
