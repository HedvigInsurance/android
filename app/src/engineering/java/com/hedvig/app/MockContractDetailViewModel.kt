package com.hedvig.app

import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.toContractDetailViewState
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT

class MockContractDetailViewModel : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        if (shouldError) {
            shouldError = false
            _viewState.value = ViewState.Error
        } else {
            val contract = mockData.contracts.first { it.id == id }
            val viewState = contract.toContractDetailViewState(true)
            _viewState.value = ViewState.Success(viewState)
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
