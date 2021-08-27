package com.hedvig.app.feature.insurance.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val items: List<InsuranceModel>) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()
    abstract fun load()
}

class InsuranceViewModelImpl(
    private val getContractsUseCase: GetContractsUseCase
) : InsuranceViewModel() {

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            when (val result = getContractsUseCase()) {
                is GetContractsUseCase.InsuranceResult.Error -> {
                    result.message?.let { e { it } }
                    _data.value = ViewState.Error
                }
                is GetContractsUseCase.InsuranceResult.Insurance -> {
                    _data.value = ViewState.Success(items(result.insurance))
                }
            }
        }
    }
}
