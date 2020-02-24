package com.hedvig.app.feature.keygear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

abstract class KeyGearValuationViewModel : ViewModel() {
    abstract val finishedUploading: LiveData<Boolean>
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    )

    abstract fun loadItem(id: String)
}

class KeyGearValuationViewModelImpl(
    private val repository: KeyGearItemsRepository
) :
    KeyGearValuationViewModel() {
    override val finishedUploading = MutableLiveData<Boolean>()
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

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

    override fun loadItem(id: String) {
        viewModelScope.launch {
            for (response in repository.keyGearItem(id)) {
                data.postValue(response.data()?.keyGearItem)
            }
        }
    }
}
