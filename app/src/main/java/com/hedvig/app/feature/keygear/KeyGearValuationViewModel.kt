package com.hedvig.app.feature.keygear

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

abstract class KeyGearValuationViewModel : ViewModel() {
    abstract val uploadResult: LiveData<KeyGearItemQuery.Data>
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
    override val uploadResult = MutableLiveData<KeyGearItemQuery.Data>()
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override fun updatePurchaseDateAndPrice(
        id: String,
        date: LocalDate,
        price: MonetaryAmountV2Input
    ) {
        viewModelScope.launch {
            val result = runCatching { repository.updatePurchasePriceAndDateAsync(id, date, price) }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
            }
            result.getOrNull()?.let { uploadResult.postValue(it) }
        }
    }

    override fun loadItem(id: String) {
        viewModelScope.launch {
            repository
                .keyGearItem(id)
                .onEach { response ->
                    data.postValue(response.data?.keyGearItem)
                }
                .catch { e -> e(e) }
                .collect()
        }
    }
}
