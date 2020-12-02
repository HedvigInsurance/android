package com.hedvig.app.feature.onbarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ChoosePlanQuery
import kotlinx.coroutines.launch

abstract class ChoosePlanViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<ChoosePlanQuery.Data>>()
    val data: LiveData<Result<ChoosePlanQuery.Data>> = _data

    private val _selectedQuoteType: MutableLiveData<OnboardingModel.Quote> =
        MutableLiveData(OnboardingModel.Quote.Bundle(true))
    val selectedQuoteType: LiveData<OnboardingModel.Quote> = _selectedQuoteType
    abstract fun load()

    fun setSelectedQuoteType(type: OnboardingModel.Quote) {
        _selectedQuoteType.postValue(type)
    }

    fun getSelectedNoPlan() = when (_selectedQuoteType.value) {
        is OnboardingModel.Quote.Bundle -> NoPlan.BUNDLE
        is OnboardingModel.Quote.Content -> NoPlan.CONTENT
        is OnboardingModel.Quote.Travel -> NoPlan.TRAVEL
        null -> NoPlan.BUNDLE
    }
}

class ChoosePlanViewModelImpl(
    private val repository: ChoosePlanRepository
) : ChoosePlanViewModel() {
    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val response = runCatching { repository.getBundles() }
            if (response.isFailure) {
                response.exceptionOrNull()?.let { exception ->
                    _data.postValue(Result.failure(exception))
                }
                return@launch
            }
            if (response.getOrNull()?.hasErrors() == true) {
                _data.postValue(Result.failure(Error()))
                return@launch
            }
            response.getOrNull()?.data?.let { _data.postValue(Result.success(it)) }
        }
    }
}
