package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

interface DeviceIdDataStore {
  fun observeDeviceId(): Flow<String>
}

internal class DeviceIdDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
) : DeviceIdDataStore {

  private val key = stringPreferencesKey("hedvig-device-id")

  init {
    CoroutineScope(Dispatchers.IO).launch {
      val deviceId = observeDeviceId().firstOrNull()
      if (deviceId == null || deviceId == "") {
        generateDeviceId()
      }
    }
  }

  override fun observeDeviceId(): Flow<String> {
    return dataStore.data.map { it[key] ?: "" }
  }

  private suspend fun generateDeviceId() {
    val deviceId = UUID.randomUUID().toString()
    dataStore.edit {
      it[key] = deviceId
    }
  }
}
