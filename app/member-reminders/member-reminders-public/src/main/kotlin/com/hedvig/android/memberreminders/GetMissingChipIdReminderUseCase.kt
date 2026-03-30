package com.hedvig.android.memberreminders

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.cache.normalized.FetchPolicy
import com.apollographql.apollo.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.data.contract.ContractGroup
import com.hedvig.android.data.contract.toContractGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import octopus.MissingChipIdReminderQuery

internal interface GetMissingChipIdReminderUseCase {
  fun invoke(): Flow<MemberReminder.MissingChipId?>
}

internal class GetMissingChipIdReminderUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMissingChipIdReminderUseCase {
  override fun invoke(): Flow<MemberReminder.MissingChipId?> {
    return apolloClient.query(MissingChipIdReminderQuery())
      .fetchPolicy(FetchPolicy.CacheAndNetwork)
      .safeFlow()
      .mapLatest { result ->
        result.fold(
          ifRight = { data ->
            data.currentMember.activeContracts
              .firstOrNull { it.missingPetId }
              ?.let { MemberReminder.MissingChipId() }
          },
          ifLeft = { null },
        )
      }
  }
}
