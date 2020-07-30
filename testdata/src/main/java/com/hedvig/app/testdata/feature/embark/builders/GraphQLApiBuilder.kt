package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class GraphQLApiBuilder(
    private val query: String,
    private val results: List<ApiFragment.Result> = emptyList(),
    private val errors: List<ApiFragment.Error> = emptyList(),
    private val next: EmbarkLinkFragment
) {
    fun build() = EmbarkStoryQuery.Api(
        fragments = EmbarkStoryQuery.Api.Fragments(
            ApiFragment(
                asEmbarkApiGraphQLQuery = ApiFragment.AsEmbarkApiGraphQLQuery(
                    data = ApiFragment.Data(
                        query = query,
                        results = results,
                        errors = errors,
                        next = ApiFragment.Next1(
                            fragments = ApiFragment.Next1.Fragments(next)
                        )
                    )
                )
            )
        )
    )
}
