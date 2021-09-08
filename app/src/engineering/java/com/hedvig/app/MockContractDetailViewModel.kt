package com.hedvig.app

import android.net.Uri
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewState
import com.hedvig.app.feature.insurance.ui.detail.coverage.createCoverageItems
import com.hedvig.app.feature.insurance.ui.detail.coverage.createInsurableLimitsItems
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.yourInfoItems
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT

class MockContractDetailViewModel : ContractDetailViewModel() {

    override fun loadContract(id: String) {
        if (shouldError) {
            shouldError = false
            _data.value = ViewState.Error
            return
        } else {
            val contract = mockData.contracts.find { it.id == id }
            contract?.let {
                _data.value = ViewState.Success(it)
                _yourInfoList.value = yourInfoItems(mockData.contracts[0], true)
                _documentsList.value = listOfNotNull(
                    it.currentAgreement.asAgreementCore?.certificateUrl?.let { certificateUrl ->
                        DocumentItems.Document(
                            titleRes = R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
                            subTitleRes = R.string.insurance_details_view_documents_full_terms_subtitle,
                            uri = Uri.parse(certificateUrl)
                        )
                    },
                    DocumentItems.Document(
                        titleRes = R.string.MY_DOCUMENTS_INSURANCE_TERMS,
                        subTitleRes = R.string.insurance_details_view_documents_insurance_letter_subtitle,
                        uri = Uri.parse(it.termsAndConditions.url)
                    )
                )

                _coverageViewState.value = CoverageViewState(
                    createCoverageItems(it),
                    createInsurableLimitsItems(it)
                )
            }
        }
    }

    override suspend fun triggerFreeTextChat() = Unit

    companion object {
        var mockData = INSURANCE_DATA_SWEDISH_APARTMENT
        var shouldError = false
    }
}
