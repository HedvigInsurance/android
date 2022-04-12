package com.hedvig.app.feature.profile.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.util.extensions.combineState
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import com.hedvig.hanalytics.PaymentType
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class PaymentViewModel(
    hAnalytics: HAnalytics,
) : ViewModel() {
    protected val _paymentData = MutableStateFlow<PaymentQuery.Data?>(null)
    protected val _payinStatusData = MutableStateFlow<Pair<PayinStatusQuery.Data?, PaymentType>?>(null)
    val data: StateFlow<Pair<PaymentQuery.Data?, Pair<PayinStatusQuery.Data?, PaymentType>?>> =
        combineState(_paymentData, _payinStatusData, viewModelScope)

    abstract fun load()

    init {
        hAnalytics.screenView(AppScreen.PAYMENTS)
    }
}

class PaymentViewModelImpl(
    private val paymentRepository: PaymentRepository,
    private val payinStatusRepository: PayinStatusRepository,
    private val featureManager: FeatureManager,
    hAnalytics: HAnalytics,
) : PaymentViewModel(hAnalytics) {

    init {
        viewModelScope.launch {
            paymentRepository
                .payment()
                .onEach { _paymentData.value = it.data }
                .catch { e(it) }
                .launchIn(this)

            payinStatusRepository
                .payinStatusFlow()
                .onEach { _payinStatusData.value = Pair(it.data, featureManager.getPaymentType()) }
                .catch { e(it) }
                .launchIn(this)
        }
    }

    override fun load() {
        viewModelScope.launch {
            paymentRepository.refresh()
        }
    }
}
