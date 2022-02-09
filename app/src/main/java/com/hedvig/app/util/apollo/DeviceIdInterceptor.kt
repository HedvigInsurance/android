package com.hedvig.app.util.apollo

import com.hedvig.app.authenticate.DeviceIdStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val HEADER_NAME = "hedvig-device-id"

class DeviceIdInterceptor(
    private val deviceIdStore: DeviceIdStore
) : Interceptor {

    private var deviceId: String? = null

    init {
        CoroutineScope(Dispatchers.IO).launch {
            deviceIdStore.observeDeviceId().collect {
                deviceId = it
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
