package com.hedvig.app.feature.adyen.payin

import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
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

    override fun makeDetailsCall(actionComponentJson: JSONObject) = runBlocking(coroutineContext) {
        val response = runCatching {
            adyenRepository
                .submitAdditionalPaymentDetails(actionComponentJson)
        }

        val result = response.getOrNull()?.data?.submitAdditionalPaymentDetails
            ?: return@runBlocking DropInServiceResult.Error("Error")

        result.asAdditionalPaymentsDetailsResponseAction?.action?.let { action ->
            return@runBlocking DropInServiceResult.Action(action)
        }

        result.asAdditionalPaymentsDetailsResponseFinished?.resultCode?.let { resultCode ->
            return@runBlocking DropInServiceResult.Finished(resultCode)
        }

        DropInServiceResult.Error("Unknown error")
    }

    override fun makePaymentsCall(paymentComponentJson: JSONObject) =
        runBlocking(coroutineContext) {
            val response = runCatching {
                adyenRepository
                    .tokenizePaymentDetails(paymentComponentJson)
            }

            val result = response.getOrNull()?.data?.tokenizePaymentDetails
                ?: return@runBlocking DropInServiceResult.Error("Error")

            result.asTokenizationResponseAction?.action?.let { action ->
                return@runBlocking DropInServiceResult.Action(action)
            }

            result.asTokenizationResponseFinished?.resultCode?.let { resultCode ->
                return@runBlocking DropInServiceResult.Finished(resultCode)
            }

            DropInServiceResult.Error("Unknown error")
        }
}
