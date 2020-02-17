package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import com.hedvig.app.util.LiveEvent
import kotlinx.coroutines.launch

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract val isUploading: LiveEvent<Boolean>

    abstract fun loadItem(id: String)
    abstract fun uploadReceipt(uri: Uri)
}

class KeyGearItemDetailViewModelImpl(private val repository: KeyGearItemsRepository) :
    KeyGearItemDetailViewModel() {

    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()

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
