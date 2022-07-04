package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.graphql.fragment.ApiFragment
import com.hedvig.android.owldroid.graphql.fragment.GraphQLVariablesFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.app.feature.embark.ValueStore
import com.hedvig.app.feature.embark.variables.CastType
import com.hedvig.app.feature.embark.variables.Variable
import com.hedvig.app.feature.embark.variables.VariableExtractor
import com.hedvig.app.util.apollo.FileVariable
import org.json.JSONObject

fun ApiFragment.AsEmbarkApiGraphQLQuery.getVariables(valueStore: ValueStore): JSONObject? {
    return queryData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.mapNotNull { it.toVariable() }
        ?.let {
            VariableExtractor.reduceVariables(
                it,
                valueStore::get,
                valueStore::put,
                valueStore::getMultiActionItems,
            )
        }
}

fun ApiFragment.AsEmbarkApiGraphQLMutation.getVariables(valueStore: ValueStore): JSONObject? {
    return mutationData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.mapNotNull { it.toVariable() }
        ?.let {
            VariableExtractor.reduceVariables(
                it,
                valueStore::get,
                valueStore::put,
                valueStore::getMultiActionItems,
            )
        }
}

private fun GraphQLVariablesFragment.toVariable(): Variable? {
    return when {
        asEmbarkAPIGraphQLSingleVariable != null -> {
            Variable.Single(
                asEmbarkAPIGraphQLSingleVariable!!.key,
                asEmbarkAPIGraphQLSingleVariable!!.from,
                asEmbarkAPIGraphQLSingleVariable!!.`as`.toCast(),
            )
        }
        asEmbarkAPIGraphQLMultiActionVariable != null -> {
            Variable.Multi(
                asEmbarkAPIGraphQLMultiActionVariable!!.key,
                asEmbarkAPIGraphQLMultiActionVariable!!.from,
                asEmbarkAPIGraphQLMultiActionVariable!!.variables.mapNotNull {
                    when {
                        it.asEmbarkAPIGraphQLGeneratedVariable1 != null -> {
                            Variable.Generated(
                                it.asEmbarkAPIGraphQLGeneratedVariable1!!.key,
                                it.asEmbarkAPIGraphQLGeneratedVariable1!!.storeAs,
                            )
                        }
                        it.asEmbarkAPIGraphQLSingleVariable1 != null -> {
                            Variable.Single(
                                it.asEmbarkAPIGraphQLSingleVariable1!!.key,
                                it.asEmbarkAPIGraphQLSingleVariable1!!.from,
                                it.asEmbarkAPIGraphQLSingleVariable1!!.`as`.toCast(),
                            )
                        }
                        else -> {
                            null
                        }
                    }
                },
            )
        }
        asEmbarkAPIGraphQLConstantVariable != null -> {
            Variable.Constant(
                asEmbarkAPIGraphQLConstantVariable!!.key,
                asEmbarkAPIGraphQLConstantVariable!!.value,
                asEmbarkAPIGraphQLConstantVariable!!.`as`.toCast(),
            )
        }
        asEmbarkAPIGraphQLGeneratedVariable != null -> {
            Variable.Generated(
                asEmbarkAPIGraphQLGeneratedVariable!!.key,
                asEmbarkAPIGraphQLGeneratedVariable!!.storeAs,
            )
        }
        else -> {
            null
        }
    }
}

private fun EmbarkAPIGraphQLSingleVariableCasting.toCast() = when (this) {
    EmbarkAPIGraphQLSingleVariableCasting.string -> CastType.STRING
    EmbarkAPIGraphQLSingleVariableCasting.int -> CastType.INT
    EmbarkAPIGraphQLSingleVariableCasting.boolean -> CastType.BOOLEAN
    EmbarkAPIGraphQLSingleVariableCasting.file -> CastType.FILE
    EmbarkAPIGraphQLSingleVariableCasting.UNKNOWN__ -> CastType.UNKNOWN
}

fun ApiFragment.AsEmbarkApiGraphQLQuery.getFileVariables(valueStore: ValueStore): List<FileVariable> {
    return queryData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
        ?: emptyList()
}

fun ApiFragment.AsEmbarkApiGraphQLMutation.getFileVariables(valueStore: ValueStore): List<FileVariable> {
    return mutationData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
        ?: emptyList()
}
