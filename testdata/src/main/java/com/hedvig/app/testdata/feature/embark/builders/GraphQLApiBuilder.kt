package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class GraphQLApiBuilder(
    private val query: String,
    private val next: String
) {
    fun build() = EmbarkStoryQuery.Api(
        fragments = EmbarkStoryQuery.Api.Fragments(
            ApiFragment(
                asEmbarkApiGraphQLQuery = ApiFragment.AsEmbarkApiGraphQLQuery(
                    data = ApiFragment.Data(
                        query = query,
                        next = ApiFragment.Next(
                            name = next
                        )
                    )
                )
            )
        )
    )
}
