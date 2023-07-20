package com.hedvig.app.feature.insurance.ui.detail

import com.hedvig.android.core.common.android.ThemedIconUrls
import com.hedvig.android.core.common.android.table.intoTable
import com.hedvig.android.feature.home.legacychangeaddress.toUpcomingAgreementResult
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.util.extensions.canChangeCoInsured
import com.hedvig.app.util.extensions.gradient
import giraffe.InsuranceQuery

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
    changeCoInsured = if (typeOfContract.canChangeCoInsured()) YourInfoModel.Change else null,
    cancelInsuranceData = if (isTerminationFlowEnabled && !isContractTerminated) {
      YourInfoModel.CancelInsuranceData(id, displayName)
    } else {
      null
    },
  )
}

fun InsuranceQuery.Contract.toDocumentsViewState(): ContractDetailViewState.DocumentsViewState {
  return ContractDetailViewState.DocumentsViewState(
    documents = listOfNotNull(
      currentAgreement?.asAgreementCore?.certificateUrl?.let {
        DocumentItems.Document(
          titleRes = hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_CERTIFICATE,
          subTitleRes = hedvig.resources.R.string.insurance_details_view_documents_full_terms_subtitle,
          uriString = it,
        )
      },
      DocumentItems.Document(
        titleRes = hedvig.resources.R.string.MY_DOCUMENTS_INSURANCE_TERMS,
        subTitleRes = hedvig.resources.R.string.insurance_details_view_documents_insurance_letter_subtitle,
        uriString = termsAndConditions.url,
      ),
    ),
  )
}
