package com.hedvig.app.feature.insurance.ui.detail

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.AgreementStatus
import com.hedvig.app.R
import com.hedvig.app.feature.chat.data.ChatRepository
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewState
import com.hedvig.app.feature.insurance.ui.detail.coverage.createCoverageItems
import com.hedvig.app.feature.insurance.ui.detail.coverage.createInsurableLimitsItems
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.yourInfoItems
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.table.Table
import com.hedvig.app.util.featureflags.Feature
import com.hedvig.app.util.featureflags.FeatureManager
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class ContractDetailViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val data: InsuranceQuery.Contract) : ViewState()
        object Error : ViewState()
        object Loading : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()

    sealed class YourInfoViewState {
        data class Success(
            val topItems: List<YourInfoModel>,
            val detailsTable: Table,
            val bottomItems: List<YourInfoModel>,
        ) : YourInfoViewState()

        object Loading : YourInfoViewState()
        object Error : YourInfoViewState()
    }

    protected val _yourInfoList = MutableStateFlow<YourInfoViewState>(YourInfoViewState.Loading)
    val yourInfoList = _yourInfoList.asStateFlow()

    protected val _documentsList = MutableLiveData<List<DocumentItems.Document>>()
    val documentsList: LiveData<List<DocumentItems.Document>> = _documentsList

    protected val _coverageViewState = MutableLiveData<CoverageViewState>()
    val coverageViewState: LiveData<CoverageViewState> = _coverageViewState

    abstract fun loadContract(id: String)
    abstract suspend fun triggerFreeTextChat()
}

class ContractDetailViewModelImpl(
    private val getContractsUseCase: GetContractsUseCase,
    private val chatRepository: ChatRepository,
    private val marketManager: MarketManager,
    private val featureRuntimeBehavior: FeatureManager
) : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        viewModelScope.launch {
            when (val insurance = getContractsUseCase()) {
                is GetContractsUseCase.InsuranceResult.Error -> {
                    _data.value = ViewState.Error
                }
                is GetContractsUseCase.InsuranceResult.Insurance -> {
                    insurance.insurance.contracts
                        .firstOrNull { it.id == id }
                        ?.let { contract ->
                            _data.value = ViewState.Success(contract)
                            _yourInfoList.value = yourInfoItems(
                                contract,
                                featureRuntimeBehavior.isFeatureEnabled(Feature.MOVING_FLOW)
                            )
                            _documentsList.postValue(createDocumentItems(contract))
                            _coverageViewState.postValue(
                                CoverageViewState(
                                    createCoverageItems(contract),
                                    createInsurableLimitsItems(contract),
                                )
                            )
                        } ?: run { _data.value = ViewState.Error }
                }
            }
        }
    }

    private fun createDocumentItems(contract: InsuranceQuery.Contract): List<DocumentItems.Document> {
        return if (contract.currentAgreement.asAgreementCore?.status == AgreementStatus.PENDING) {
            // Do not show anything if status is pending
            // TODO: Show error state
            emptyList()
        } else {
            listOfNotNull(
                contract.currentAgreement.asAgreementCore?.certificateUrl?.let {
                    DocumentItems.Document(
                        titleRes = R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
                        subTitleRes = R.string.insurance_details_view_documents_full_terms_subtitle,
                        uri = Uri.parse(it),
                    )
                },
                contract.termsAndConditions.url.let {
                    DocumentItems.Document(
                        titleRes = R.string.MY_DOCUMENTS_INSURANCE_TERMS,
                        subTitleRes = R.string.insurance_details_view_documents_insurance_letter_subtitle,
                        // TODO Quick fix for getting new terms and conditions
                        uri = Uri.parse(
                            if (marketManager.market == Market.SE) {
                                "https://www.hedvig.com/se/villkor"
                            } else {
                                it
                            }
                        ),
                        type = DocumentItems.Document.Type.TERMS_AND_CONDITIONS
                    )
                }
            )
        }
    }

    override suspend fun triggerFreeTextChat() {
        val response = runCatching { chatRepository.triggerFreeTextChat() }
        if (response.isFailure) {
            response.exceptionOrNull()?.let { e(it) }
        }
    }
}
