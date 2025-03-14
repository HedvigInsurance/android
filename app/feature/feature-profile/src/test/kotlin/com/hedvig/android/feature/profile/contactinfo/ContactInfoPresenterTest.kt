package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.feature.NoopNetworkCacheManager
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.feature.profile.data.ContactInformation.PhoneNumber
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.ContactInformationQuery
import octopus.MemberUpdateEmailMutation
import octopus.MemberUpdatePhoneNumberMutation
import octopus.type.buildMember
import octopus.type.buildMemberMutationOutput
import org.junit.Rule
import org.junit.Test

@OptIn(ApolloExperimental::class)
class ContactInfoPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @get:Rule
  val testApolloClientRule = TestApolloClientRule(TestNetworkTransportType.TURBINE_MAP)
  val apolloClient: ApolloClient
    get() = testApolloClientRule.apolloClient

  @Test
  fun `Changing the info to a new valid input should be reflected in the state after it`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(Provider { repository })
    val originalEmail = "test@hedvig.com"
    val originalPhoneNumber = "+123"
    val alteredEmail = "test@hedvig.co"
    val alteredPhoneNumber = "+123456"
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data {
        this.currentMember = this.buildMember {
          this.phoneNumber = originalPhoneNumber
          this.email = originalEmail
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem()) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo(originalPhoneNumber)
        assertThat(this.content!!.emailState.text).isEqualTo(originalEmail)
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber(originalPhoneNumber))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(originalEmail))
        assertThat(this.content!!.submittingUpdatedInfo).isEqualTo(false)
        assertThat(this.content!!.canSubmit).isEqualTo(false)
        content!!.phoneNumberState.edit {
          delete(0, length)
          append(alteredPhoneNumber)
        }
        content!!.emailState.edit {
          delete(0, length)
          append(alteredEmail)
        }
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber(originalPhoneNumber))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(originalEmail))
        assertThat(this.content!!.canSubmit).isEqualTo(true)
      }
      sendEvent(ContactInfoEvent.SubmitData)
      with(awaitItem().content!!) {
        assertThat(emailHasError).isFalse()
        assertThat(phoneNumberHasError).isFalse()
        assertThat(submittingUpdatedInfo).isTrue()
        assertThat(canSubmit).isFalse()
      }
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateEmailMutation(alteredEmail),
        MemberUpdateEmailMutation.Data {
          this.memberUpdateEmail = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = originalPhoneNumber
              this.email = alteredEmail
            }
          }
        },
      )
      apolloClient.registerSuspendingTestResponse(
        MemberUpdatePhoneNumberMutation(alteredPhoneNumber),
        MemberUpdatePhoneNumberMutation.Data {
          this.memberUpdatePhoneNumber = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = alteredPhoneNumber
              this.email = originalEmail
            }
          }
        },
      )
      with(awaitItem().content!!) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo(alteredPhoneNumber)
        assertThat(this.content!!.emailState.text).isEqualTo(alteredEmail)
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber(alteredPhoneNumber))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(alteredEmail))
        assertThat(this.content!!.submittingUpdatedInfo).isFalse()
        assertThat(this.content!!.canSubmit).isFalse()
      }
    }
  }

  @Test
  fun `Allowing sumbission should depend on if the input is valid and is different from the original input`() =
    runTest {
      val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
      val presenter = ContactInfoPresenter(Provider { repository })
      val originalEmail = "test@hedvig.com"
      val originalPhoneNumber = "+123"
      val validEmails = listOf(
        "test@hedvig.co",
        "a@hedvig.c",
        "a@h.c",
      )
      val validPhoneNumbers = listOf(
        "+1234",
        "+1",
        "1",
        "+1234567890123",
        "1234567890123",
        "+123 456 789 0123",
        "123 456 789 0123",
      )
      val invalidEmails = listOf(
        "test@hedvig .com",
        "a@a",
        "a@a@a.com",
        " ",
        "",
      )
      val invalidPhoneNumbers = listOf(
        "++1234",
        "+1234a",
        "+",
        " ",
        "",
      )
      presenter.test(
        ContactInfoUiState.Content(
          phoneNumberState = TextFieldState(originalPhoneNumber),
          emailState = TextFieldState(originalEmail),
          uploadedPhoneNumber = PhoneNumber(originalPhoneNumber),
          uploadedEmail = Email(originalEmail),
          submittingUpdatedInfo = false,
        ),
      ) {
        with(awaitItem().content!!) {
          assertThat(canSubmit).isFalse()
          phoneNumberState.replaceTextWith(validPhoneNumbers.first())
          assertThat(canSubmit).isTrue()
          for (invalidEmail in invalidEmails) {
            emailState.replaceTextWith(invalidEmail)
            assertThat(canSubmit).isFalse()
          }
          emailState.replaceTextWith(validEmails.first())
          assertThat(canSubmit).isTrue()
          for (invalidPhoneNumber in invalidPhoneNumbers) {
            phoneNumberState.replaceTextWith(invalidPhoneNumber)
            println("Stelios invalidPhoneNumber:$invalidPhoneNumber")
            assertThat(canSubmit).isFalse()
          }
          phoneNumberState.replaceTextWith(validPhoneNumbers.first())
          assertThat(canSubmit).isTrue()
          emailState.replaceTextWith(originalEmail)
          assertThat(canSubmit).isTrue()
          phoneNumberState.replaceTextWith(originalPhoneNumber)
          assertThat(canSubmit).isFalse()
        }
      }
    }
}

private fun TextFieldState.replaceTextWith(text: String) = this.edit {
  delete(0, length)
  append(text)
}
