package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import e
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {
    protected val _data = MutableLiveData<Result<InsuranceQuery.Contract>>()
    val data: LiveData<Result<InsuranceQuery.Contract>> = _data

    abstract fun loadContract(id: String)
    abstract suspend fun triggerFreeTextChat()
}

class ContractDetailViewModelImpl(
    private val insuranceRepository: InsuranceRepository,
    private val chatRepository: ChatRepository
) : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val response = runCatching {
                insuranceRepository
                    .dashboardAsync()
                    .await()
            }

            if (response.isFailure) {
                response.exceptionOrNull()?.let { exception ->
                    _data.postValue(Result.failure(exception))
                    e(exception)
                }
                return@launch
            }

            if (response.getOrNull()?.hasErrors() == true) {
                _data.postValue(Result.failure(Error()))
                return@launch
            }

            response
                .getOrNull()
                ?.data
                ?.contracts
                ?.firstOrNull { it.id == id }?.let {contract ->
                    _data.postValue(Result.success(contract))
                }
        }
    }

    override suspend fun triggerFreeTextChat() {
        val response = runCatching { chatRepository.triggerFreeTextChatAsync().await() }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
        }
    }
}
