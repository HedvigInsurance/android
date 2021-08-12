package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.GraphQLErrorsFragment
import com.hedvig.android.owldroid.fragment.GraphQLResultsFragment
import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment

data class GraphQLApiBuilder(
    private val type: Type,
    private val query: String,
    private val results: List<GraphQLResultsFragment> = emptyList(),
    private val errors: List<GraphQLErrorsFragment> = emptyList(),
    private val variables: List<GraphQLVariablesFragment> = emptyList(),
    private val next: EmbarkLinkFragment
) {
    fun build() = ApiFragment(
        asEmbarkApiGraphQLQuery = if (type == Type.QUERY) {
            ApiFragment.AsEmbarkApiGraphQLQuery(
                queryData = ApiFragment.QueryData(
                    query = query,
                    results = results.map {
                        ApiFragment.Result(
                            fragments = ApiFragment.Result.Fragments(
                                it
                            )
                        )
                    },
                    errors = errors.map {
                        ApiFragment.Error(
                            fragments = ApiFragment.Error.Fragments(
                                it
                            )
                        )
                    },
                    variables = variables.map {
                        ApiFragment.Variable(
                            fragments = ApiFragment.Variable.Fragments(
                                it
                            )
                        )
                    },
                    next = ApiFragment.Next(
                        fragments = ApiFragment.Next.Fragments(next)
                    )
                )
            )
        } else {
            null
        },
        asEmbarkApiGraphQLMutation = if (type == Type.MUTATION) {
            ApiFragment.AsEmbarkApiGraphQLMutation(
                mutationData = ApiFragment.MutationData(
                    mutation = query,
                    results = results.map {
                        ApiFragment.Result1(
                            fragments = ApiFragment.Result1.Fragments(
                                it
                            )
                        )
                    },
                    errors = errors.map {
                        ApiFragment.Error1(
                            fragments = ApiFragment.Error1.Fragments(
                                it
                            )
                        )
                    },
                    variables = variables.map {
                        ApiFragment.Variable1(
                            fragments = ApiFragment.Variable1.Fragments(
                                it
                            )
                        )
                    },
                    next = ApiFragment.Next1(fragments = ApiFragment.Next1.Fragments(next))
                )
            )
        } else {
            null
        }
    )

    enum class Type {
        QUERY,
        MUTATION
    }
}
