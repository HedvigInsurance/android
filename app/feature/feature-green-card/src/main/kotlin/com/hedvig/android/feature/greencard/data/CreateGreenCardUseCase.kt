package com.hedvig.android.feature.greencard.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.datetime.LocalDate
import octopus.GreenCardCreateMutation
import octopus.type.GreenCardAddressInput
import octopus.type.GreenCardCoDriverInput
import octopus.type.GreenCardCountry
import octopus.type.GreenCardCreateInput

internal interface CreateGreenCardUseCase {
  suspend fun invoke(
    contractId: String,
    travelStartDate: LocalDate,
    travelEndDate: LocalDate,
    mailingAddress: GreenCardAddress,
    coDrivers: List<GreenCardCoDriver>,
  ): Either<CreateGreenCardError, GreenCardUrl>
}

internal sealed interface CreateGreenCardError {
  /**
   * The backend rejected the request with a [message] meant for the member.
   */
  data class UserError(val message: String) : CreateGreenCardError

  data object Unknown : CreateGreenCardError
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class CreateGreenCardUseCaseImpl(
  private val apolloClient: ApolloClient,
) : CreateGreenCardUseCase {
  override suspend fun invoke(
    contractId: String,
    travelStartDate: LocalDate,
    travelEndDate: LocalDate,
    mailingAddress: GreenCardAddress,
    coDrivers: List<GreenCardCoDriver>,
  ): Either<CreateGreenCardError, GreenCardUrl> = either {
    val input = GreenCardCreateInput(
      contractId = contractId,
      travelStartDate = travelStartDate,
      travelEndDate = travelEndDate,
      mailingAddress = mailingAddress.toInput(),
      coDrivers = coDrivers.map { GreenCardCoDriverInput(name = it.name, address = it.address.toInput()) },
    )
    val output = apolloClient.mutation(GreenCardCreateMutation(input))
      .safeExecute()
      .onLeft { logcat(LogPriority.ERROR, it) { "CreateGreenCardUseCase: $it" } }
      .mapLeft { CreateGreenCardError.Unknown }
      .bind()
      .greenCardCreate
    output.userError?.let { userError ->
      logcat(LogPriority.WARN) { "CreateGreenCardUseCase: user error ${userError.message}" }
      raise(CreateGreenCardError.UserError(userError.message))
    }
    val greenCard = ensureNotNull(output.greenCard) {
      logcat(LogPriority.ERROR) { "CreateGreenCardUseCase: neither a green card nor a user error was returned" }
      CreateGreenCardError.Unknown
    }
    GreenCardUrl(greenCard.signedUrl)
  }
}

private fun GreenCardAddress.toInput() = GreenCardAddressInput(
  street = street,
  postalCode = postalCode,
  city = city,
  country = GreenCardCountry.safeValueOf(country.name),
)
