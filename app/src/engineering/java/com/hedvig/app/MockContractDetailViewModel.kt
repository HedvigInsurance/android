package com.hedvig.app

import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT

class MockContractDetailViewModel : ContractDetailViewModel() {
    override fun loadContract(id: String) {
        mockData.contracts.find { it.id == id }?.let { _data.value = it } ?: run {
            _data.value = mockData.contracts[0]
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockData = INSURANCE_DATA_SWEDISH_APARTMENT
    }
}
