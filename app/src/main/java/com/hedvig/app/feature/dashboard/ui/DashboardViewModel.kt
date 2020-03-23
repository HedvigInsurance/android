package com.hedvig.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.app.data.debit.DirectDebitRepository
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class DashboardViewModel : ViewModel() {
    abstract val data: LiveData<DashboardQuery.Data>
    abstract val directDebitStatus: LiveData<DirectDebitQuery.Data>
}

class DashboardViewModelImpl(
    private val dashboardRepository: DashboardRepository,
    private val directDebitRepository: DirectDebitRepository
) : DashboardViewModel() {

    override val data = MutableLiveData<DashboardQuery.Data>()
    override val directDebitStatus = MutableLiveData<DirectDebitQuery.Data>()

    init {
        viewModelScope.launch {
            directDebitRepository
                .directDebit()
                .onEach { response ->
                    response.data()?.let { data ->
                        directDebitStatus.postValue(data)
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
                return@launch
            }

            data.postValue(dashboardResponse.getOrNull()?.data())
        }
    }
}
