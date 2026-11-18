package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.GreenCardAvailabilityQuery

internal interface CheckGreenCardDestinationAvailabilityUseCase {
  suspend fun invoke(): Either<GreenCardAvailabilityError, Unit>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class CheckGreenCardDestinationAvailabilityUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CheckGreenCardDestinationAvailabilityUseCase {
  override suspend fun invoke(): Either<GreenCardAvailabilityError, Unit> = either {
    val isGreenCardEnabled = apolloClient.query(GreenCardAvailabilityQuery())
      .safeExecute()
      .mapLeft {
        logcat(LogPriority.ERROR, it) { "CheckGreenCardAvailability error. Message: $it" }
        GreenCardAvailabilityError.Error(ErrorMessage("Could not check isCreatingOfGreenCardEnabled: $it"))
      }
      .bind()
      .currentMember.memberActions?.isCreatingOfGreenCardEnabled
    ensure(isGreenCardEnabled == true) { GreenCardAvailabilityError.GreenCardNotAvailable }
  }
}

internal sealed interface GreenCardAvailabilityError {
  data object GreenCardNotAvailable : GreenCardAvailabilityError

  data class Error(
    val errorMessage: ErrorMessage,
  ) : GreenCardAvailabilityError, ErrorMessage by errorMessage
}
