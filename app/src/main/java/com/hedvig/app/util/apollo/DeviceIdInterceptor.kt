package com.hedvig.app.util.apollo

import com.hedvig.android.core.datastore.DeviceIdDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val HEADER_NAME = "hedvig-device-id"

class DeviceIdInterceptor(
  private val deviceIdDataStore: DeviceIdDataStore,
) : Interceptor {

  private var deviceId: String? = null

  init {
    CoroutineScope(Dispatchers.IO).launch {
      deviceIdDataStore.observeDeviceId().collect { id ->
        deviceId = id
      }
    }
  }

  override fun intercept(chain: Interceptor.Chain): Response {
    val request = deviceId?.let {
      addHeader(chain, it)
    } ?: chain.request()

    return chain.proceed(request)
  }

  private fun addHeader(chain: Interceptor.Chain, it: String): Request {
    return chain.request().newBuilder()
      .addHeader(HEADER_NAME, it)
      .build()
  }
}
