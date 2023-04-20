package com.hedvig.app.feature.insurance.ui.tab

import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.feature.insurance.ui.detail.toContractCardViewState
import giraffe.InsuranceQuery

fun items(
  insurances: InsuranceQuery.Data,
  crossSells: List<CrossSellData>,
  showCrossSellNotificationBadge: Boolean = false,
): List<InsuranceModel> = ArrayList<InsuranceModel>().apply {
  add(InsuranceModel.Header)
  val contracts = insurances.contracts
    .let { contractModels ->
      if (hasNotOnlyTerminatedContracts(insurances.contracts)) {
        contractModels.filter {
          it.status.fragments.contractStatusFragment.asTerminatedStatus == null
        }
      } else {
        contractModels
      }
    }
    .map { it.toContractCardViewState() }
    .map { InsuranceModel.Contract(it) }

  addAll(contracts)

  if (crossSells.isNotEmpty()) {
    add(InsuranceModel.CrossSellHeader(showCrossSellNotificationBadge))
    addAll(crossSells.map { InsuranceModel.CrossSellCard(it) })
  }

  if (hasNotOnlyTerminatedContracts(insurances.contracts)) {
    val terminatedContracts = amountOfTerminatedContracts(insurances.contracts)
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
