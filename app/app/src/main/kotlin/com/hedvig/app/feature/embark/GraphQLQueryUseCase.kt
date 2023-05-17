package com.hedvig.app.feature.embark

import com.adyen.checkout.core.model.getStringOrNull
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.core.common.android.getWithDotNotation
import com.hedvig.android.core.common.android.toStringArray
import com.hedvig.app.util.apollo.FileVariable
import com.hedvig.app.util.apollo.GraphQLQueryHandler
import giraffe.fragment.ApiFragment
import org.json.JSONArray
import org.json.JSONObject

sealed class GraphQLQueryResult {
  data class Error(
    val message: String?,
    val passageName: String,
  ) : GraphQLQueryResult()

  data class ValuesFromResponse(
    val arrayValues: List<Pair<String, List<String>>>,
    val objectValues: List<Pair<String, String?>>,
    val passageName: String?,
  ) : GraphQLQueryResult()
}

class GraphQLQueryUseCase(
  private val graphQLQueryHandler: GraphQLQueryHandler,
) {

  suspend fun executeQuery(
    graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery,
    variables: JSONObject?,
    fileVariables: List<FileVariable>,
  ): GraphQLQueryResult {
    return when (
      val result =
        graphQLQueryHandler.graphQLQuery(graphQLQuery.queryData.query, variables, fileVariables)
    ) {
      is OperationResult.Error -> GraphQLQueryResult.Error(result.message, graphQLQuery.getErrorPassageName())
      is OperationResult.Success -> handleQueryCallSuccess(graphQLQuery, result)
    }
  }

  private fun handleQueryCallSuccess(
    graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery,
    result: OperationResult.Success<JSONObject>,
  ) = when {
    hasErrors(graphQLQuery) || result.data.isNull(DATA_TITLE) -> GraphQLQueryResult.Error(
      result.getErrorMessage(),
      graphQLQuery.getErrorPassageName(),
    )
    else -> parseValuesFromJsonResult(result, graphQLQuery)
  }

  suspend fun executeMutation(
    graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation,
    variables: JSONObject?,
    fileVariables: List<FileVariable>,
  ): GraphQLQueryResult {
    return when (
      val result =
        graphQLQueryHandler.graphQLQuery(graphQLMutation.mutationData.mutation, variables, fileVariables)
    ) {
      is OperationResult.Error -> GraphQLQueryResult.Error(result.message, graphQLMutation.getErrorPassageName())
      is OperationResult.Success -> handleMutationCallSuccess(graphQLMutation, result)
    }
  }

  private fun handleMutationCallSuccess(
    graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation,
    result: OperationResult.Success<JSONObject>,
  ) = when {
    hasErrors(graphQLMutation) || result.data.isNull(DATA_TITLE) -> GraphQLQueryResult.Error(
      result.getErrorMessage(),
      graphQLMutation.getErrorPassageName(),
    )
    else -> parseValuesFromJsonResult(result, graphQLMutation)
  }

  private fun parseValuesFromJsonResult(
    result: OperationResult.Success<JSONObject>,
    graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery,
  ): GraphQLQueryResult.ValuesFromResponse {
    val response = result.data.getJSONObject(DATA_TITLE)
    val arrayValues = mutableListOf<Pair<String, List<String>>>()
    val objectValues = mutableListOf<Pair<String, String?>>()

    graphQLQuery.queryData.results.forEach { r ->
      addValues(
        r.fragments.graphQLResultsFragment.key,
        r.fragments.graphQLResultsFragment.`as`,
        response,
        arrayValues,
        objectValues,
      )
    }
    return GraphQLQueryResult.ValuesFromResponse(
      arrayValues,
      objectValues,
      graphQLQuery.getSuccessPassageName(),
    )
  }

  private fun parseValuesFromJsonResult(
    result: OperationResult.Success<JSONObject>,
    graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation,
  ): GraphQLQueryResult.ValuesFromResponse {
    val response = result.data.getJSONObject(DATA_TITLE)

    val arrayValues = mutableListOf<Pair<String, List<String>>>()
    val objectValues = mutableListOf<Pair<String, String?>>()

    graphQLMutation.mutationData.results.filterNotNull().forEach { r ->
      addValues(
        r.fragments.graphQLResultsFragment.key,
        r.fragments.graphQLResultsFragment.`as`,
        response,
        arrayValues,
        objectValues,
      )
    }
    return GraphQLQueryResult.ValuesFromResponse(arrayValues, objectValues, graphQLMutation.getSuccessPassageName())
  }

  private fun addValues(
    accessor: String,
    key: String,
    response: JSONObject,
    arrayValues: MutableList<Pair<String, List<String>>>,
    objectValues: MutableList<Pair<String, String?>>,
  ) {
    when (val value = response.getWithDotNotation(accessor)) {
      is JSONArray -> arrayValues.add(Pair(key, value.toStringArray()))
      is JSONObject -> objectValues.add(Pair(key, value.toString()))
      JSONObject.NULL -> objectValues.add(Pair(key, null))
      else -> objectValues.add(Pair(key, value.toString()))
    }
  }

  private fun hasErrors(graphQLQuery: ApiFragment.AsEmbarkApiGraphQLQuery) =
    graphQLQuery.queryData.errors.any { it.fragments.graphQLErrorsFragment.contains != null }

  private fun hasErrors(graphQLMutation: ApiFragment.AsEmbarkApiGraphQLMutation) =
    graphQLMutation.mutationData.errors.any { it.fragments.graphQLErrorsFragment.contains != null }

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

  private fun OperationResult.Success<JSONObject>.getErrorMessage(): String? {
    return (data.get(ERROR_TITLE) as JSONArray).getJSONObject(0).getStringOrNull(ERROR_MESSAGE)
  }

  companion object {
    private const val DATA_TITLE = "data"
    private const val ERROR_TITLE = "errors"
    private const val ERROR_MESSAGE = "message"
  }
}
