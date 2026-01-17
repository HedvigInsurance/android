package com.hedvig.android.navigation.compose

import io.ktor.http.decodeURLPart
import io.ktor.http.encodeURLParameter

internal actual fun urlEncode(value: String): String = value.encodeURLParameter()

internal actual fun urlDecode(value: String): String = value.decodeURLPart()
