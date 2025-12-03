package com.hedvig.android.navigation.compose

import io.ktor.http.encodeURLParameter
import io.ktor.http.decodeURLPart

internal actual fun urlEncode(value: String): String = value.encodeURLParameter()

internal actual fun urlDecode(value: String): String = value.decodeURLPart()
