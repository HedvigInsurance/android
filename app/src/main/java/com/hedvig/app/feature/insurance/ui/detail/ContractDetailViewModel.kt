package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import e
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {

    val yourInfoListItemBuilder = YourInfoListItemBuilder()

    protected val _data = MutableLiveData<Result<InsuranceQuery.Contract>>()
    val data: LiveData<Result<InsuranceQuery.Contract>> = _data

    protected val _yourInfoList = MutableLiveData<List<YourInfoModel>>()
    val yourInfoList: LiveData<List<YourInfoModel>> = _yourInfoList

    abstract fun loadContract(id: String)
    abstract suspend fun triggerFreeTextChat()
}

class ContractDetailViewModelImpl(
    private val insuranceRepository: InsuranceRepository,
    private val chatRepository: ChatRepository,
    private val getUpcomingAgreement: GetUpcomingAgreementUseCase
) : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        viewModelScope.launch {
            val upcomingAgreementItem = getUpComingAgreement()

            val response = runCatching { insuranceRepository.insurance() }

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

            response.getOrNull()
                ?.data
                ?.contracts
                ?.firstOrNull { it.id == id }
                ?.let { contract ->
                    val list = yourInfoListItemBuilder.createYourInfoList(contract, upcomingAgreementItem)
                    _yourInfoList.postValue(list)
                }
        }
    }

    private suspend fun getUpComingAgreement() = when (val upcomingAgreement = getUpcomingAgreement()) {
        is GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange -> null
        is GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement -> YourInfoModel.PendingAddressChange(upcomingAgreement)
        is GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error -> YourInfoModel.PendingAddressChange(null)
    }


    override suspend fun triggerFreeTextChat() {
        val response = runCatching { chatRepository.triggerFreeTextChat() }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
        }
    }
}
