package com.hedvig.app.feature.insurance.ui.terminatedcontracts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.databinding.TerminatedContractsActivityBinding
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import com.hedvig.app.util.extensions.viewBinding
import kotlinx.coroutines.launch

class TerminatedContractsActivity : BaseActivity(R.layout.terminated_contracts_activity) {
    private val binding by viewBinding(TerminatedContractsActivityBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    companion object {
        fun newInstance(context: Context) = Intent(context, TerminatedContractsActivity::class.java)
    }
}

abstract class TerminatedContractsViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<List<InsuranceQuery.Contract>>>()
    val data: LiveData<Result<List<InsuranceQuery.Contract>>> = _data
}

class TerminatedContractsViewModelImpl(
    private val repository: InsuranceRepository
) : TerminatedContractsViewModel() {
    init {
        viewModelScope.launch {
            val result = runCatching { repository.dashboardAsync().await() }
        }
    }
}
