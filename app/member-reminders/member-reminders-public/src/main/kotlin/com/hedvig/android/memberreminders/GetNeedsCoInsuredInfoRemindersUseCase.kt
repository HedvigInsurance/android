package com.hedvig.android.memberreminders

import arrow.core.Either
import arrow.core.NonEmptyList
import arrow.core.left
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.logger.logcat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import octopus.NeedsCoInsuredInfoReminderQuery

internal interface GetNeedsCoInsuredInfoRemindersUseCase {
  fun invoke(): Flow<Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>>>
}

internal class GetNeedsCoInsuredInfoRemindersUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val featureManager: FeatureManager,
) : GetNeedsCoInsuredInfoRemindersUseCase {
  override fun invoke(): Flow<Either<CoInsuredInfoReminderError, NonEmptyList<MemberReminder.CoInsuredInfo>>> {
    return featureManager.isFeatureEnabled(Feature.EDIT_COINSURED).flatMapLatest { isEditCoInsuredFeatureEnabled ->
      if (!isEditCoInsuredFeatureEnabled) {
        flow {
          emit(CoInsuredInfoReminderError.CoInsuredReminderNotEnabled.left())
        }
      } else {
        apolloClient.query(NeedsCoInsuredInfoReminderQuery())
          .fetchPolicy(FetchPolicy.CacheAndNetwork)
          .safeFlow(::ErrorMessage)
          .mapLatest { result: Either<ErrorMessage, NeedsCoInsuredInfoReminderQuery.Data> ->
            either {
              val contracts = result.mapLeft(CoInsuredInfoReminderError::NetworkError)
                .bind()
                .currentMember
                .activeContracts

              val coInsuredReminderInfoList = contracts
                .filter { it.hasMissingInfoAndIsNotTerminating() }
                .map { MemberReminder.CoInsuredInfo(it.id) }
                .toNonEmptyListOrNull()

              ensureNotNull(coInsuredReminderInfoList) {
                CoInsuredInfoReminderError.NoCoInsuredReminders
              }
            }
          }
      }
    }
  }

  private fun NeedsCoInsuredInfoReminderQuery.Data.CurrentMember.ActiveContract.hasMissingInfoAndIsNotTerminating():Boolean {
    return coInsured?.any {
      it.hasMissingInfo && it.terminatesOn == null
    } == true
  }
}

sealed interface CoInsuredInfoReminderError {
  data object NoCoInsuredReminders : CoInsuredInfoReminderError

  data object CoInsuredReminderNotEnabled : CoInsuredInfoReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : CoInsuredInfoReminderError, ErrorMessage by errorMessage
}
