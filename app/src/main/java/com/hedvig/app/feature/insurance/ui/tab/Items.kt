package com.hedvig.app.feature.insurance.ui.tab

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.insurance.ui.InsuranceModel

fun items(data: InsuranceQuery.Data): List<InsuranceModel> = ArrayList<InsuranceModel>().apply {
    add(InsuranceModel.Header)
    val contracts = data.contracts.map(InsuranceModel::Contract).let { contractModels ->
        if (hasNotOnlyTerminatedContracts(data.contracts)) {
            contractModels.filter {
                it.inner.status.fragments.contractStatusFragment.asTerminatedStatus == null
            }
        } else {
            contractModels
        }
    }
    addAll(contracts)

    val potentialCrossSells = data.activeContractBundles.flatMap { it.potentialCrossSells }
    if (potentialCrossSells.isNotEmpty()) {
        add(InsuranceModel.CrossSellHeader)
        addAll(potentialCrossSells.map(::crossSell))
    }

    if (hasNotOnlyTerminatedContracts(data.contracts)) {
        val terminatedContracts = amountOfTerminatedContracts(data.contracts)
        add(InsuranceModel.TerminatedContractsHeader)
        add(InsuranceModel.TerminatedContracts(terminatedContracts))
    }
}

private fun hasNotOnlyTerminatedContracts(contracts: List<InsuranceQuery.Contract>): Boolean {
    val terminatedContracts = amountOfTerminatedContracts(contracts)
    return (terminatedContracts > 0 && contracts.size != terminatedContracts)
}

private fun amountOfTerminatedContracts(contracts: List<InsuranceQuery.Contract>) =
    contracts.filter { it.status.fragments.contractStatusFragment.asTerminatedStatus != null }.size

private fun crossSell(potentialCrossSell: InsuranceQuery.PotentialCrossSell) =
    InsuranceModel.CrossSellCard(CrossSellData.from(potentialCrossSell))
