package com.hedvig.android.shareddi

import io.ktor.client.plugins.HttpSendInterceptor
import io.ktor.client.request.header

@Suppress("FunctionName")
fun DeviceIdInterceptor(deviceIdFetcher: DeviceIdFetcher): HttpSendInterceptor = { request ->
  val deviceId = deviceIdFetcher.fetch()
  execute(request.apply { header(HEADER_NAME, deviceId) })
}

private const val HEADER_NAME = "hedvig-device-id"
