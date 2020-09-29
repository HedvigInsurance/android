package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class GraphQLApiBuilder(
    private val query: String,
    private val results: List<ApiFragment.Result> = emptyList(),
    private val errors: List<ApiFragment.Error> = emptyList(),
    private val variables: List<ApiFragment.Variable> = emptyList(),
    private val next: EmbarkLinkFragment
) {
    fun build() = EmbarkStoryQuery.Api(
        fragments = EmbarkStoryQuery.Api.Fragments(
            ApiFragment(
                asEmbarkApiGraphQLQuery = ApiFragment.AsEmbarkApiGraphQLQuery(
                    queryData = ApiFragment.QueryData(
                        query = query,
                        results = results,
                        errors = errors,
                        variables = variables,
                        next = ApiFragment.Next(
                            fragments = ApiFragment.Next.Fragments(next)
                        )
                    )
                ),
                asEmbarkApiGraphQLMutation = null
            )
        )
    )
}
