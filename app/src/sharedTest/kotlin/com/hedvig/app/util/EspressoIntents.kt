package com.hedvig.app.util

import android.app.Activity
import android.app.Instrumentation
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.hamcrest.Matchers

fun stubExternalIntents() {
    Intents.intending(Matchers.not(IntentMatchers.isInternal())).respondWith(
        Instrumentation.ActivityResult(
            Activity.RESULT_OK,
            null
        )
    )
}
