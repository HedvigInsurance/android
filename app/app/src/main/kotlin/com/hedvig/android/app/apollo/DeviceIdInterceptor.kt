package com.hedvig.android.app.apollo

import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.datastore.DeviceIdDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response

private const val HEADER_NAME = "hedvig-device-id"

class DeviceIdInterceptor(
  private val deviceIdDataStore: DeviceIdDataStore,
  applicationScope: ApplicationScope,
) : Interceptor {
  private var deviceId: String? = null

  init {
    applicationScope.launch(Dispatchers.IO) {
      deviceIdDataStore.observeDeviceId().collect { id ->
        deviceId = id
      }
    }
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val deviceId = deviceId
    val request = chain.request().newBuilder()
      .apply {
        if (deviceId != null) {
          addHeader(HEADER_NAME, deviceId)
        }
      }
      .build()

    return chain.proceed(request)
  }
}
