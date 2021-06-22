package com.hedvig.app.feature.insurance.ui.detail.coverage

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem

fun createCoverageItems(contract: InsuranceQuery.Contract): List<CoverageModel> {
    return listOf(
        CoverageModel.Header(contract.typeOfContract)
    ) + contract.perils.map {
        CoverageModel.Peril(it.fragments.perilFragment)
    }
}

fun createInsurableLimitsItems(contract: InsuranceQuery.Contract) = contract.insurableLimits.map {
    it.fragments.insurableLimitsFragment.let { insurableLimitsFragment ->
        InsurableLimitItem.InsurableLimit(
            label = insurableLimitsFragment.label,
            limit = insurableLimitsFragment.limit,
            description = insurableLimitsFragment.description,
        )
    }
}.let { listOf(InsurableLimitItem.Header.MoreInfo) + it }
