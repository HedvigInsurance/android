package com.hedvig.app.feature.adyen.payin

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.hedvig.app.feature.adyen.AdyenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
        // coroutineJob.cancel() // Cannot cancel this job due to https://github.com/Adyen/adyen-android/issues/447
    }

    override fun onDetailsCallRequested(actionComponentData: ActionComponentData, actionComponentJson: JSONObject) {
        launch(coroutineContext) {
            val response = runCatching {
                adyenRepository
                    .submitAdditionalPaymentDetails(actionComponentJson)
            }

            val result = response.getOrNull()?.data?.submitAdditionalPaymentDetails

            if (result == null) {
                sendResult(DropInServiceResult.Error("Error"))
                return@launch
            }

            result.asAdditionalPaymentsDetailsResponseAction?.action?.let { action ->
                sendResult(DropInServiceResult.Action(action))
                return@launch
            }

            result.asAdditionalPaymentsDetailsResponseFinished?.resultCode?.let { resultCode ->
                sendResult(DropInServiceResult.Finished(resultCode))
                return@launch
            }

            sendResult(DropInServiceResult.Error("Unknown error"))
        }
    }

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch(coroutineContext) {
            val response = runCatching {
                adyenRepository
                    .tokenizePaymentDetails(paymentComponentJson)
            }

            val result = response.getOrNull()?.data?.tokenizePaymentDetails
            if (result == null) {
                sendResult(DropInServiceResult.Error("Error"))
                return@launch
            }

            result.asTokenizationResponseAction?.action?.let { action ->
                sendResult(DropInServiceResult.Action(action))
                return@launch
            }

            result.asTokenizationResponseFinished?.resultCode?.let { resultCode ->
                sendResult(DropInServiceResult.Finished(resultCode))
                return@launch
            }

            sendResult(DropInServiceResult.Error("Unknown error"))
        }
    }
}
