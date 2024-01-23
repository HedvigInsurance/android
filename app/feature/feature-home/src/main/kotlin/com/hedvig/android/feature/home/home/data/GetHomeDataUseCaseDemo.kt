package com.hedvig.android.feature.home.home.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.memberreminders.MemberReminders
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class GetHomeDataUseCaseDemo : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>> = flowOf(
    HomeData(
      contractStatus = HomeData.ContractStatus.Active,
      claimStatusCardsData = null,
      veryImportantMessages = persistentListOf(),
      memberReminders = MemberReminders(
        connectPayment = null,
        upcomingRenewals = null,
        enableNotifications = null,
      ),
    ).right(),
  )
}
