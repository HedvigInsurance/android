package com.hedvig.android.network.clients.di

import java.util.TimeZone

internal actual fun platformClientName(appPackageId: String): String = appPackageId

internal actual fun deviceTimeZoneId(): String = TimeZone.getDefault().id
