package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.fragment.ApiFragment
import com.hedvig.android.owldroid.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.fragment.GraphQLErrorsFragment
import com.hedvig.android.owldroid.graphql.fragment.GraphQLResultsFragment
import com.hedvig.android.owldroid.graphql.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkAPIGraphQLSingleVariable
import com.hedvig.android.owldroid.graphql.type.EmbarkApiGraphQLMutation
import com.hedvig.android.owldroid.graphql.type.EmbarkApiGraphQLQuery

data class GraphQLApiBuilder(
    private val type: Type,
    private val query: String,
    private val results: List<GraphQLResultsFragment> = emptyList(),
    private val errors: List<GraphQLErrorsFragment> = emptyList(),
    private val variables: List<GraphQLVariablesFragment> = emptyList(),
    private val next: EmbarkLinkFragment,
) {
    fun build() = ApiFragment(
        __typename = type.typename,
        asEmbarkApiGraphQLQuery = if (type == Type.QUERY) {
            ApiFragment.AsEmbarkApiGraphQLQuery(
                __typename = type.typename,
                queryData = ApiFragment.QueryData(
                    query = query,
                    results = results.map {
                        ApiFragment.Result(
                            __typename = "",
                            fragments = ApiFragment.Result.Fragments(
                                it
                            )
                        )
                    },
                    errors = errors.map {
                        ApiFragment.Error(
                            __typename = "",
                            fragments = ApiFragment.Error.Fragments(
                                it
                            )
                        )
                    },
                    variables = variables.map {
                        ApiFragment.Variable(
                            __typename = "",
                            fragments = ApiFragment.Variable.Fragments(
                                it
                            )
                        )
                    },
                    next = ApiFragment.Next(
                        __typename = "",
                        fragments = ApiFragment.Next.Fragments(next)
                    )
                )
            )
        } else {
            null
        },
        asEmbarkApiGraphQLMutation = if (type == Type.MUTATION) {
            ApiFragment.AsEmbarkApiGraphQLMutation(
                __typename = type.typename,
                mutationData = ApiFragment.MutationData(
                    mutation = query,
                    results = results.map {
                        ApiFragment.Result1(
                            __typename = "",
                            fragments = ApiFragment.Result1.Fragments(
                                it
                            )
                        )
                    },
                    errors = errors.map {
                        ApiFragment.Error1(
                            __typename = "",
                            fragments = ApiFragment.Error1.Fragments(
                                it
                            )
                        )
                    },
                    variables = variables.map {
                        ApiFragment.Variable1(
                            __typename = EmbarkAPIGraphQLSingleVariable.type.name,
                            fragments = ApiFragment.Variable1.Fragments(
                                it
                            )
                        )
                    },
                    next = ApiFragment.Next1(
                        __typename = "",
                        fragments = ApiFragment.Next1.Fragments(next)
                    )
                )
            )
        } else {
            null
        }
    )

    enum class Type {
        QUERY,
        MUTATION,
        ;

        val typename: String
            get() = when (this) {
                QUERY -> EmbarkApiGraphQLQuery.type.name
                MUTATION -> EmbarkApiGraphQLMutation.type.name
            }
    }
}
