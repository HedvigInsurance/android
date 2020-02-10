package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.YearMonth

abstract class KeyGearValuationViewModel: ViewModel() {
    abstract val purchaseDate: LiveData<YearMonth>

    abstract fun choosePurchaseDate(yearMonth: YearMonth)

}

class KeyGearValuationViewModelIml : KeyGearValuationViewModel() {

    override val purchaseDate = MutableLiveData<YearMonth>()

    override fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }
}
