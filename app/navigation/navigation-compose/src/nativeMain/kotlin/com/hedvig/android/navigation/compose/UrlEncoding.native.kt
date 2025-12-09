package com.hedvig.android.navigation.compose

import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLParameter

// todo ios: if nav is ever shared. Otherwise this is fine.
internal actual fun urlEncode(value: String): String = value.encodeURLParameter()

internal actual fun urlDecode(value: String): String = value.decodeURLPart()
