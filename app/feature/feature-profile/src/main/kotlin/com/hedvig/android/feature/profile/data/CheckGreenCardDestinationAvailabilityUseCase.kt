package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

internal interface CheckGreenCardDestinationAvailabilityUseCase {
  suspend fun invoke(): Either<GreenCardAvailabilityError, Unit>
}

// TODO: Replace with CheckGreenCardDestinationAvailabilityUseCaseImpl once the green card schema is published on
//  Octopus. Until then the green card row is always shown.
@ContributesBinding(AppScope::class)
@Inject
internal class FakeCheckGreenCardDestinationAvailabilityUseCase : CheckGreenCardDestinationAvailabilityUseCase {
  override suspend fun invoke(): Either<GreenCardAvailabilityError, Unit> = Unit.right()
}

// @ContributesBinding(AppScope::class)
// @SingleIn(AppScope::class)
// @Inject
// internal class CheckGreenCardDestinationAvailabilityUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : CheckGreenCardDestinationAvailabilityUseCase {
//   override suspend fun invoke(): Either<GreenCardAvailabilityError, Unit> = either {
//     val isGreenCardEnabled = apolloClient.query(GreenCardAvailabilityQuery())
//       .safeExecute()
//       .mapLeft {
//         logcat(LogPriority.ERROR, it) { "CheckGreenCardAvailability error. Message: $it" }
//         GreenCardAvailabilityError.Error(ErrorMessage("Could not check isGreenCardEnabled: $it"))
//       }
//       .bind()
//       .currentMember.memberActions?.isGreenCardEnabled
//     ensure(isGreenCardEnabled == true) { GreenCardAvailabilityError.GreenCardNotAvailable }
//   }
// }

internal sealed interface GreenCardAvailabilityError {
  data object GreenCardNotAvailable : GreenCardAvailabilityError

  data class Error(
    val errorMessage: ErrorMessage,
  ) : GreenCardAvailabilityError, ErrorMessage by errorMessage
}
