package com.hedvig.app.feature.keygear.ui.tab

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.KeyGearItemsQuery
import com.hedvig.app.feature.keygear.data.DeviceInformationService
import com.hedvig.app.feature.keygear.data.KeyGearItemsRepository
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class KeyGearViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val data: KeyGearItemsQuery.Data) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()

    abstract fun sendAutoAddedItems(viewContext: Context)
    abstract fun load()
}

class KeyGearViewModelImpl(
    private val repository: KeyGearItemsRepository,
    private val deviceInformationService: DeviceInformationService
) : KeyGearViewModel() {
    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            repository
                .keyGearItems()
                .onEach { response ->
                    response.errors?.let {
                        _data.value = ViewState.Error
                        return@onEach
                    }
                    response.data?.let { _data.value = ViewState.Success(it) }
                }
                .catch { e ->
                    e(e)
                    _data.value = ViewState.Error
                }
                .collect()
        }
    }

    override fun sendAutoAddedItems(viewContext: Context) {
        viewModelScope.launch {
            val deviceFingerprint = deviceInformationService.getDeviceFingerprint()
            val deviceType = deviceInformationService.getDeviceType(viewContext)
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
