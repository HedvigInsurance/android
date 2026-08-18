package com.hedvig.android.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.benasher44.uuid.uuid4
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface DeviceIdDataStore {
  fun observeDeviceId(): Flow<String?>
}

@Inject
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
internal class DeviceIdDataStoreImpl(
  private val dataStore: DataStore<Preferences>,
  coroutineScope: ApplicationScope,
) : DeviceIdDataStore {
  init {
    coroutineScope.launch(Dispatchers.IO) {
      val deviceId = observeDeviceId().firstOrNull()
      if (deviceId.isNullOrBlank()) {
        generateDeviceId()
      }
    }
  }

  override fun observeDeviceId(): Flow<String?> {
    return dataStore.data.map { it[key] }.distinctUntilChanged()
  }

  private suspend fun generateDeviceId() {
    val deviceId = uuid4().toString()
    dataStore.edit {
      it[key] = deviceId
    }
  }

  companion object {
    private val key = stringPreferencesKey("hedvig-device-id")
  }
}
