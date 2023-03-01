package com.hedvig.app.feature.insurance.ui.detail

import android.net.Uri
import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.app.feature.documents.DocumentItems
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.insurance.ui.ContractCardViewState
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoModel
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.ThemedIconUrls
import com.hedvig.app.util.apollo.toUpcomingAgreementResult

fun InsuranceQuery.Contract.toContractDetailViewState(
  isTerminationFlowEnabled: Boolean,
): ContractDetailViewState {
  return ContractDetailViewState(
    contractCardViewState = toContractCardViewState(),
    memberDetailsViewState = toMemberDetailsViewState(isTerminationFlowEnabled),
    coverageViewState = toCoverageViewState(),
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

fun InsuranceQuery.Contract.toMemberDetailsViewState(isTerminationFlowEnabled: Boolean = true) =
  ContractDetailViewState.MemberDetailsViewState(
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
    change = YourInfoModel.Change,
    cancelInsurance = if (isTerminationFlowEnabled) YourInfoModel.CancelInsuranceButton(id) else null,
  )

fun InsuranceQuery.Contract.toCoverageViewState() = ContractDetailViewState.CoverageViewState(
  perils = listOf(
    PerilItem.Header.CoversSuffix(displayName),
  ) + contractPerils.map {
    PerilItem.Peril(Peril.from(it.fragments.perilFragment))
  },
  insurableLimits = insurableLimits.map {
    it.fragments.insurableLimitsFragment.let { insurableLimitsFragment ->
      InsurableLimitItem.InsurableLimit(
        label = insurableLimitsFragment.label,
        limit = insurableLimitsFragment.limit,
        description = insurableLimitsFragment.description,
      )
    }
  }.let { listOf(InsurableLimitItem.Header.MoreInfo) + it },
)

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
