package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.table.Table

data class ContractDetailViewState(
  val contractCardViewState: ContractCardViewState,
  val memberDetailsViewState: MemberDetailsViewState,
  val documentsViewState: DocumentsViewState,
) {

  data class MemberDetailsViewState(
    val pendingAddressChange: YourInfoModel.PendingAddressChange?,
    val detailsTable: Table,
    val changeAddressButton: YourInfoModel.ChangeAddressButton?,
    val change: YourInfoModel.Change,
    val cancelInsurance: YourInfoModel.CancelInsuranceButton?,
  )

  data class DocumentsViewState(
    val documents: List<DocumentItems>,
  )
}
