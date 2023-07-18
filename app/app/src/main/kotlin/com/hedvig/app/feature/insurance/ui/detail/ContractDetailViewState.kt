package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.android.core.common.android.table.Table
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel

data class ContractDetailViewState(
  val contractCardViewState: ContractCardViewState,
  val memberDetailsViewState: MemberDetailsViewState,
  val documentsViewState: DocumentsViewState,
) {

  data class MemberDetailsViewState(
    val pendingAddressChange: YourInfoModel.PendingAddressChange?,
    val detailsTable: Table,
    val changeAddressButton: YourInfoModel.ChangeAddressButton?,
  )

  data class DocumentsViewState(
    val documents: List<DocumentItems>,
    val cancelInsurance: DocumentItems.CancelInsuranceButton?,
  ) {
    fun getItems(): List<DocumentItems> = documents + listOfNotNull(cancelInsurance)
  }
}
