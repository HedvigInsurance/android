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
import com.hedvig.android.data.coinsured.CoInsuredFlowType
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.featureflags.flags.Feature
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
              val coInsuredReminderInfoList = result.mapLeft(CoInsuredInfoReminderError::NetworkError)
                .bind()
                .currentMember
                .activeContracts
                .toCoInsuredInfoList()
                .toNonEmptyListOrNull()

              ensureNotNull(coInsuredReminderInfoList) {
                CoInsuredInfoReminderError.NoCoInsuredReminders
              }
            }
          }
      }
    }
  }

  private fun List<NeedsCoInsuredInfoReminderQuery.Data.CurrentMember.ActiveContract>.toCoInsuredInfoList():
    List<MemberReminder.CoInsuredInfo> {
    return mapNotNull {
      val coInsuredHasMissingInfo = it.supportsCoInsured && it.coInsured?.any {
        it.hasMissingInfo && it.terminatesOn == null
      } == true
      val coOwnerHasMissingInfo = it.supportsCoOwners && it.coOwners?.any {
        it.hasMissingInfo && it.terminatesOn == null
      } == true
      when {
        coInsuredHasMissingInfo -> {
          MemberReminder.CoInsuredInfo(it.id, CoInsuredFlowType.CoInsured)
        }

        coOwnerHasMissingInfo -> {
          MemberReminder.CoInsuredInfo(it.id, CoInsuredFlowType.CoOwners)
        }

        else -> {
          null
        }
      }
    }
  }
}

sealed interface CoInsuredInfoReminderError {
  data object NoCoInsuredReminders : CoInsuredInfoReminderError

  data object CoInsuredReminderNotEnabled : CoInsuredInfoReminderError

  data class NetworkError(val errorMessage: ErrorMessage) : CoInsuredInfoReminderError, ErrorMessage by errorMessage
}
