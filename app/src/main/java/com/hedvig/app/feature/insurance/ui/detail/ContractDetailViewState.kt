package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.Table

data class ContractDetailViewState(
    val contractCardViewState: ContractCardViewState,
    val memberDetailsViewState: MemberDetailsViewState,
    val coverageViewState: CoverageViewState,
    val documentsViewState: DocumentsViewState,
) {

    data class MemberDetailsViewState(
        val pendingAddressChange: YourInfoModel.PendingAddressChange?,
        val detailsTable: Table,
        val changeAddressButton: YourInfoModel.ChangeAddressButton?,
        val change: YourInfoModel.Change,
    )

    data class CoverageViewState(
        val perils: List<PerilItem>,
        val insurableLimits: List<InsurableLimitItem>,
    )

    data class DocumentsViewState(
        val documents: List<DocumentItems>,
    )
}
