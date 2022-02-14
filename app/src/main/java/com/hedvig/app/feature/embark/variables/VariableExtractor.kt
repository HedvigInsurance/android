package com.hedvig.app.feature.embark.variables

import com.hedvig.android.owldroid.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.app.feature.embark.FileVariable
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.util.createAndAddWithLodashNotation
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object VariableExtractor {

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

    fun reduceVariables(
        variables: List<Variable>,
        getValue: (key: String) -> String?,
        setValue: (key: String, value: String?) -> Unit,
        getMultiActionItems: (String) -> List<Map<String, String>>
    ): JSONObject {
        return variables.fold(JSONObject("{}")) { acc, variable ->
            when (variable) {
                is Variable.Constant -> acc.createAndAddWithLodashNotation(
                    value = variable.castAs.cast(variable.value),
                    key = variable.key,
                    currentKey = variable.key.substringBefore(".")
                )
                is Variable.Single -> acc.createAndAddWithLodashNotation(
                    value = variable.castAs.cast(getValue(variable.from)),
                    key = variable.key,
                    currentKey = variable.key.substringBefore(".")
                )
                is Variable.Generated -> {
                    val generatedValue = UUID.randomUUID().toString()
                    setValue(variable.storeAs, generatedValue)
                    acc.createAndAddWithLodashNotation(
                        value = generatedValue,
                        key = variable.key,
                        currentKey = variable.key.substringBefore(".")
                    )
                }
                is Variable.Multi -> {
                    acc.put(variable.key, JSONArray())
                    getMultiActionItems(variable.key).mapIndexed { index, map ->
                        val multiActionJson = reduceVariables(
                            variable.variables,
                            map::get,
                            setValue,
                            getMultiActionItems
                        )
                        acc.getJSONArray(variable.key).put(index, multiActionJson)
                    }
                }
            }

            acc
        }
    }
}
