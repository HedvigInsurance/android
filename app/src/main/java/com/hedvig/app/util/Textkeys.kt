package com.hedvig.app.util

fun interpolateTextKey(text: String, vararg replacements: Pair<String, Any?>): String =
    replacements
        .toList()
        .fold(text) { acc, (key, value) ->
            acc.replace("{$key}", value?.toString() ?: "")
        }
