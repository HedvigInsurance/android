package com.hedvig.android.data.paying.member

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.validation.PhoneNumberRules
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import octopus.PayingMemberPhoneNumberQuery

/** The number the backend already holds for the member, null when it has never been given. */
interface GetMemberPhoneNumberUseCase {
  suspend fun invoke(): Either<ErrorMessage, String?>
}

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class GetMemberPhoneNumberUseCaseImpl(
  private val apolloClient: ApolloClient,
) : GetMemberPhoneNumberUseCase {
  override suspend fun invoke(): Either<ErrorMessage, String?> = either {
    apolloClient
      .query(PayingMemberPhoneNumberQuery())
      .safeExecute(::ErrorMessage)
      .bind()
      .currentMember
      .phoneNumber
  }
}

/**
 * [storedNumber] ready for a Swish field, or null when it cannot be read as a number the backend
 * takes, in which case there is nothing safe to prefill and the member types it.
 *
 * The backend accepts `07…`, `467…` and `+467…` and normalises them itself, so the stored form is
 * passed through rather than rewritten — `+46` is what Swish ends up wanting anyway. Only
 * separators are dropped, since they are formatting the field applies for itself.
 */
fun swishPhoneNumberOrNull(storedNumber: String): String? {
  // A letter is a mistake rather than formatting, so it makes the whole value unreadable instead of
  // being quietly dropped along with the separators.
  if (storedNumber.any { it.isLetter() }) return null
  val hasCountryCodePlus = storedNumber.trimStart().startsWith("+")
  val digits = storedNumber.filter { it.isDigit() }
  val lengthAccepted = digits.length >= PhoneNumberRules.SwishPhoneNumber.minDigits &&
    digits.length <= PhoneNumberRules.E164_MAX_DIGITS
  val prefixAccepted = when {
    digits.startsWith("467") -> true

    // A plus belongs to a country code, so it cannot lead a domestic number.
    digits.startsWith("07") -> !hasCountryCodePlus

    else -> false
  }
  if (!lengthAccepted || !prefixAccepted) return null
  return if (hasCountryCodePlus) "+$digits" else digits
}
