package com.hedvig.app.feature.embark

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.getWithDotNotation
import com.hedvig.app.util.toStringArray
import org.json.JSONArray
import org.json.JSONObject

interface GraphQLQueryUseCase {
    suspend fun executeQuery(
        graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery,
        variables: JSONObject?,
        fileVariables: List<FileVariable>
    ): GraphQLQueryResult

    suspend fun executeMutation(
        graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation,
        variables: JSONObject?,
        fileVariables: List<FileVariable>
    ): GraphQLQueryResult
}

sealed class GraphQLQueryResult {
    data class Error(
        val message: String?,
        val passageName: String
    ) : GraphQLQueryResult()

    data class ValuesFromResponse(
        val arrayValues: List<Pair<String, List<String>>>,
        val objectValues: List<Pair<String, String>>,
        val passageName: String?
    ) : GraphQLQueryResult()
}

class GraphQLQueryUseCaseImpl(
    private val embarkRepository: EmbarkRepository
) : GraphQLQueryUseCase {

    override suspend fun executeQuery(
        graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery,
        variables: JSONObject?,
        fileVariables: List<FileVariable>
    ): GraphQLQueryResult {
        return when (
            val result =
                embarkRepository.graphQLQuery(graphQLQuery.queryData.query, variables, fileVariables)
        ) {
            is QueryResult.Error -> GraphQLQueryResult.Error(result.message, graphQLQuery.getErrorPassageName())
            is QueryResult.Success -> parseValuesFromJsonResult(result, graphQLQuery)
        }
    }

    override suspend fun executeMutation(
        graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation,
        variables: JSONObject?,
        fileVariables: List<FileVariable>
    ): GraphQLQueryResult {
        return when (
            val result =
                embarkRepository.graphQLQuery(graphQLMutation.mutationData.mutation, variables, fileVariables)
        ) {
            is QueryResult.Error -> GraphQLQueryResult.Error(result.message, graphQLMutation.getErrorPassageName())
            is QueryResult.Success -> parseValuesFromJsonResult(result, graphQLMutation)
        }
    }

    private fun parseValuesFromJsonResult(
        result: QueryResult.Success<JSONObject>,
        graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery
    ): GraphQLQueryResult.ValuesFromResponse {

        val response = result.data.getJSONObject("data")
        val arrayValues = mutableListOf<Pair<String, List<String>>>()
        val objectValues = mutableListOf<Pair<String, String>>()

        graphQLQuery.queryData.results.forEach { r ->
            addValues(
                r.fragments.graphQLResultsFragment.key,
                r.fragments.graphQLResultsFragment.as_,
                response,
                arrayValues,
                objectValues
            )
        }
        return GraphQLQueryResult.ValuesFromResponse(
            arrayValues,
            objectValues,
            graphQLQuery.getSuccessPassageName()
        )
    }

    private fun parseValuesFromJsonResult(
        result: QueryResult.Success<JSONObject>,
        graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation
    ): GraphQLQueryResult.ValuesFromResponse {
        val response = result.data.getJSONObject("data")

        val arrayValues = mutableListOf<Pair<String, List<String>>>()
        val objectValues = mutableListOf<Pair<String, String>>()

        graphQLMutation.mutationData.results.filterNotNull().forEach { r ->
            addValues(
                r.fragments.graphQLResultsFragment.key,
                r.fragments.graphQLResultsFragment.as_,
                response,
                arrayValues,
                objectValues
            )
        }
        return GraphQLQueryResult.ValuesFromResponse(arrayValues, objectValues, graphQLMutation.getSuccessPassageName())
    }

    private fun addValues(
        accessor: String,
        key: String,
        response: JSONObject,
        arrayValues: MutableList<Pair<String, List<String>>>,
        objectValues: MutableList<Pair<String, String>>
    ) {
        when (val value = response.getWithDotNotation(accessor)) {
            is JSONArray -> arrayValues.add(Pair(key, value.toStringArray()))
            is JSONObject -> objectValues.add(Pair(key, value.toString()))
            else -> objectValues.add(Pair(key, value.toString()))
        }
    }

    private fun ApiFragment.AsEmbarkApiGraphQLQuery.getSuccessPassageName() =
        queryData.next?.fragments?.embarkLinkFragment?.name

    private fun ApiFragment.AsEmbarkApiGraphQLQuery.getErrorPassageName() = queryData
        .errors.first().fragments.graphQLErrorsFragment
        .next.fragments.embarkLinkFragment.name

    private fun ApiFragment.AsEmbarkApiGraphQLMutation.getSuccessPassageName() =
        mutationData.next?.fragments?.embarkLinkFragment?.name

    private fun ApiFragment.AsEmbarkApiGraphQLMutation.getErrorPassageName() = mutationData
        .errors.first().fragments.graphQLErrorsFragment
        .next.fragments.embarkLinkFragment.name
}
