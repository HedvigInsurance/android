package com.hedvig.app.util

import androidx.annotation.StringRes

interface ErrorEvent {
    @StringRes
    fun getErrorResource(): Int
}
