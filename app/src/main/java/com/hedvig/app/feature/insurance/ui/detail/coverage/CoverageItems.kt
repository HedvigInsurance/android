package com.hedvig.app.feature.insurance.ui.detail.coverage

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurablelimits.InsurableLimitItem
import com.hedvig.app.feature.perils.Peril
import com.hedvig.app.feature.perils.PerilItem

fun createCoverageItems(contract: InsuranceQuery.Contract): List<PerilItem> {
    return listOf(
        PerilItem.Header(contract.typeOfContract)
    ) + contract.perils.map {
        PerilItem.Peril(Peril.from(it.fragments.perilFragment))
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
