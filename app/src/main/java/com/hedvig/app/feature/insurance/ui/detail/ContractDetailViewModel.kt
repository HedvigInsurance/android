package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.app.R
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.insurance.data.InsuranceRepository
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageModel
import com.hedvig.app.feature.insurance.ui.detail.documents.DocumentsModel
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.util.apollo.toUpcomingAgreementResult
import e
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {

    protected val _data = MutableLiveData<Result<InsuranceQuery.Contract>>()
    val data: LiveData<Result<InsuranceQuery.Contract>> = _data

    protected val _yourInfoList = MutableLiveData<List<YourInfoModel>>()
    val yourInfoList: LiveData<List<YourInfoModel>> = _yourInfoList

    protected val _documentsList = MutableLiveData<List<DocumentsModel>>()
    val documentsList: LiveData<List<DocumentsModel>> = _documentsList

    protected val _coverageList = MutableLiveData<List<CoverageModel>>()
    val coverageList: LiveData<List<CoverageModel>> = _coverageList

    abstract fun loadContract(id: String)
    abstract suspend fun triggerFreeTextChat()
}

class ContractDetailViewModelImpl(
    private val insuranceRepository: InsuranceRepository,
    private val chatRepository: ChatRepository
) : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        viewModelScope.launch {
            when (val insurance = insuranceRepository()) {
                is InsuranceRepository.InsuranceResult.Error -> {
                    _data.postValue(Result.failure(Throwable(insurance.message)))
                }
                is InsuranceRepository.InsuranceResult.Insurance -> {
                    insurance.insurance.contracts
                        .firstOrNull { it.id == id }
                        ?.let { contract ->
                            _data.postValue(Result.success(contract))
                            _yourInfoList.postValue(createContractItems(contract))
                            _documentsList.postValue(createDocumentItems(contract))
                            _coverageList.postValue(createCoverageItems(contract))
                        } ?: _data.postValue(Result.failure(Throwable("No contract found")))
                }
            }
        }
    }

    private fun createContractItems(contract: InsuranceQuery.Contract): List<YourInfoModel> {
        val contractItems = contract.toModelItems()
        val upcomingAgreement = contract.fragments.upcomingAgreementFragment.toUpcomingAgreementResult()
        val upcomingAgreementItem = upcomingAgreement?.let { YourInfoModel.PendingAddressChange(it) }
        return listOfNotNull(upcomingAgreementItem) + contractItems + listOf(YourInfoModel.Change)
    }

    private fun createDocumentItems(contract: InsuranceQuery.Contract): List<DocumentsModel> {
        return if (contract.currentAgreement.asAgreementCore?.status == AgreementStatus.PENDING) {
            // Do not show anything if status is pending
            // TODO: Show error state
            emptyList()
        } else {
            listOfNotNull(
                contract.currentAgreement.asAgreementCore?.certificateUrl?.let {
                    DocumentsModel(
                        R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
                        R.string.insurance_details_view_documents_full_terms_subtitle,
                        it
                    )
                },
                contract.termsAndConditions.url.let {
                    DocumentsModel(
                        R.string.MY_DOCUMENTS_INSURANCE_TERMS,
                        R.string.insurance_details_view_documents_insurance_letter_subtitle,
                        it
                    )
                }
            )
        }
    }

    private fun createCoverageItems(contract: InsuranceQuery.Contract): List<CoverageModel> {
        return listOf(
            CoverageModel.Header.Perils(contract.typeOfContract)
        ) + contract.perils.map {
            CoverageModel.Peril(it.fragments.perilFragment)
        } + CoverageModel.Header.InsurableLimits + contract.insurableLimits.map {
            CoverageModel.InsurableLimit(it.fragments.insurableLimitsFragment)
        }
    }

    override suspend fun triggerFreeTextChat() {
        val response = runCatching { chatRepository.triggerFreeTextChat() }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
        }
    }
}
