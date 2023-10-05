package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.apollo.OperationResult
import org.javamoney.moneta.Money
import java.math.BigDecimal

internal class ProfileRepositoryDemo : ProfileRepository {
  private var email = "google@gmail.com"
  private var phoneNumber = "072102103"

  private val demoMember: Member
    get() = Member(
      id = "test",
      firstName = "Google",
      lastName = "Tester",
      phoneNumber = phoneNumber,
      email = email,
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
    email = input
    demoMember
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, Member> = either {
    phoneNumber = input
    demoMember
  }
}
