package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.android.owldroid.type.MonetaryAmountV2Input
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.launch
import org.threeten.bp.YearMonth

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract val isUploading: LiveEvent<Boolean>

    abstract fun loadItem(id: String)
    abstract fun uploadReceipt(uri: Uri)
    abstract fun updatePurchaseDate(id: String, yearMonth: YearMonth, price: MonetaryAmountV2Input)
}

class KeyGearItemDetailViewModelImpl(private val repository: KeyGearItemsRepository) :
    KeyGearItemDetailViewModel() {
    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()

    override fun updatePurchaseDate(
        id: String,
        yearMonth: YearMonth,
        price: MonetaryAmountV2Input
    ) {
        viewModelScope.launch {
            repository.updatePurchasePriceAndDateAsync(id, yearMonth, price)

        }
    }

    override fun loadItem(id: String) {
        viewModelScope.launch {
            for (response in repository.keyGearItem(id)) {
                data.postValue(response.data()?.keyGearItem)
            }
        }
    }

    override fun uploadReceipt(uri: Uri) {
        isUploading.value = true
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
