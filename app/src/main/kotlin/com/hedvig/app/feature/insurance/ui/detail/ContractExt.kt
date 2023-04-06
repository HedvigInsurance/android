package com.hedvig.app.feature.insurance.ui.detail

import android.net.Uri
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.apollo.toUpcomingAgreementResult
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
  gradientOption = gradientOption,
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
    change = if (canChangeCoInsured()) YourInfoModel.Change else null,
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

fun InsuranceQuery.Contract.canChangeCoInsured() = when (typeOfContract) {
  TypeOfContract.SE_HOUSE -> true
  TypeOfContract.SE_APARTMENT_BRF -> true
  TypeOfContract.SE_APARTMENT_RENT -> true
  TypeOfContract.SE_APARTMENT_STUDENT_BRF -> true
  TypeOfContract.SE_APARTMENT_STUDENT_RENT -> true
  TypeOfContract.SE_ACCIDENT -> true
  TypeOfContract.SE_ACCIDENT_STUDENT -> true
  TypeOfContract.SE_CAR_TRAFFIC -> false
  TypeOfContract.SE_CAR_HALF -> false
  TypeOfContract.SE_CAR_FULL -> false
  TypeOfContract.SE_GROUP_APARTMENT_RENT -> false
  TypeOfContract.SE_QASA_SHORT_TERM_RENTAL -> false
  TypeOfContract.SE_QASA_LONG_TERM_RENTAL -> false
  TypeOfContract.SE_DOG_BASIC -> false
  TypeOfContract.SE_DOG_STANDARD -> false
  TypeOfContract.SE_DOG_PREMIUM -> false
  TypeOfContract.SE_CAT_BASIC -> false
  TypeOfContract.SE_CAT_STANDARD -> false
  TypeOfContract.SE_CAT_PREMIUM -> false
  TypeOfContract.NO_HOUSE -> true
  TypeOfContract.NO_HOME_CONTENT_OWN -> true
  TypeOfContract.NO_HOME_CONTENT_RENT -> true
  TypeOfContract.NO_HOME_CONTENT_YOUTH_OWN -> true
  TypeOfContract.NO_HOME_CONTENT_YOUTH_RENT -> true
  TypeOfContract.NO_HOME_CONTENT_STUDENT_OWN -> true
  TypeOfContract.NO_HOME_CONTENT_STUDENT_RENT -> true
  TypeOfContract.NO_TRAVEL -> true
  TypeOfContract.NO_TRAVEL_YOUTH -> true
  TypeOfContract.NO_TRAVEL_STUDENT -> true
  TypeOfContract.NO_ACCIDENT -> true
  TypeOfContract.DK_HOME_CONTENT_OWN -> true
  TypeOfContract.DK_HOME_CONTENT_RENT -> true
  TypeOfContract.DK_HOME_CONTENT_STUDENT_OWN -> true
  TypeOfContract.DK_HOME_CONTENT_STUDENT_RENT -> true
  TypeOfContract.DK_HOUSE -> true
  TypeOfContract.DK_ACCIDENT -> true
  TypeOfContract.DK_ACCIDENT_STUDENT -> true
  TypeOfContract.DK_TRAVEL -> true
  TypeOfContract.DK_TRAVEL_STUDENT -> true
  is TypeOfContract.UNKNOWN__ -> false
}


