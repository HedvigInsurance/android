package com.hedvig.app.feature.insurance.ui.detail.yourinfo

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailViewModel
import com.hedvig.app.feature.table.intoTable
import com.hedvig.app.util.apollo.canHaveAddressChanged
import com.hedvig.app.util.apollo.toUpcomingAgreementResult

fun yourInfoItems(
    contract: InsuranceQuery.Contract,
    movingFlowEnabled: Boolean,
): ContractDetailViewModel.YourInfoViewState.Success {
    val upcomingAgreement = contract
        .fragments
        .upcomingAgreementFragment
        .toUpcomingAgreementResult()
        ?.let { YourInfoModel.PendingAddressChange(it) }
    val topItems = listOfNotNull(upcomingAgreement)
    val table = contract.currentAgreementDetailsTable.fragments.tableFragment.intoTable()
    val bottomItems = listOfNotNull(
        if (movingFlowEnabled && contract.typeOfContract.canHaveAddressChanged()) {
            YourInfoModel.ChangeAddressButton
        } else {
            null
        },
        YourInfoModel.Change,
    )
    return ContractDetailViewModel.YourInfoViewState.Success(
        topItems,
        table,
        bottomItems,
    )
}
