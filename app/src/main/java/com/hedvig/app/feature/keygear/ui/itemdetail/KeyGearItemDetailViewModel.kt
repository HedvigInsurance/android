
package com.hedvig.app.feature.keygear.ui.itemdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.threeten.bp.YearMonth

class KeyGearItemDetailViewModel : ViewModel() {
    val purchaseDate = MutableLiveData<YearMonth>()

    fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }
}
