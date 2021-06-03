package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.util.plus
import com.hedvig.app.util.toJsonObject
import org.json.JSONObject

object VariableExtractor {

    fun extractVariables(variables: List<GraphQLVariablesFragment>, valueStore: ValueStore): JSONObject {
        var regularVariables = variables.mapNotNull {
            it.asEmbarkAPIGraphQLSingleVariable?.let {
                val storeValue = valueStore.get(it.from)
                if (storeValue != null) {
                    it.createSingleVariable(storeValue)
                } else {
                    null
                }
            } ?: it.asEmbarkAPIGraphQLGeneratedVariable?.createGeneratedVariable()
        }.toJsonObject()

        val multiActionVariables = variables.mapNotNull {
            it.asEmbarkAPIGraphQLMultiActionVariable?.createMultiActionVariables(valueStore::getMultiActionItems)
        }

        multiActionVariables.forEach {
            regularVariables = regularVariables.plus(it)
        }
        return regularVariables
    }
}
