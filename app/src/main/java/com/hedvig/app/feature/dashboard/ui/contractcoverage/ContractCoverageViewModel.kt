package com.hedvig.app.feature.dashboard.ui.contractcoverage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hedvig.app.feature.dashboard.ui.Contract

abstract class ContractCoverageViewModel : ViewModel() {
    abstract val data: LiveData<Contract>

    abstract fun loadContract(id: String)
}

class ContractCoverageViewModelImpl : ContractCoverageViewModel() {
    override val data = MutableLiveData<Contract>()

    override fun loadContract(id: String) {
        TODO("Not yet implemented")
    }
}
