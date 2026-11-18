package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal interface GetGreenCardsUseCase {
  suspend fun invoke(): Either<ErrorMessage, List<GreenCard>>
}

// TODO: Replace with GetGreenCardsUseCaseImpl once the green card schema is published on Octopus.
@ContributesBinding(AppScope::class)
@Inject
internal class FakeGetGreenCardsUseCase(
  private val fakeGreenCardBackend: FakeGreenCardBackend,
  private val clock: Clock,
) : GetGreenCardsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<GreenCard>> {
    val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return fakeGreenCardBackend.greenCards().map {
      GreenCard(
        id = it.id,
        signedUrl = it.signedUrl,
        registrationNumber = it.registrationNumber,
        validFrom = it.validFrom,
        validTo = it.validTo,
        isExpiredNow = it.validTo < today,
      )
    }.right()
  }
}

// @ContributesBinding(AppScope::class)
// @SingleIn(AppScope::class)
// @Inject
// internal class GetGreenCardsUseCaseImpl(
//   private val apolloClient: ApolloClient,
//   private val clock: Clock,
// ) : GetGreenCardsUseCase {
//   override suspend fun invoke(): Either<ErrorMessage, List<GreenCard>> {
//     return apolloClient.query(GreenCardsQuery())
//       .fetchPolicy(FetchPolicy.NetworkOnly)
//       .safeExecute(::ErrorMessage)
//       .map { data ->
//         val today = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
//         data.currentMember.greenCards.map {
//           GreenCard(
//             id = it.id,
//             signedUrl = it.signedUrl,
//             registrationNumber = it.registrationNumber,
//             validFrom = it.validFrom,
//             validTo = it.validTo,
//             isExpiredNow = it.validTo < today,
//           )
//         }
//       }
//   }
// }
