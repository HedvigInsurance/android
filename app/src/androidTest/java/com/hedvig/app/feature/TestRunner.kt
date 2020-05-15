package com.hedvig.app.feature

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import org.koin.core.KoinComponent

class TestRunner : AndroidJUnitRunner(), KoinComponent {

    override fun newApplication(
        cl: ClassLoader?,
        className: String?,
        context: Context?
    ): Application = super.newApplication(cl, TestApplication::class.java.name, context)
}
