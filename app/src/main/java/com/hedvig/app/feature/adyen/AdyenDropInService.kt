package com.hedvig.app.feature.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.hedvig.app.ApolloClientWrapper
import org.json.JSONObject
import org.koin.android.ext.android.inject

class AdyenDropInService : DropInService() {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
