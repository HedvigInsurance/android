package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import org.threeten.bp.YearMonth

abstract class KeyGearValuationViewModel : ViewModel() {
    val purchaseDate = MutableLiveData<YearMonth>()

    fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }

    abstract fun submit()
}

class KeyGearValuationViewModelImpl(keyGearItemsRepository: KeyGearItemsRepository) :
    KeyGearValuationViewModel() {
    override fun submit() {
    }
}
