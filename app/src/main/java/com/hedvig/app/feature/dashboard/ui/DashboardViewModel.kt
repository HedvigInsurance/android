package com.hedvig.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import com.hedvig.app.util.extensions.safeLaunch
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class DashboardViewModel : ViewModel() {
    abstract val data: LiveData<Pair<DashboardQuery.Data?, PayinStatusQuery.Data?>>
}

class DashboardViewModelImpl(
    private val dashboardRepository: DashboardRepository,
    private val payinStatusRepository: PayinStatusRepository
) : DashboardViewModel() {

    private val payinStatusData = MutableLiveData<PayinStatusQuery.Data>()
    private val dashboardData = MutableLiveData<DashboardQuery.Data>()
    override val data = combineTuple(dashboardData, payinStatusData)

    init {
        viewModelScope.safeLaunch {
            payinStatusRepository
                .payinStatus()
                .onEach { response ->
                    response.data()?.let { data ->
                        payinStatusData.postValue(data)
                    }
                }
                .catch { e(it) }
                .launchIn(this)

            val dashboardResponse = runCatching {
                dashboardRepository
                    .dashboardAsync()
                    .await()
            }

            if (dashboardResponse.isFailure) {
                dashboardResponse.exceptionOrNull()?.let { e(it) }
                return@safeLaunch
            }

            dashboardData.postValue(dashboardResponse.getOrNull()?.data())
        }
    }
}
