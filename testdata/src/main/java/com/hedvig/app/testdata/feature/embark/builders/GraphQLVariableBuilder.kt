package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
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

    fun build() = ApiFragment.Variable(
        asEmbarkAPIGraphQLSingleVariable = if (kind == VariableKind.SINGLE) {
            ApiFragment.AsEmbarkAPIGraphQLSingleVariable(
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
            ApiFragment.AsEmbarkAPIGraphQLGeneratedVariable(
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
        }
    )

    enum class VariableKind {
        SINGLE,
        GENERATED,
        MULTI_ACTION
    }
}
