package com.hedvig.android.network.clients.di

/**
 * The value of the `apollographql-client-name` header, in the format the platform's app sends.
 */
internal expect fun platformClientName(appPackageId: String): String

/** The device's IANA time zone id, such as `Europe/Stockholm`, for the `Hedvig-TimeZone` header. */
internal expect fun deviceTimeZoneId(): String
