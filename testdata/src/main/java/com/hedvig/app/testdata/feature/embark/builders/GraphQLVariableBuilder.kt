package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLVariableGeneratedType

data class GraphQLVariableBuilder(
    private val kind: VariableKind,
    private val key: String,
    private val from: String = "",
    private val singleType: EmbarkAPIGraphQLSingleVariableCasting = EmbarkAPIGraphQLSingleVariableCasting.STRING,
    private val storeAs: String = "",
    private val generatedType: EmbarkAPIGraphQLVariableGeneratedType = EmbarkAPIGraphQLVariableGeneratedType.UUID
) {

    fun build() = GraphQLVariablesFragment(
        asEmbarkAPIGraphQLSingleVariable = if (kind == VariableKind.SINGLE) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
                key = key,
                from = if (from.isEmpty()) {
                    throw Error("Programmer error: attempted to build SingleVariable without providing `from`")
                } else {
                    from
                },
                as_ = singleType
            )
        } else {
            null
        },
        asEmbarkAPIGraphQLGeneratedVariable = if (kind == VariableKind.GENERATED) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable(
                key = key,
                storeAs = if (storeAs.isEmpty()) {
                    throw Error("Programmer error: attempted to build GeneratedVariable without providing `storeAs`")
                } else {
                    storeAs
                },
                type = generatedType
            )
        } else {
            null
        },
        asEmbarkAPIGraphQLMultiActionVariable = if (kind == VariableKind.MULTI_ACTION) {
            GraphQLVariablesFragment.AsEmbarkAPIGraphQLMultiActionVariable(
                key = key,
                variables = listOf(
                    GraphQLVariablesFragment.Variable(
                        asEmbarkAPIGraphQLGeneratedVariable1 = GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable1(
                            key = key,
                            storeAs = if (storeAs.isEmpty()) {
                                throw Error("Programmer error: attempted to build GeneratedVariable without providing `storeAs`")
                            } else {
                                storeAs
                            },
                            type = generatedType
                        ),
                        asEmbarkAPIGraphQLSingleVariable1 = null
                    )
                )
            )
        } else {
            null
        }
    )

    enum class VariableKind {
        SINGLE,
        GENERATED,
        MULTI_ACTION
    }
}
