package com.hedvig.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.android.owldroid.type.ContractStatus
import com.hedvig.app.feature.dashboard.data.DashboardRepository
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

abstract class DashboardViewModel : ViewModel() {
    abstract val data: LiveData<DashboardData>
}

class DashboardViewModelImpl(
    private val dashboardRepository: DashboardRepository
) : DashboardViewModel() {

    override val data = MutableLiveData<DashboardData>()

    init {
        viewModelScope.launch {
            val response = dashboardRepository
                .dashboardAsync()
                .await()
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
    val upcomingRenewal: DashboardQuery.UpcomingRenewal?,
    val currentAgreement: DashboardQuery.CurrentAgreement,
    val perils: List<PerilCategoryFragment.Peril>
)
