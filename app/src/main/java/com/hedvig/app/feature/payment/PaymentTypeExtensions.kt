package com.hedvig.app.feature.payment

import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinActivity
import com.hedvig.app.feature.trustly.TrustlyConnectPayinActivity
import com.hedvig.hanalytics.PaymentType

val PaymentType.connectPayinActivity: Class<*>
    get() = when (this) {
        PaymentType.ADYEN -> AdyenConnectPayinActivity::class.java
        PaymentType.TRUSTLY -> TrustlyConnectPayinActivity::class.java
    }
