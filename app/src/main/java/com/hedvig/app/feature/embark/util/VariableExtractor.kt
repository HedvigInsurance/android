package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.app.feature.embark.FileVariable
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.util.plus
import com.hedvig.app.util.toJsonObject
import org.json.JSONObject

object VariableExtractor {

    fun extractVariables(variables: List<GraphQLVariablesFragment>, valueStore: ValueStore): JSONObject {
        var regularVariables = variables.mapNotNull {
            it.asEmbarkAPIGraphQLSingleVariable?.let { singleVariable ->
                val storeValue = valueStore.get(singleVariable.from)
                if (storeValue != null) {
                    singleVariable.createSingleVariable(storeValue)
                } else {
                    null
                }
            } ?: it.asEmbarkAPIGraphQLGeneratedVariable?.let { generatedVariable ->
                val variable = generatedVariable.createGeneratedVariable()
                if (variable?.second != null) {
                    valueStore.put(generatedVariable.storeAs, variable.second)
                }
                variable
            }
        }.toJsonObject()

        val multiActionVariables = variables.mapNotNull {
            it.asEmbarkAPIGraphQLMultiActionVariable?.createMultiActionVariables(valueStore::getMultiActionItems)
        }

        multiActionVariables.forEach {
            regularVariables = regularVariables.plus(it)
        }
        return regularVariables
    }

    fun extractFileVariable(variables: List<GraphQLVariablesFragment>, valueStore: ValueStore): List<FileVariable> {
        return variables.mapNotNull {
            it.asEmbarkAPIGraphQLSingleVariable?.let { singleVariable ->
                val storeValue = valueStore.get(singleVariable.from)
                if (storeValue != null && singleVariable.as_ == EmbarkAPIGraphQLSingleVariableCasting.FILE) {
                    FileVariable(
                        key = singleVariable.key,
                        path = storeValue
                    )
                } else {
                    null
                }
            }
        }
    }
}
