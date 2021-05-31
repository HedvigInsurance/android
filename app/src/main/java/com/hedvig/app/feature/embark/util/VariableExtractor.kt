package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLVariableGeneratedType
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.util.plus
import com.hedvig.app.util.toJsonObject
import java.util.UUID
import org.json.JSONObject

object VariableExtractor {

    fun extractVariables(variables: List<GraphQLVariablesFragment>, valueStore: ValueStore): JSONObject {
        var regularVariables = variables.mapNotNull {
            extractRegularVariables(it, valueStore)
        }.toJsonObject()

        val multiActionVariables = variables.mapNotNull {
            extractMultiActionVariables(it, valueStore)
        }

        multiActionVariables.forEach {
            regularVariables = regularVariables.plus(it)
        }
        return regularVariables
    }

    private fun extractRegularVariables(
        variable: GraphQLVariablesFragment,
        valueStore: ValueStore
    ): Pair<String, Any>? {
        variable.asEmbarkAPIGraphQLSingleVariable?.let { singleVariable ->
            val storeValue = valueStore.get(singleVariable.from)
            return storeValue?.let {
                createSingleVariable(singleVariable.key, it, singleVariable.as_)
            }
        }
        variable.asEmbarkAPIGraphQLGeneratedVariable?.let { generatedVariable ->
            val generatedValue = UUID.randomUUID()
            valueStore.put(generatedVariable.storeAs, generatedValue.toString())
            return createGeneratedVariable(
                generatedVariable.key,
                generatedVariable.type,
                generatedValue
            )
        }
        return null
    }

    private fun extractMultiActionVariables(
        variable: GraphQLVariablesFragment,
        valueStore: ValueStore
    ): JSONObject? {
        variable.asEmbarkAPIGraphQLMultiActionVariable?.let { multiActionVariables ->
            val multiActionItems = valueStore.getMultiActionItems(multiActionVariables.key)
            val casted = multiActionItems.map { multiActionMap ->
                multiActionVariables.variables.mapNotNull { multiActionVariable ->
                    castMultiActionItems(multiActionVariable, multiActionMap, valueStore)
                }.toJsonObject()
            }
            return JSONObject().put(multiActionVariables.key, casted)
        }
        return null
    }

    private fun castMultiActionItems(
        multiActionVariable: GraphQLVariablesFragment.Variable,
        multiActionMap: Map<String, String>,
        valueStore: ValueStore
    ): Pair<String, Any>? {
        multiActionVariable.asEmbarkAPIGraphQLSingleVariable1?.let { singleVariable ->
            val value = multiActionMap[singleVariable.key]
            if (value != null) {
                return createSingleVariable(
                    key = singleVariable.key,
                    storeValue = value,
                    type = singleVariable.as_
                )
            }
        }
        multiActionVariable.asEmbarkAPIGraphQLGeneratedVariable1?.let { generatedVariable ->
            val value = multiActionMap[generatedVariable.key]
            if (value != null) {
                val generatedValue = UUID.randomUUID()
                valueStore.put(generatedVariable.storeAs, generatedValue.toString())
                return createGeneratedVariable(
                    key = generatedVariable.key,
                    type = generatedVariable.type,
                    generatedValue = generatedValue
                )
            }
        }
        return null
    }

    private fun createSingleVariable(
        key: String,
        storeValue: String,
        type: EmbarkAPIGraphQLSingleVariableCasting
    ): Pair<String, Any>? {
        val value = when (type) {
            EmbarkAPIGraphQLSingleVariableCasting.STRING -> storeValue
            EmbarkAPIGraphQLSingleVariableCasting.INT -> storeValue.toInt()
            EmbarkAPIGraphQLSingleVariableCasting.BOOLEAN -> storeValue.toBoolean()
            // Unsupported generated types are ignored for now.
            EmbarkAPIGraphQLSingleVariableCasting.UNKNOWN__ -> null
        } ?: return null
        return Pair(key, value)
    }

    private fun createGeneratedVariable(
        key: String,
        type: EmbarkAPIGraphQLVariableGeneratedType,
        generatedValue: UUID
    ): Pair<String, String>? {
        return when (type) {
            EmbarkAPIGraphQLVariableGeneratedType.UUID -> Pair(key, generatedValue.toString())
            // Unsupported generated types are ignored for now.
            EmbarkAPIGraphQLVariableGeneratedType.UNKNOWN__ -> null
        }
    }
}
