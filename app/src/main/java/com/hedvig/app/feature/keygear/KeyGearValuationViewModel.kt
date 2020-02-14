package com.hedvig.app.feature.keygear

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.YearMonth

abstract class KeyGearValuationViewModel : ViewModel() {
    val purchaseDate = MutableLiveData<YearMonth>()

    fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }
}

class KeyGearValuationViewModelImpl : KeyGearValuationViewModel() {

}
