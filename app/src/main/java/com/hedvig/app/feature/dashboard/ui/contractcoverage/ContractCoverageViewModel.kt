package com.hedvig.app.feature.dashboard.ui.contractcoverage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.android.owldroid.graphql.DashboardQuery

abstract class ContractCoverageViewModel : ViewModel() {
    abstract val data: LiveData<DashboardQuery.Contract>

    abstract fun loadContract(id: String)
}

class ContractCoverageViewModelImpl : ContractCoverageViewModel() {
    override val data = MutableLiveData<DashboardQuery.Contract>()

    override fun loadContract(id: String) {
        TODO("Not yet implemented")
    }
}
