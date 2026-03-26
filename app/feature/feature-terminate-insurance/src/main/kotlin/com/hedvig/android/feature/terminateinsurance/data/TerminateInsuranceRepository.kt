package com.hedvig.android.feature.terminateinsurance.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.logcat
import kotlinx.datetime.LocalDate
import octopus.DeleteContractMutation
import octopus.TerminateContractMutation
import octopus.TerminationSurveyQuery
import octopus.fragment.TerminationSurveyOptionSuggestionFragment
import octopus.type.TerminationFlowDeleteContractInput
import octopus.type.TerminationFlowSurveyOptionSuggestionType
import octopus.type.TerminationFlowTerminateContractInput

internal interface TerminateInsuranceRepository {
  suspend fun getTerminationSurvey(contractId: String): Either<ErrorMessage, TerminationSurveyData>

  suspend fun terminateContract(
    contractId: String,
    terminationDate: LocalDate,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult>

  suspend fun deleteContract(
    contractId: String,
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
    terminationDate: LocalDate,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> {
    return either {
      val result = apolloClient
        .mutation(
          TerminateContractMutation(
            TerminationFlowTerminateContractInput(
              contractId = contractId,
              terminationDate = terminationDate,
              terminationSurveyOptionId = surveyOptionId,
              terminationComment = Optional.presentIfNotNull(comment),
            ),
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
        .terminateContract
      val userError = result.userError?.message
      if (userError != null) {
        TerminationResult.UserError(userError)
      } else {
        TerminationResult.Success(result.contract?.terminationDate)
      }
    }
  }

  override suspend fun deleteContract(
    contractId: String,
    surveyOptionId: String,
    comment: String?,
  ): Either<ErrorMessage, TerminationResult> {
    return either {
      val result = apolloClient
        .mutation(
          DeleteContractMutation(
            TerminationFlowDeleteContractInput(
              contractId = contractId,
              terminationSurveyOptionId = surveyOptionId,
              terminationComment = Optional.presentIfNotNull(comment),
            ),
          ),
        )
        .safeExecute(::ErrorMessage)
        .bind()
        .deleteContract
      val userError = result.userError?.message
      if (userError != null) {
        TerminationResult.UserError(userError)
      } else {
        TerminationResult.Success(terminationDate = null)
      }
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

private fun TerminationSurveyQuery.Data.TerminationSurvey.Action.toTerminationAction(): TerminationAction {
  return when (this) {
    is TerminationSurveyQuery.Data.TerminationSurvey.TerminationFlowActionTerminateWithDateAction -> {
      TerminationAction.TerminateWithDate(
        minDate = minDate,
        maxDate = maxDate,
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    is TerminationSurveyQuery.Data.TerminationSurvey.TerminationFlowActionDeleteInsuranceAction -> {
      TerminationAction.DeleteInsurance(
        extraCoverageItems = extraCoverage.map { ExtraCoverageItem(it.displayName, it.displayValue) },
      )
    }

    else -> {
      logcat { "Unknown TerminationAction type: ${this::class.simpleName}, falling back to DeleteInsurance" }
      TerminationAction.DeleteInsurance(extraCoverageItems = emptyList())
    }
  }
}

private fun TerminationSurveyOptionSuggestionFragment.toSuggestion(): SurveyOptionSuggestion {
  return SurveyOptionSuggestion(
    type = when (type) {
      TerminationFlowSurveyOptionSuggestionType.UPDATE_ADDRESS -> SuggestionType.UPDATE_ADDRESS
      TerminationFlowSurveyOptionSuggestionType.UPGRADE_COVERAGE -> SuggestionType.UPGRADE_COVERAGE
      TerminationFlowSurveyOptionSuggestionType.DOWNGRADE_PRICE -> SuggestionType.DOWNGRADE_PRICE
      TerminationFlowSurveyOptionSuggestionType.REDIRECT -> SuggestionType.REDIRECT
      TerminationFlowSurveyOptionSuggestionType.INFO -> SuggestionType.INFO
      TerminationFlowSurveyOptionSuggestionType.AUTO_CANCEL_SOLD -> SuggestionType.AUTO_CANCEL_SOLD
      TerminationFlowSurveyOptionSuggestionType.AUTO_CANCEL_SCRAPPED -> SuggestionType.AUTO_CANCEL_SCRAPPED
      TerminationFlowSurveyOptionSuggestionType.AUTO_DECOMMISSION -> SuggestionType.AUTO_DECOMMISSION
      TerminationFlowSurveyOptionSuggestionType.CAR_DECOMMISSION_INFO -> SuggestionType.CAR_DECOMMISSION_INFO
      TerminationFlowSurveyOptionSuggestionType.CAR_ALREADY_DECOMMISSION -> SuggestionType.CAR_ALREADY_DECOMMISSION
      else -> SuggestionType.UNKNOWN
    },
    description = description,
    url = url,
  )
}
