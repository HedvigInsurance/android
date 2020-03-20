package com.hedvig.app.feature.keygear.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.feature.keygear.data.DeviceInformationService
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

abstract class KeyGearViewModel : ViewModel() {
    abstract val data: LiveData<KeyGearItemsQuery.Data>

    abstract fun sendAutoAddedItems()
}

class KeyGearViewModelImpl(
    private val repository: KeyGearItemsRepository,
    private val deviceInformationService: DeviceInformationService
) : KeyGearViewModel() {
    override val data = MutableLiveData<KeyGearItemsQuery.Data>()

    override fun sendAutoAddedItems() {
        viewModelScope.launch {
            val deviceFingerprint = deviceInformationService.getDeviceFingerprint()
            val deviceType = deviceInformationService.getDeviceType()
            val deviceName = deviceInformationService.getDeviceName()

            val result = runCatching {
                repository.createKeyGearItemAsync(
                    deviceType.into(),
                    listOf(),
                    deviceFingerprint,
                    deviceName
                )
            }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e { it.localizedMessage } }
            }
        }
    }

    init {
        viewModelScope.launch {
            val result = runCatching {
                repository
                    .keyGearItems()
                    .catch { e -> e { e.localizedMessage } }
                    .collect { response ->
                        data.postValue(response.data())
                    }
            }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { e { it.localizedMessage } }
            }
        }
    }
}
