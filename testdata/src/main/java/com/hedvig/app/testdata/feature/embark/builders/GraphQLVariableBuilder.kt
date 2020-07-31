package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting

data class GraphQLVariableBuilder(
    private val kind: VariableKind,
    private val key: String,
    private val from: String,
    private val type: EmbarkAPIGraphQLSingleVariableCasting
) {

    fun build() = ApiFragment.Variable(
        asEmbarkAPIGraphQLSingleVariable = if (kind == VariableKind.SINGLE) {
            ApiFragment.AsEmbarkAPIGraphQLSingleVariable(
                key = key,
                from = from,
                as_ = type
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
