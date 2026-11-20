package com.hedvig.android.network.clients.di

import platform.Foundation.NSTimeZone
import platform.Foundation.localTimeZone

internal actual fun platformClientName(appPackageId: String): String = "iOS:$appPackageId"

internal actual fun deviceTimeZoneId(): String = NSTimeZone.localTimeZone.name
