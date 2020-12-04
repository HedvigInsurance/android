package com.hedvig.app.feature.adyen.payout

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.BaseActivity
import com.hedvig.app.R
import com.hedvig.app.feature.adyen.payin.AdyenRepository
import kotlinx.coroutines.launch
import org.koin.android.viewmodel.ext.android.viewModel

class AdyenConnectPayoutActivity:BaseActivity(R.layout.fragment_container_activity) {
    private val model: AdyenConnectPayoutViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }
}

abstract class AdyenConnectPayoutViewModel: ViewModel() {
    protected val _payoutMethods = MutableLiveData<Unit>()
    val payoutMethods: LiveData<Unit> = _payoutMethods
}

class AdyenConnectPayoutViewModelImpl(
    private val repository: AdyenRepository
): AdyenConnectPayoutViewModel() {
    init {
        viewModelScope.launch {

        }
    }
}
