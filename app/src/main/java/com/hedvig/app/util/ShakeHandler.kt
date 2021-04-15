package com.hedvig.app.util

import com.hedvig.app.HedvigApplication
import com.shakebugs.shake.Shake

class ShakeHandler {

    private val clientId = "UL4cR8O6F49Vac5LzphITIEMDi1bp6GhbaE0Cj1O"
    private val clientSecret = "f3QstvAkEEsnKtzLc5RthSF83qklzmb4J5S6ICqUBEBxHeyEuGBO1o9"

    fun configureAndStart(application: HedvigApplication) {
        Shake.getReportConfiguration().isInvokeShakeOnShakeDeviceEvent = true
        Shake.getReportConfiguration().isInvokeShakeOnScreenshot = true
        Shake.getReportConfiguration().isShowFloatingReportButton = true
        Shake.start(application, clientId, clientSecret)
    }
}
