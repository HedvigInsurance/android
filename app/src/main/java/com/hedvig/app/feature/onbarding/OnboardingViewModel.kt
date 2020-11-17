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
}
