package com.hedvig.app.feature.keygear.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.feature.keygear.data.DeviceInformationService
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
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

            repository.createKeyGearItemAsync(deviceType.into(), listOf(), deviceFingerprint, deviceName)
        }
    }

    init {
        viewModelScope.launch {
            repository
                .keyGearItems()
                .collect { response ->
                    data.postValue(response.data())
                }
        }
    }
}
