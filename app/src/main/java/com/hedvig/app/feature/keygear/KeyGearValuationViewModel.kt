package com.hedvig.app.feature.keygear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import kotlinx.coroutines.launch
import org.threeten.bp.YearMonth

abstract class KeyGearValuationViewModel : ViewModel() {
    val purchaseDate = MutableLiveData<YearMonth>()

    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    fun choosePurchaseDate(yearMonth: YearMonth) {
        purchaseDate.value = yearMonth
    }

    abstract fun updatePurchaseDateAndPrice(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    )
}

class KeyGearValuationViewModelImpl(private val repository: KeyGearItemsRepository) :
    KeyGearValuationViewModel() {

    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun updatePurchaseDateAndPrice(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    ) {
        viewModelScope.launch {
            repository.updatePurchasePriceAndDateAsync(id, yearMonth, price)
        }
    }
}
