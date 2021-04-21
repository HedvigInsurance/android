package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.graphql.SelfChangeEligibilityQuery

class SelfChangeEligibilityBuilder(
    val embarkStoryId: String? = null
) {

    fun build() = SelfChangeEligibilityQuery.SelfChangeEligibility(
        embarkStoryId = embarkStoryId
    )
}
