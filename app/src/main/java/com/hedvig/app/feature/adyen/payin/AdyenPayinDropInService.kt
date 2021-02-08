package com.hedvig.app.feature.adyen.payin

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.hedvig.app.feature.adyen.AdyenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AdyenPayinDropInService : DropInService(), CoroutineScope {
    private val adyenRepository: AdyenRepository by inject()

    private val coroutineJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel()
    }

    override fun makeDetailsCall(actionComponentData: JSONObject) = runBlocking(coroutineContext) {
        val response = runCatching {
            adyenRepository
                .submitAdditionalPaymentDetails(actionComponentData)
        }

        val result = response.getOrNull()?.data?.submitAdditionalPaymentDetails
            ?: return@runBlocking CallResult(CallResult.ResultType.ERROR, "Error")

        result.asAdditionalPaymentsDetailsResponseAction?.action?.let { action ->
            return@runBlocking CallResult(CallResult.ResultType.ACTION, action)
        }

        result.asAdditionalPaymentsDetailsResponseFinished?.resultCode?.let { resultCode ->
            return@runBlocking CallResult(CallResult.ResultType.FINISHED, resultCode)
        }

        CallResult(CallResult.ResultType.ERROR, "Unknown error")
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject) =
        runBlocking(coroutineContext) {
            val response = runCatching {
                adyenRepository
                    .tokenizePaymentDetails(paymentComponentData)
            }

            val result = response.getOrNull()?.data?.tokenizePaymentDetails
                ?: return@runBlocking CallResult(CallResult.ResultType.ERROR, "Error")

            result.asTokenizationResponseAction?.action?.let { action ->
                return@runBlocking CallResult(CallResult.ResultType.ACTION, action)
            }

            result.asTokenizationResponseFinished?.resultCode?.let { resultCode ->
                return@runBlocking CallResult(CallResult.ResultType.FINISHED, resultCode)
            }

            CallResult(CallResult.ResultType.ERROR, "Unknown error")
        }
}
