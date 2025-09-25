package com.hedvig.android.logging.device.model.di

import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.logging.device.model.DeviceNameLoggingAuthListener
import org.koin.dsl.bind
import org.koin.dsl.module

val loggingDeviceModelModule = module {
  single<DeviceNameLoggingAuthListener> { DeviceNameLoggingAuthListener(get()) } bind AuthEventListener::class
}
