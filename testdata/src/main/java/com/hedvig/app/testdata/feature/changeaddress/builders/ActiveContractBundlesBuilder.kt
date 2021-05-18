package com.hedvig.app.testdata.feature.changeaddress.builders

import com.hedvig.android.owldroid.graphql.ActiveContractBundlesQuery

class ActiveContractBundlesBuilder(
    val embarkStoryId: String? = null
) {

    fun build() = ActiveContractBundlesQuery.ActiveContractBundle(
        angelStories = ActiveContractBundlesQuery.AngelStories(
            addressChange = embarkStoryId
        )
    )
}
