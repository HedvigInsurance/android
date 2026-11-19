package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.validation.PhoneNumberRules
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.core.common.android.validation.isValidEmail

internal interface ContactInfoRepository {
  suspend fun contactInfo(): Either<ErrorMessage, ContactInformation>

  suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email): Either<ErrorMessage, ContactInformation>
}

data class ContactInformation(
  /**
   * The stored number exactly as the backend holds it, which is not necessarily a valid [PhoneNumber]. It is shown to
   * the member so that they can correct it, and validated again on submission.
   */
  val phoneNumber: String?,
  val email: Email?,
) {
  @JvmInline
  value class Email(val value: String) {
    init {
      require(isValidEmail(value)) {
        "Email $value is invalid"
      }
    }

    companion object {
      private val invalidInputErrorMessage = { email: String? -> "Email [$email] must be a valid email address" }

      /**
       * returns [Either.Left] with an [ErrorMessage] if the input is an invalid email
       * returns [Either.Right] with an [Email] if the input is a valid email
       * returns [Either.Right] with a [null] [Email] if the input is null or empty
       */
      fun fromString(input: String?): Either<ErrorMessage, Email?> {
        return when {
          input.isNullOrBlank() -> null.right()
          isValidEmail(input) -> Email(input).right()
          else -> ErrorMessage(invalidInputErrorMessage(input)).left()
        }
      }

      /**
       * returns [Either.Left] with an [ErrorMessage] if the input is an invalid email or empty string
       * returns [Either.Right] with an [Email] if the input is a valid email
       */
      fun fromStringNotNull(input: String): Either<ErrorMessage, Email> {
        return when {
          !input.isBlank() && isValidEmail(input) -> Email(input).right()
          else -> ErrorMessage(invalidInputErrorMessage(input)).left()
        }
      }
    }
  }

  @JvmInline
  value class PhoneNumber(val value: String) {
    init {
      require(value.any { it.isWhitespace() } == false) {
        "Phone number cannot contain whitespaces"
      }
      require(value == "" || (rules.isWellFormed(value) && rules.digitsIn(value) > 0)) {
        "Phone number [$value] must contain only numbers with an optional '+' in the beginning"
      }
    }

    companion object {
      /**
       * The one definition of what a member's phone number may look like, shared with the field they
       * type it into so that what the field accepts and what this rejects cannot drift apart.
       */
      private val rules = PhoneNumberRules.MemberPhoneNumber

      private val invalidInputErrorMessage = { phoneNumber: String ->
        "Phone number [$phoneNumber] must contain only numbers with an optional '+' in the beginning"
      }

      private val tooShortInputErrorMessage = { phoneNumber: String ->
        "Phone number [$phoneNumber] must contain at least ${rules.minDigits} digits"
      }

      /**
       * Separators in [input] are formatting and are dropped, so what comes back is what should be
       * sent rather than what was typed.
       *
       * returns [Either.Left] with an [ErrorMessage] if the input cannot be read as a number, is a
       * blank string, or holds fewer digits than [PhoneNumberRules.minDigits]
       * returns [Either.Right] with a [PhoneNumber] holding the cleaned number
       */
      fun notNullFromString(input: String): Either<ErrorMessage, PhoneNumber> {
        val cleaned = rules.cleanedForSubmission(input)?.toString()
        return when {
          cleaned.isNullOrBlank() -> ErrorMessage(invalidInputErrorMessage(input)).left()
          !rules.hasEnoughDigits(cleaned) -> ErrorMessage(tooShortInputErrorMessage(input)).left()
          else -> PhoneNumber(cleaned).right()
        }
      }
    }
  }
}

/**
 * Defaults to an empty [String] since a textField needs to have at least an empty input if the [Email] is null
 */
internal val Email?.valueForTextField: String
  get() = this?.value ?: ""
