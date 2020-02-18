package com.hedvig.app.feature.keygear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

abstract class KeyGearValuationViewModel : ViewModel() {
    abstract val finishedUploading: LiveData<Boolean>

    abstract fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    )
}

class KeyGearValuationViewModelImpl(
    private val repository: KeyGearItemsRepository
) :
    KeyGearValuationViewModel() {
    override val finishedUploading = MutableLiveData<Boolean>()

    override fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ) {
        viewModelScope.launch {
            repository.updatePurchasePriceAndDateAsync(id, date, price)
            finishedUploading.postValue(true)
        }
    }
}
