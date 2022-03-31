package com.hedvig.app.feature.adyen.payin

import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.hedvig.app.feature.adyen.ConnectPaymentUseCase
import com.hedvig.app.feature.adyen.SubmitAdditionalPaymentDetailsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class AdyenPayinDropInService : DropInService(), CoroutineScope {
    private val connectPaymentUseCase: ConnectPaymentUseCase by inject()
    private val submitAdditionalPaymentDetailsUseCase: SubmitAdditionalPaymentDetailsUseCase by inject()

    private val coroutineJob = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    override fun onDestroy() {
        super.onDestroy()
        // coroutineJob.cancel() // Cannot cancel this job due to https://github.com/Adyen/adyen-android/issues/447
    }

    override fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        actionComponentJson: JSONObject
    ) {
        launch(coroutineContext) {
            submitAdditionalPaymentDetailsUseCase.submitAdditionalPaymentDetails(actionComponentJson)
                .mapLeft { it.toDropInServiceResult() }
                .fold(
                    ifLeft = { sendResult(it) },
                    ifRight = { sendResult(DropInServiceResult.Finished(it.code)) }
                )
        }
    }

    override fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch(coroutineContext) {
            connectPaymentUseCase.getPaymentTokenId(paymentComponentJson)
                .mapLeft { it.toError() }
                .fold(
                    ifLeft = { sendResult(it) },
                    ifRight = { sendResult(DropInServiceResult.Finished(it.id)) }
                )
        }
    }

    private fun ConnectPaymentUseCase.Error.toError() = when (this) {
        is ConnectPaymentUseCase.Error.CheckoutPaymentAction -> DropInServiceResult.Action(action)
        is ConnectPaymentUseCase.Error.ErrorMessage -> DropInServiceResult.Error(message)
    }
}

fun SubmitAdditionalPaymentDetailsUseCase.Error.toDropInServiceResult() = when (this) {
    is SubmitAdditionalPaymentDetailsUseCase.Error.CheckoutPaymentAction -> DropInServiceResult.Action(action)
    is SubmitAdditionalPaymentDetailsUseCase.Error.ErrorMessage -> DropInServiceResult.Error(message)
}
