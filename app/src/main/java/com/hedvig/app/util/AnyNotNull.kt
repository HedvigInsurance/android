package com.hedvig.app.util

fun anyNotNull(vararg item: Any?, action: () -> Unit) {
    if (item.any { it != null }) {
        action()
    }
}
