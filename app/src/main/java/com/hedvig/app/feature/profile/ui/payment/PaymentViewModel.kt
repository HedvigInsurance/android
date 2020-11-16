package com.hedvig.app.feature.profile.ui.payment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.data.debit.PayinStatusRepository
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class PaymentViewModel : ViewModel() {
    protected val _paymentData = MutableLiveData<PaymentQuery.Data>()
    protected val _payinStatusData = MutableLiveData<PayinStatusQuery.Data>()
    val data = combineTuple(_paymentData, _payinStatusData)
}

class PaymentViewModelImpl(
    private val paymentRepository: PaymentRepository,
    private val payinStatusRepository: PayinStatusRepository
) : PaymentViewModel() {

    init {
        viewModelScope.launch {
            paymentRepository
                .payment()
                .onEach { _paymentData.postValue(it.data) }
                .catch { e(it) }
                .launchIn(this)

            payinStatusRepository
                .payinStatus()
                .onEach { _payinStatusData.postValue(it.data) }
                .catch { e(it) }
                .launchIn(this)
        }
    }
}

class PaymentRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun payment() = apolloClientWrapper
        .apolloClient
        .query(PaymentQuery())
        .watcher()
        .toFlow()
}
