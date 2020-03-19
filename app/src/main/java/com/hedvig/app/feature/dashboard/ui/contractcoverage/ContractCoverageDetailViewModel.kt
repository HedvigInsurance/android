package com.hedvig.app.feature.dashboard.ui.contractcoverage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.dashboard.ui.Contract

abstract class ContractCoverageDetailViewModel : ViewModel() {
    abstract val data: LiveData<Contract>

    abstract fun loadContract(id: String)
}

class ContractCoverageDetailViewModelImpl : ContractCoverageDetailViewModel() {
    override val data = MutableLiveData<Contract>()

    override fun loadContract(id: String) {
        TODO("Not yet implemented")
    }
}
