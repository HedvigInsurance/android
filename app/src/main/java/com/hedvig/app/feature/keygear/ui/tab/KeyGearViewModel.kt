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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class KeyGearViewModel : ViewModel() {
    abstract val data: LiveData<Result<KeyGearItemsQuery.Data>>

    abstract fun sendAutoAddedItems()
    abstract fun load()
}

class KeyGearViewModelImpl(
    private val repository: KeyGearItemsRepository,
    private val deviceInformationService: DeviceInformationService
) : KeyGearViewModel() {
    override val data = MutableLiveData<Result<KeyGearItemsQuery.Data>>()

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            repository
                .keyGearItems()
                .onEach { response ->
                    response.errors?.let {
                        data.postValue(Result.failure(Error()))
                        return@onEach
                    }
                    response.data?.let { data.postValue(Result.success(it)) }
                }
                .catch { e ->
                    e(e)
                    data.postValue(Result.failure(e))
                }
                .collect()
        }
    }

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
                result.exceptionOrNull()?.let { e(it) }
            }
        }
    }
}
