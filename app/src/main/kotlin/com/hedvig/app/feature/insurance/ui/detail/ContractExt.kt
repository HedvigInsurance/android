package com.hedvig.app.feature.insurance.ui.detail

import android.net.Uri
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.apollo.toUpcomingAgreementResult
import com.hedvig.app.util.extensions.canChangeCoInsured
import com.hedvig.app.util.extensions.gradient
import giraffe.InsuranceQuery
import giraffe.type.TypeOfContract

fun InsuranceQuery.Contract.toContractDetailViewState(
  isTerminationFlowEnabled: Boolean,
): ContractDetailViewState {
  return ContractDetailViewState(
    contractCardViewState = toContractCardViewState(),
    memberDetailsViewState = toMemberDetailsViewState(isTerminationFlowEnabled),
    documentsViewState = toDocumentsViewState(),
  )
}

fun InsuranceQuery.Contract.toContractCardViewState() = ContractCardViewState(
  id = id,
  firstStatusPillText = statusPills.getOrNull(0),
  secondStatusPillText = statusPills.getOrNull(1),
  gradientType = typeOfContract.gradient(),
  displayName = displayName,
  detailPills = detailPills,
  logoUrls = logo?.variants?.fragments?.iconVariantsFragment?.let { ThemedIconUrls.from(it) },
)

fun InsuranceQuery.Contract.toMemberDetailsViewState(
  isTerminationFlowEnabled: Boolean = true,
): ContractDetailViewState.MemberDetailsViewState {
  val isContractTerminated = run {
    val isTerminatedInTheFuture = fragments.upcomingAgreementFragment.status.asTerminatedInFutureStatus != null
    val isTerminatedToday = fragments.upcomingAgreementFragment.status.asTerminatedTodayStatus != null
    isTerminatedInTheFuture || isTerminatedToday
  }
  return ContractDetailViewState.MemberDetailsViewState(
    pendingAddressChange = fragments
      .upcomingAgreementFragment
      .toUpcomingAgreementResult()
      ?.let { YourInfoModel.PendingAddressChange(it) },
    detailsTable = currentAgreementDetailsTable.fragments.tableFragment.intoTable(),
    changeAddressButton = if (supportsAddressChange) {
      YourInfoModel.ChangeAddressButton
    } else {
      null
    },
    change = if (typeOfContract.canChangeCoInsured()) YourInfoModel.Change else null,
    cancelInsurance = if (isTerminationFlowEnabled && !isContractTerminated) {
      YourInfoModel.CancelInsuranceButton(id, displayName)
    } else {
      null
    },
  )
}

fun InsuranceQuery.Contract.toDocumentsViewState() = ContractDetailViewState.DocumentsViewState(
  documents = listOfNotNull(
    currentAgreement?.asAgreementCore?.certificateUrl?.let {
      DocumentItems.Document(
        titleRes = hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
        subTitleRes = hedvig.resources.R.string.insurance_details_view_documents_full_terms_subtitle,
        uri = Uri.parse(it),
      )
    },
    DocumentItems.Document(
      titleRes = hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_TERMS,
      subTitleRes = hedvig.resources.R.string.insurance_details_view_documents_insurance_letter_subtitle,
      uri = Uri.parse(termsAndConditions.url),
      type = DocumentItems.Document.Type.TERMS_AND_CONDITIONS,
    ),
  ),
)


