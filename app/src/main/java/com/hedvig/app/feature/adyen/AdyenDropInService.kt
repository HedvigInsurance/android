package com.hedvig.app.feature.adyen

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AdyenDropInService : DropInService(), CoroutineScope {
    private val adyenRepository: AdyenRepository by inject()

    private val coroutineJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
    }


    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        TODO("Not implemented")
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        launch {
            val response = runCatching {

            }
        }
        TODO("Not implemented")
    }

}
