package com.hedvig.app.feature.onbarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OnboardingViewModel : ViewModel() {
    protected val _selectedQuoteType: MutableLiveData<OnboardingModel.Quote> =
        MutableLiveData(OnboardingModel.Quote.Bundle(true))
    val selectedQuoteType: LiveData<OnboardingModel.Quote> = _selectedQuoteType

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
