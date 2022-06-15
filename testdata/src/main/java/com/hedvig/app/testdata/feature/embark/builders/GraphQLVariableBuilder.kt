package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.graphql.type.EmbarkAPIGraphQLVariableGeneratedType

data class GraphQLVariableBuilder(
    private val kind: VariableKind,
    private val key: String,
    private val from: String = "",
    private val singleType: EmbarkAPIGraphQLSingleVariableCasting = EmbarkAPIGraphQLSingleVariableCasting.string,
    private val storeAs: String = "",
    private val generatedType: EmbarkAPIGraphQLVariableGeneratedType = EmbarkAPIGraphQLVariableGeneratedType.uuid,
) {

    fun build() = GraphQLVariablesFragment(
        __typename = "",
        asEmbarkAPIGraphQLSingleVariable = if (kind == VariableKind.SINGLE) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                __typename = "",
                key = key,
                from = from.ifEmpty {
                    throw Error("Programmer error: attempted to build SingleVariable without providing `from`")
                },
                `as` = singleType,
            )
        } else {
            null
        },
        asEmbarkAPIGraphQLGeneratedVariable = if (kind == VariableKind.GENERATED) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable(
                __typename = "",
                key = key,
                storeAs = storeAs.ifEmpty {
                    throw Error("Programmer error: attempted to build GeneratedVariable without providing `storeAs`")
                },
                type = generatedType,
            )
        } else {
            null
        },
        asEmbarkAPIGraphQLMultiActionVariable = if (kind == VariableKind.MULTI_ACTION) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLMultiActionVariable(
                __typename = "",
                key = key,
                from = from,
                variables = listOf(
                    GraphQLVariablesFragment.Variable(
                        __typename = "",
                        asEmbarkAPIGraphQLGeneratedVariable1 = GraphQLVariablesFragment
                            .AsEmbarkAPIGraphQLGeneratedVariable1(
                                __typename = "",
                                key = key,
                                storeAs = storeAs.ifEmpty {
                                    throw Error(
                                        "Programmer error: attempted to build" +
                                            " GeneratedVariable without providing `storeAs`",
                                    )
                                },
                                type = generatedType,
                            ),
                        asEmbarkAPIGraphQLSingleVariable1 = null,
                    ),
                ),
            )
        } else {
            null
        },
        asEmbarkAPIGraphQLConstantVariable = null
    )

    enum class VariableKind {
        SINGLE,
        GENERATED,
        MULTI_ACTION
    }
}
