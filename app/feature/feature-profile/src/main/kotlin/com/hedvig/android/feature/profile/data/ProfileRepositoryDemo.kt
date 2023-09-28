package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.apollo.OperationResult
import java.math.BigDecimal
import org.javamoney.moneta.Money

internal class ProfileRepositoryDemo : ProfileRepository {
  private val demoMember = Member(
    id = "test",
    firstName = "Google",
    lastName = "Tester",
    phoneNumber = null,
    email = "google@gmail.com",
  )

  override suspend fun profile(): Either<OperationResult.Error, ProfileData> = either {
    ProfileData(
      member = demoMember,
      chargeEstimation = ChargeEstimation(
        subscription = Money.of(BigDecimal("120"), "SEK"),
        charge = Money.of(BigDecimal("120"), "SEK"),
        discount = Money.of(BigDecimal("120"), "SEK"),
      ),
      directDebitStatus = null,
      activePaymentMethods = null,
    )
  }

  override suspend fun updateEmail(input: String): Either<OperationResult.Error, Member> = either {
    demoMember
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, Member> = either {
    demoMember
  }
}
