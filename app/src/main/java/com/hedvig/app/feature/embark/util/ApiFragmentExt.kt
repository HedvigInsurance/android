package com.hedvig.app.feature.embark.util

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.app.feature.embark.FileVariable
import com.hedvig.app.feature.embark.ValueStore
import org.json.JSONObject

fun ApiFragment.AsEmbarkApiGraphQLQuery.getVariables(valueStore: ValueStore): JSONObject? {
    return queryData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractVariables(it, valueStore) }
}

fun ApiFragment.AsEmbarkApiGraphQLQuery.getFileVariables(valueStore: ValueStore): List<FileVariable> {
    return queryData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
        ?: emptyList()
}

fun ApiFragment.AsEmbarkApiGraphQLMutation.getVariables(valueStore: ValueStore): JSONObject? {
    return mutationData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractVariables(it, valueStore) }
}

fun ApiFragment.AsEmbarkApiGraphQLMutation.getFileVariables(valueStore: ValueStore): List<FileVariable> {
    return mutationData.variables
        .takeIf { it.isNotEmpty() }
        ?.map { it.fragments.graphQLVariablesFragment }
        ?.let { VariableExtractor.extractFileVariable(it, valueStore) }
        ?: emptyList()
}
