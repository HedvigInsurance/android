package com.hedvig.app.util

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.intent.Intents
import org.junit.rules.ExternalResource

open class LazyActivityScenarioRule<T: Activity>(private val cls: Class<T>): ExternalResource() {
    private var scenario: ActivityScenario<T>? = null

    fun launch(intent: Intent) {
        scenario = ActivityScenario.launch(intent)
    }

    fun launch() {
        scenario = ActivityScenario.launch(cls)
    }

    override fun after() {
        super.after()
        scenario?.close()
    }
}

class LazyIntentsActivityScenarioRule<T: Activity>(cls: Class<T>): LazyActivityScenarioRule<T>(cls) {
    override fun before() {
        super.before()
        Intents.init()
    }

    override fun after() {
        Intents.release()
        super.after()
    }
}
