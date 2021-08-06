package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLVariableGeneratedType
import com.hedvig.app.util.toJsonArray
import com.hedvig.app.util.toJsonObject
import org.json.JSONObject
import java.util.UUID

fun GraphQLVariablesFragment.Variable.toFragment() = GraphQLVariablesFragment(
    asEmbarkAPIGraphQLSingleVariable = asEmbarkAPIGraphQLSingleVariable1?.let { single ->
        GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable(
            key = single.key,
            from = single.from,
            as_ = single.as_,
        )
    },
    asEmbarkAPIGraphQLGeneratedVariable = asEmbarkAPIGraphQLGeneratedVariable1?.let { generated ->
        GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable(
            key = generated.key,
            storeAs = generated.storeAs,
            type = generated.type,
        )
    },
    asEmbarkAPIGraphQLMultiActionVariable = null
)

fun GraphQLVariablesFragment.createKeyValuePairs(getStoredValue: (String) -> String?): Pair<String, Any>? {
    return asEmbarkAPIGraphQLSingleVariable?.let {
        val storeValue = getStoredValue(it.from)
        if (storeValue != null) {
            it.createSingleVariable(storeValue)
        } else {
            null
        }
    } ?: asEmbarkAPIGraphQLGeneratedVariable?.createGeneratedVariable()
}

fun GraphQLVariablesFragment.AsEmbarkAPIGraphQLSingleVariable.createSingleVariable(
    storeValue: String,
): Pair<String, Any>? {
    val value = when (as_) {
        EmbarkAPIGraphQLSingleVariableCasting.STRING -> storeValue
        EmbarkAPIGraphQLSingleVariableCasting.INT -> {
            try {
                storeValue.toInt()
            } catch (exception: NumberFormatException) {
                // The stored value can in some cases be floats, eg. for computed store values
                storeValue.toFloat().toInt()
            }
        }
        EmbarkAPIGraphQLSingleVariableCasting.BOOLEAN -> storeValue.toBoolean()
        // Unsupported generated types are ignored for now.
        EmbarkAPIGraphQLSingleVariableCasting.UNKNOWN__ -> null
    } ?: return null
    return Pair(key, value)
}

fun GraphQLVariablesFragment.AsEmbarkAPIGraphQLGeneratedVariable.createGeneratedVariable(
    generatedValue: UUID = UUID.randomUUID()
): Pair<String, String>? {
    return when (type) {
        EmbarkAPIGraphQLVariableGeneratedType.UUID -> Pair(key, generatedValue.toString())
        // Unsupported generated types are ignored for now.
        EmbarkAPIGraphQLVariableGeneratedType.UNKNOWN__ -> null
    }
}

fun GraphQLVariablesFragment.AsEmbarkAPIGraphQLMultiActionVariable.createMultiActionVariables(
    getMultiActionItems: (String) -> List<Map<String, String>>
): JSONObject? {
    return getMultiActionItems(key)
        .map { multiActionMap ->
            variables
                .map { it.toFragment() }
                .mapNotNull { multiActionVariable -> multiActionVariable.createKeyValuePairs(multiActionMap::get) }
                .toJsonObject()
        }
        .takeIf { it.isNotEmpty() }
        ?.let { JSONObject().put(key, it.toJsonArray()) }
}
