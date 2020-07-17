package com.hedvig.app.feature.referrals.ui.tab

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.referrals.ReferralsRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        viewModelScope.launch {
            referralsRepository
                .referrals()
                .onEach { response ->
                    if (response.errors?.isNotEmpty() == true) {
                        data.postValue(Result.failure(Error()))
                        return@onEach
                    }
                    response.data?.let { data.postValue(Result.success(it)) }
                }
                .catch { e ->
                    data.postValue(Result.failure(e))
                }
                .launchIn(this)
        }
    }

    override fun load() {
        viewModelScope.launch {
            runCatching { referralsRepository.reloadReferralsAsync().await() }
        }
    }
}
