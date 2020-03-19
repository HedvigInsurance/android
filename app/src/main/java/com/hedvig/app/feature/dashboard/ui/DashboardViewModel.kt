package com.hedvig.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.app.data.debit.DirectDebitRepository
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

abstract class DashboardViewModel : ViewModel() {
    abstract val data: LiveData<DashboardData>
    abstract val directDebitStatus: LiveData<DirectDebitQuery.Data>
}

class DashboardViewModelImpl(
    private val dashboardRepository: DashboardRepository,
    private val directDebitRepository: DirectDebitRepository
) : DashboardViewModel() {

    override val data = MutableLiveData<DashboardData>()
    override val directDebitStatus = MutableLiveData<DirectDebitQuery.Data>()

    init {
        viewModelScope.launch {
            val dashboardResponse = dashboardRepository
                .dashboardAsync()
                .await()

            directDebitRepository
                .directDebit()
                .collect { response ->
                    response.data()?.let { data ->
                        directDebitStatus.postValue(data)
                    }
                }
        }
    }
}

data class DashboardData(
    val contracts: List<Contract>
)

data class Contract(
    val id: String,
    val status: ContractStatus,
    val inception: LocalDate,
    val displayName: String,
    val upcomingRenewal: DashboardQuery.UpcomingRenewal?,
    val currentAgreement: DashboardQuery.CurrentAgreement,
    val perils: List<PerilCategoryFragment.Peril>
)
