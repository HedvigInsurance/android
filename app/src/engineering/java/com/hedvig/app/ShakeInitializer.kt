package com.hedvig.app

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.shakebugs.shake.Shake

class ShakeInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        // Only initialize Shake when running a staging build
        if (BuildConfig.APPLICATION_ID != "com.hedvig.test.app") {
            return
        }
        Shake.getReportConfiguration().isInvokeShakeOnShakeDeviceEvent = true
        Shake.getReportConfiguration().isInvokeShakeOnScreenshot = true
        Shake.getReportConfiguration().isShowFloatingReportButton = true
        Shake.start(context.applicationContext as Application, "TODO", "TODO")
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
