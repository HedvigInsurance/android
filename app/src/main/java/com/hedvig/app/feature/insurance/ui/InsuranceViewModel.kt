package com.hedvig.app.feature.insurance.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    abstract val data: LiveData<InsuranceQuery.Data?>
}

class InsuranceViewModelImpl(
    private val insuranceRepository: InsuranceRepository,
    private val payinStatusRepository: PayinStatusRepository
) : InsuranceViewModel() {

    //    private val payinStatusData = MutableLiveData<PayinStatusQuery.Data>()
    override val data = MutableLiveData<InsuranceQuery.Data>()
//    override val data = combineTuple(dashboardData, payinStatusData)

    init {
        viewModelScope.launch {
//            payinStatusRepository
//                .payinStatus()
//                .onEach { response ->
//                    response.data?.let { data ->
//                        payinStatusData.postValue(data)
//                    }
//                }
//                .catch { e(it) }
//                .launchIn(this)

            val dashboardResponse = runCatching {
                insuranceRepository
                    .dashboardAsync()
                    .await()
            }

            if (dashboardResponse.isFailure) {
                dashboardResponse.exceptionOrNull()?.let { e(it) }
                return@launch
            }

            data.postValue(dashboardResponse.getOrNull()?.data)
        }
    }
}
