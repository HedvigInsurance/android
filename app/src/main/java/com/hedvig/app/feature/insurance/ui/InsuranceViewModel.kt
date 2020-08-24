package com.hedvig.app.feature.insurance.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    abstract val data: LiveData<Pair<InsuranceQuery.Data?, PayinStatusQuery.Data?>>
}

class InsuranceViewModelImpl(
    private val insuranceRepository: InsuranceRepository,
    private val payinStatusRepository: PayinStatusRepository
) : InsuranceViewModel() {

    private val payinStatusData = MutableLiveData<PayinStatusQuery.Data>()
    private val dashboardData = MutableLiveData<InsuranceQuery.Data>()
    override val data = combineTuple(dashboardData, payinStatusData)

    init {
        viewModelScope.launch {
            payinStatusRepository
                .payinStatus()
                .onEach { response ->
                    response.data?.let { data ->
                        payinStatusData.postValue(data)
                    }
                }
                .catch { e(it) }
                .launchIn(this)

            val dashboardResponse = runCatching {
                insuranceRepository
                    .dashboardAsync()
                    .await()
            }

            if (dashboardResponse.isFailure) {
                dashboardResponse.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            dashboardData.postValue(dashboardResponse.getOrNull()?.data)
        }
    }
}
