package com.hedvig.app.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import java.time.Duration

fun context(): Context = ApplicationProvider.getApplicationContext()

val Int.seconds: Duration
    get() = Duration.ofSeconds(this.toLong())
