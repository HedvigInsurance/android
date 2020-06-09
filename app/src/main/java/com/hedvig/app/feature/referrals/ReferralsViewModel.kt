package com.hedvig.app.feature.referrals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import kotlinx.coroutines.launch

abstract class ReferralsViewModel : ViewModel() {
    abstract val data: LiveData<ReferralsQuery.Data>
}

class ReferralsViewModelImpl(
    private val referralsRepository: ReferralsRepository
) : ReferralsViewModel() {
    override val data = MutableLiveData<ReferralsQuery.Data>()

    init {
        viewModelScope.launch {
            val result = runCatching { referralsRepository.referralsAsync().await() }
            data.postValue(result.getOrNull()?.data())
        }
    }
}
