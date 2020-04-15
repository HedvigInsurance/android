package com.hedvig.app.feature.keygear.ui.itemdetail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemQuery
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import com.hedvig.app.util.LiveEvent
import com.hedvig.app.util.extensions.safeLaunch
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

abstract class KeyGearItemDetailViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemQuery.KeyGearItem>

    abstract val isUploading: LiveEvent<Boolean>
    abstract val isDeleted: LiveEvent<Boolean>

    abstract fun loadItem(id: String)
    abstract fun uploadReceipt(uri: Uri)
    abstract fun updateItemName(newName: String)
    abstract fun deleteItem()
}

class KeyGearItemDetailViewModelImpl(
    private val repository: KeyGearItemsRepository
) : KeyGearItemDetailViewModel() {

    override val data = MutableLiveData<KeyGearItemQuery.KeyGearItem>()

    override val isUploading = LiveEvent<Boolean>()
    override val isDeleted = LiveEvent<Boolean>()

    override fun loadItem(id: String) {
        viewModelScope.safeLaunch {
            repository
                .keyGearItem(id)
                .onEach { response -> data.postValue(response.data()?.keyGearItem) }
                .catch { e -> e(e) }
                .collect()
        }
    }

    override fun uploadReceipt(uri: Uri) {
        viewModelScope.safeLaunch {
            isUploading.postValue(true)
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@safeLaunch
            val result = runCatching { repository.uploadReceipt(id, uri) }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
            }
            isUploading.postValue(false)
        }
    }

    override fun updateItemName(newName: String) {
        viewModelScope.safeLaunch {
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@safeLaunch
            val result = runCatching { repository.updateItemName(id, newName) }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
            }
        }
    }

    override fun deleteItem() {
        viewModelScope.safeLaunch {
            val id = data.value?.fragments?.keyGearItemFragment?.id ?: return@safeLaunch
            val result = runCatching { repository.deleteItem(id) }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e(it) }
            }
            isDeleted.postValue(true)
        }
    }
}
