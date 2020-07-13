package com.hedvig.app.feature.referrals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import kotlinx.coroutines.launch

abstract class ReferralsViewModel : ViewModel() {
    abstract val data: LiveData<Result<ReferralsQuery.Data>>
    abstract fun load()

    fun retry() {
        load()
    }
}

class ReferralsViewModelImpl(
    private val referralsRepository: ReferralsRepository
) : ReferralsViewModel() {
    override val data = MutableLiveData<Result<ReferralsQuery.Data>>()

    init {
        load()
    }

    override fun load() {
        viewModelScope.launch {
            val result = runCatching { referralsRepository.referralsAsync().await() }
            if (result.isFailure) {
                result.exceptionOrNull()?.let { data.postValue(Result.failure(it)) }
                return@launch
            }
            if (result.getOrNull()?.errors?.isNotEmpty() == true) {
                data.postValue(Result.failure(Error()))
                return@launch
            }

            result.getOrNull()?.data?.let { data.postValue(Result.success(it)) }
        }
    }
}
