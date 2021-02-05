package com.hedvig.app.feature.adyen.payout

import com.adyen.checkout.dropin.service.CallResult
import com.adyen.checkout.dropin.service.DropInService
import com.hedvig.android.owldroid.type.PayoutMethodStatus
import com.hedvig.android.owldroid.type.TokenizationResultType
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.profile.ui.payment.PaymentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AdyenPayoutDropInService : DropInService(), CoroutineScope {
    private val adyenRepository: AdyenRepository by inject()
    private val paymentRepository: PaymentRepository by inject()

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

        result.asAdditionalPaymentsDetailsResponseFinished?.let { finishedResponse ->
            finishedResponse.tokenizationResult.toPayoutMethodStatusOrNull()?.let { payoutMethodStatus ->
                runCatching { paymentRepository.writeActivePayoutMethodStatus(payoutMethodStatus) }
            }
            return@runBlocking CallResult(CallResult.ResultType.FINISHED, finishedResponse.resultCode)
        }

        CallResult(CallResult.ResultType.ERROR, "Unknown error")
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject) =
        runBlocking(coroutineContext) {
            val response = runCatching {
                adyenRepository
                    .tokenizePayoutDetails(paymentComponentData)
            }

            val result = response.getOrNull()?.data?.tokenizePayoutDetails
                ?: return@runBlocking CallResult(CallResult.ResultType.ERROR, "Error")

            result.asTokenizationResponseAction?.action?.let { action ->
                return@runBlocking CallResult(CallResult.ResultType.ACTION, action)
            }

            result.asTokenizationResponseFinished?.let { finishedResponse ->
                finishedResponse.tokenizationResult.toPayoutMethodStatusOrNull()?.let { payoutMethodStatus ->
                    runCatching { paymentRepository.writeActivePayoutMethodStatus(payoutMethodStatus) }
                }
                return@runBlocking CallResult(CallResult.ResultType.FINISHED, finishedResponse.resultCode)
            }

            CallResult(CallResult.ResultType.ERROR, "Unknown error")
        }

    private fun TokenizationResultType.toPayoutMethodStatusOrNull() = when (this) {
        TokenizationResultType.COMPLETED -> PayoutMethodStatus.ACTIVE
        TokenizationResultType.PENDING -> PayoutMethodStatus.PENDING
        else -> null
    }
}

