package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.YearMonth

abstract class KeyGearValuationViewModel: ViewModel() {
    val purchaseDate = MutableLiveData<YearMonth>()

    fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }

    abstract fun submit()
}

class KeyGearValuationViewModelImpl : KeyGearValuationViewModel() {
    override fun submit() {
        TODO("implement some network calls") //To change body of created functions use File | Settings | File Templates.
    }
}
