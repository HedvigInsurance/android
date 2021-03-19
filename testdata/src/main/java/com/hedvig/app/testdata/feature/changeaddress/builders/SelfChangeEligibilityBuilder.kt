package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery
import com.hedvig.android.owldroid.type.SelfChangeBlocker

class SelfChangeEligibilityBuilder(
    private val blockers: List<SelfChangeBlocker> = emptyList()
) {

    fun build() = SelfChangeEligibilityQuery.SelfChangeEligibility(
        blockers = blockers
    )
}
