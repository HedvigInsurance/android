package com.hedvig.android.feature.profile.data

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.core.common.android.validation.isValidEmail

internal interface ContactInfoRepository {
  suspend fun contactInfo(): Either<ErrorMessage, ContactInformation>

  suspend fun updateInfo(phoneNumber: PhoneNumber, email: Email): Either<ErrorMessage, ContactInformation>
}

internal data class ContactInformation(
  val phoneNumber: PhoneNumber?,
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
      require(value == "" || phoneNumberRegex.matches(value)) {
        "Phone number [$value] must contain only numbers with an optional '+' in the beginning"
      }
    }

    companion object {
      private val phoneNumberRegex = Regex("""([+]?\d+)""")
      private val invalidInputErrorMessage = { phoneNumber: String ->
        "Phone number [$phoneNumber] must contain only numbers with an optional '+' in the beginning"
      }

      private val whitespacesInInputErrorMessage = { phoneNumber: String ->
        "Phone number [$phoneNumber] cannot contain whitespaces"
      }

      /**
       * for the phone number we get from the backend, can be null
       */
      fun fromStringAfterTrimmingWhitespaces(input: String?): Either<ErrorMessage, PhoneNumber?> {
        val inputWithoutWhitespaces = input?.filterNot { it.isWhitespace() }
        return fromString(inputWithoutWhitespaces)
      }

      /**
       * for the phone number we're trying to send to backend, cannot be null
       */
      fun notNullFromStringAfterTrimmingWhitespaces(input: String): Either<ErrorMessage, PhoneNumber> {
        val inputWithoutWhitespaces = input.filterNot { it.isWhitespace() }
        return notNullFromString(inputWithoutWhitespaces)
      }

      /**
       * returns [Either.Left] with an [ErrorMessage] if the input is an invalid phone number
       * returns [Either.Right] with a [PhoneNumber] if the input is a valid phone number
       * returns [Either.Right] with a [null] [PhoneNumber] if the input is null or empty
       */
      fun fromString(input: String?): Either<ErrorMessage, PhoneNumber?> {
        return when {
          input.isNullOrBlank() -> null.right()
          input.any { it.isWhitespace() } -> ErrorMessage(whitespacesInInputErrorMessage(input)).left()
          input.matches(phoneNumberRegex) -> PhoneNumber(input).right()
          else -> ErrorMessage(invalidInputErrorMessage(input)).left()
        }
      }

      /**
       * returns [Either.Left] with an [ErrorMessage] if the input is an invalid phone number or blank string
       * returns [Either.Right] with a [PhoneNumber] if the input is a valid phone number
       */
      fun notNullFromString(input: String): Either<ErrorMessage, PhoneNumber> {
        return when {
          input.any { it.isWhitespace() } -> ErrorMessage(whitespacesInInputErrorMessage(input)).left()
          !input.isBlank() && input.matches(phoneNumberRegex) -> PhoneNumber(input).right()
          else -> ErrorMessage(invalidInputErrorMessage(input)).left()
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

/**
 * Defaults to an empty [String] since a textField needs to have at least an empty input if the [PhoneNumber] is null
 */
internal val PhoneNumber?.valueForTextField: String
  get() = this?.value ?: ""
