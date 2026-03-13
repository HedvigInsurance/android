package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import kotlinx.datetime.LocalDate
import octopus.TerminateContractMutation
import octopus.TerminationSurveyQuery
import octopus.type.TerminateContractInput

internal interface TerminateInsuranceRepository {
  suspend fun getTerminationSurvey(contractId: String): Either<ErrorMessage, TerminationSurveyData>

  suspend fun terminateContract(
    contractId: String,
    terminationDate: LocalDate?,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult>
}

internal class TerminateInsuranceRepositoryImpl(
  private val apolloClient: ApolloClient,
) : TerminateInsuranceRepository {
  override suspend fun getTerminationSurvey(contractId: String): Either<ErrorMessage, TerminationSurveyData> {
    return either {
      val result = apolloClient
        .query(TerminationSurveyQuery(contractId))
        .safeExecute(::ErrorMessage)
        .bind()
        .terminationSurvey
      result.toTerminationSurveyData()
    }
  }

  override suspend fun terminateContract(
    contractId: String,
    terminationDate: LocalDate?,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> {
    return either {
      val result = apolloClient
        .mutation(
          TerminateContractMutation(
            TerminateContractInput(
              contractId = contractId,
              terminationDate = Optional.presentIfNotNull(terminationDate),
              terminationSurveyOptionId = surveyOptionId,
              terminationComment = Optional.presentIfNotNull(comment),
            ),
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
        .terminateContract
      TerminationResult(
        terminationDate = result.terminationDate,
        userError = result.userError?.message,
      )
    }
  }
}

private fun TerminationSurveyQuery.Data.TerminationSurvey.toTerminationSurveyData(): TerminationSurveyData {
  return TerminationSurveyData(
    options = options.mapIndexed { index, option -> option.toTerminationSurveyOption(index) },
    action = action.toTerminationAction(),
  )
}

private fun TerminationSurveyQuery.Data.TerminationSurvey.Option.toTerminationSurveyOption(
  index: Int,
): TerminationSurveyOption {
  return TerminationSurveyOption(
    id = id,
    title = title,
    listIndex = index,
    feedbackRequired = feedbackRequired,
    suggestion = suggestion?.toSuggestion(),
    subOptions = subOptions.mapIndexed { subIndex, subOption ->
      TerminationSurveyOption(
        id = subOption.id,
        title = subOption.title,
        listIndex = subIndex,
        feedbackRequired = subOption.feedbackRequired,
        suggestion = subOption.suggestion?.toSuggestion(),
        subOptions = emptyList(),
      )
    },
  )
}

// Note: The exact Apollo-generated class names depend on the schema.
// These will need adjusting once the real schema is available and codegen runs.
private fun TerminationSurveyQuery.Data.TerminationSurvey.Action.toTerminationAction(): TerminationAction {
  return when (this) {
    is TerminationSurveyQuery.Data.TerminationSurvey.TerminateWithDateAction -> {
      TerminationAction.TerminateWithDate(
        minDate = minDate,
        maxDate = maxDate,
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    is TerminationSurveyQuery.Data.TerminationSurvey.DeleteInsuranceAction -> {
      TerminationAction.DeleteInsurance(
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    else -> {
      TerminationAction.DeleteInsurance(extraCoverageItems = emptyList())
    }
  }
}

private fun TerminationSurveyQuery.Data.TerminationSurvey.Option.Suggestion.toSuggestion(): SurveyOptionSuggestion {
  return SurveyOptionSuggestion(
    type = when (type) {
      octopus.type.TerminationSurveyOptionSuggestionType.UPDATE_ADDRESS -> SuggestionType.UPDATE_ADDRESS
      octopus.type.TerminationSurveyOptionSuggestionType.UPGRADE_COVERAGE -> SuggestionType.UPGRADE_COVERAGE
      octopus.type.TerminationSurveyOptionSuggestionType.DOWNGRADE_PRICE -> SuggestionType.DOWNGRADE_PRICE
      octopus.type.TerminationSurveyOptionSuggestionType.REDIRECT -> SuggestionType.REDIRECT
      octopus.type.TerminationSurveyOptionSuggestionType.INFO -> SuggestionType.INFO
      octopus.type.TerminationSurveyOptionSuggestionType.AUTO_DECOMMISSION -> SuggestionType.AUTO_DECOMMISSION
      octopus.type.TerminationSurveyOptionSuggestionType.AUTO_CANCEL -> SuggestionType.AUTO_CANCEL
      else -> SuggestionType.UNKNOWN
    },
    description = description,
    url = url,
  )
}
