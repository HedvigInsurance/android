package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.datetime.LocalDate

internal interface CreateGreenCardUseCase {
  suspend fun invoke(
    contractId: String,
    travelStartDate: LocalDate,
    travelEndDate: LocalDate,
    email: String?,
  ): Either<ErrorMessage, GreenCardUrl>
}

// TODO: Replace with CreateGreenCardUseCaseImpl once the green card schema is published on Octopus.
@ContributesBinding(AppScope::class)
@Inject
internal class FakeCreateGreenCardUseCase(
  private val fakeGreenCardBackend: FakeGreenCardBackend,
) : CreateGreenCardUseCase {
  override suspend fun invoke(
    contractId: String,
    travelStartDate: LocalDate,
    travelEndDate: LocalDate,
    email: String?,
  ): Either<ErrorMessage, GreenCardUrl> {
    return GreenCardUrl(fakeGreenCardBackend.create(contractId, travelStartDate, travelEndDate).signedUrl).right()
  }
}

// @ContributesBinding(AppScope::class)
// @SingleIn(AppScope::class)
// @Inject
// internal class CreateGreenCardUseCaseImpl(
//   private val apolloClient: ApolloClient,
// ) : CreateGreenCardUseCase {
//   override suspend fun invoke(
//     contractId: String,
//     travelStartDate: LocalDate,
//     travelEndDate: LocalDate,
//     email: String?,
//   ): Either<ErrorMessage, GreenCardUrl> {
//     val input = GreenCardCreateInput(
//       contractId = contractId,
//       travelStartDate = travelStartDate,
//       travelEndDate = travelEndDate,
//       email = Optional.presentIfNotNull(email),
//     )
//     return apolloClient.mutation(GreenCardCreateMutation(input))
//       .safeExecute()
//       .onLeft { logcat(LogPriority.ERROR, it) { "CreateGreenCardUseCase: $it" } }
//       .mapLeft(::ErrorMessage)
//       .map { GreenCardUrl(it.greenCardCreate.signedUrl) }
//   }
// }
