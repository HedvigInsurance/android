package com.hedvig.android.feature.profile.contactinfo

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.annotations.ApolloExperimental
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.octopus.test.OctopusFakeResolver
import com.hedvig.android.apollo.test.TestApolloClientRule
import com.hedvig.android.apollo.test.TestNetworkTransportType
import com.hedvig.android.apollo.test.registerSuspendingTestNetworkError
import com.hedvig.android.apollo.test.registerSuspendingTestResponse
import com.hedvig.android.feature.NoopNetworkCacheManager
import com.hedvig.android.feature.profile.data.ContactInfoRepositoryImpl
import com.hedvig.android.feature.profile.data.ContactInformation.Email
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import octopus.ContactInformationQuery
import octopus.MemberUpdateContactInfoMutation
import octopus.builder.Data
import octopus.builder.buildMember
import octopus.builder.buildMemberMutationOutput
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ApolloExperimental::class)
@RunWith(TestParameterInjector::class)
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
    val presenter = ContactInfoPresenter(repository)
    val originalEmail = "test@hedvig.com"
    val originalPhoneNumber = "+1234567"
    val alteredEmail = "test2@hedvig.com"
    val alteredPhoneNumber = "+123456"
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data(OctopusFakeResolver) {
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
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(originalPhoneNumber)
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(originalEmail))
        assertThat(this.content!!.submittingUpdatedInfo).isEqualTo(false)
        assertThat(this.content!!.phoneNumberHasError).isFalse()
        assertThat(this.content!!.emailHasError).isFalse()
        content!!.phoneNumberState.setTextAndPlaceCursorAtEnd(alteredPhoneNumber)
        content!!.emailState.setTextAndPlaceCursorAtEnd(alteredEmail)
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(originalPhoneNumber)
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(originalEmail))
      }
      sendEvent(ContactInfoEvent.SubmitData)
      with(awaitItem().content!!) {
        assertThat(emailHasError).isFalse()
        assertThat(phoneNumberHasError).isFalse()
        assertThat(submittingUpdatedInfo).isTrue()
      }
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateContactInfoMutation(alteredEmail, originalPhoneNumber),
        MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
          this.memberUpdateContactInfo = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = originalPhoneNumber
              this.email = alteredEmail
            }
          }
        },
      )
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateContactInfoMutation(alteredEmail, alteredPhoneNumber),
        MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
          this.memberUpdateContactInfo = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = alteredPhoneNumber
              this.email = alteredEmail
            }
          }
        },
      )
      with(awaitItem().content!!) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo(alteredPhoneNumber)
        assertThat(this.content!!.emailState.text).isEqualTo(alteredEmail)
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(alteredPhoneNumber)
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(alteredEmail))
        assertThat(this.content!!.submittingUpdatedInfo).isFalse()
        assertThat(this.content!!.phoneNumberHasError).isFalse()
        assertThat(this.content!!.emailHasError).isFalse()
      }
    }
  }

  @Test
  fun `Each field reports its own validity once submission has been attempted`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    val originalEmail = "test@hedvig.com"
    val originalPhoneNumber = "+123456"
    val validEmails = listOf(
      "test@hedvig.co",
      "a@hedvig.c",
      "a@h.c",
    )
    val validPhoneNumbers = listOf(
      "+1234567890123",
      "1234567890123",
      "+123 456 789 0123",
      "123 456 789 0123",
    )
    val invalidEmails = listOf(
      "test@hedvig .com",
      "a@a",
      "a@a@a.com",
      "",
      " ",
    )
    val invalidPhoneNumbers = listOf(
      "++1234",
      "+1234a",
      "+",
      "",
      " ",
      "+1",
      "1",
      "+12345",
      "12345",
    )
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumberState = TextFieldState(originalPhoneNumber),
        emailState = TextFieldState(originalEmail),
        uploadedPhoneNumber = originalPhoneNumber,
        uploadedEmail = Email(originalEmail),
        submittingUpdatedInfo = false,
        hasAttemptedSubmission = true,
      ),
    ) {
      with(expectMostRecentItem().content!!) {
        assertThat(phoneNumberHasError).isFalse()
        assertThat(emailHasError).isFalse()
        for (validPhoneNumber in validPhoneNumbers) {
          phoneNumberState.setTextAndPlaceCursorAtEnd(validPhoneNumber)
          assertThat(phoneNumberHasError).isFalse()
        }
        for (invalidPhoneNumber in invalidPhoneNumbers) {
          phoneNumberState.setTextAndPlaceCursorAtEnd(invalidPhoneNumber)
          assertThat(phoneNumberHasError).isTrue()
          assertThat(emailHasError).isFalse()
        }
        for (validEmail in validEmails) {
          emailState.setTextAndPlaceCursorAtEnd(validEmail)
          assertThat(emailHasError).isFalse()
        }
        for (invalidEmail in invalidEmails) {
          emailState.setTextAndPlaceCursorAtEnd(invalidEmail)
          assertThat(emailHasError).isTrue()
        }
      }
      expectNoEvents()
    }
  }

  @Test
  fun `An invalid phone number from the backend is shown as an editable error instead of failing the screen`(
    @TestParameter("+12345", "12", "+1234a") backendPhoneNumber: String,
  ) = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    val backendEmail = "test@hedvig.com"
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data(OctopusFakeResolver) {
        this.currentMember = this.buildMember {
          this.phoneNumber = backendPhoneNumber
          this.email = backendEmail
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem().content!!) {
        assertThat(phoneNumberState.text).isEqualTo(backendPhoneNumber)
        assertThat(phoneNumberHasError).isTrue()
        phoneNumberState.setTextAndPlaceCursorAtEnd("+123456")
        assertThat(phoneNumberHasError).isFalse()
      }
    }
  }

  @Test
  fun `Submitting invalid input shows the field error instead of uploading it`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    val email = "test@hedvig.com"
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumberState = TextFieldState(""),
        emailState = TextFieldState(email),
        uploadedPhoneNumber = null,
        uploadedEmail = Email(email),
        submittingUpdatedInfo = false,
      ),
    ) {
      // An empty field is not an error until the member has tried to submit it
      assertThat(expectMostRecentItem().content!!.phoneNumberHasError).isFalse()
      sendEvent(ContactInfoEvent.SubmitData)
      val content = awaitItem().content!!
      assertThat(content.phoneNumberHasError).isTrue()
      assertThat(content.submittingUpdatedInfo).isFalse()
      // A number with too few digits is not uploaded either
      content.phoneNumberState.setTextAndPlaceCursorAtEnd("+12345")
      assertThat(content.phoneNumberHasError).isTrue()
      sendEvent(ContactInfoEvent.SubmitData)
      expectNoEvents()
      content.phoneNumberState.setTextAndPlaceCursorAtEnd("+123456")
      assertThat(content.phoneNumberHasError).isFalse()
      sendEvent(ContactInfoEvent.SubmitData)
      assertThat(awaitItem().content!!.submittingUpdatedInfo).isTrue()
    }
  }

  @Test
  fun `Retrying does fetch the new state after an initial failure`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    presenter.test(ContactInfoUiState.Error) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Error)
      // By default, coming back to a failed screen should automatically trigger a refresh and go into a loading state
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      apolloClient.registerSuspendingTestNetworkError(ContactInformationQuery())
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Error)
      sendEvent(ContactInfoEvent.RetryLoadData)
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      apolloClient.registerSuspendingTestResponse(
        ContactInformationQuery(),
        ContactInformationQuery.Data(OctopusFakeResolver) {
          this.currentMember = this.buildMember {
            this.phoneNumber = "+123"
            this.email = "test@hedvig.com"
          }
        },
      )
      assertThat(awaitItem()).isInstanceOf<ContactInfoUiState.Content>()
    }
  }

  @Test
  fun `Backend returning empty or null info should result in an empty but valid UI state`(
    @TestParameter testingNullPhoneNumber: Boolean,
  ) = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    val backendEmail = ""
    val backendPhoneNumber = "".takeIf { !testingNullPhoneNumber }
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data(OctopusFakeResolver) {
        this.currentMember = this.buildMember {
          this.phoneNumber = backendPhoneNumber
          this.email = backendEmail
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem()) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo("")
        assertThat(this.content!!.emailState.text).isEqualTo("")
        assertThat(this.content!!.uploadedPhoneNumber).isNull()
        assertThat(this.content!!.uploadedEmail).isNull()
        assertThat(this.content!!.submittingUpdatedInfo).isEqualTo(false)
        assertThat(this.content!!.phoneNumberHasError).isFalse()
        assertThat(this.content!!.emailHasError).isFalse()
      }
    }
  }

  @Test
  fun `Can not submit new contact info if that would mean deleting some previously present info`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository)
    val backendEmail = "test@hedvig.com"
    val backendPhoneNumber = "+123"
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data(OctopusFakeResolver) {
        this.currentMember = this.buildMember {
          this.phoneNumber = backendPhoneNumber
          this.email = backendEmail
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem()) {
        this.content!!.phoneNumberState.setTextAndPlaceCursorAtEnd("")
        content!!.emailState.setTextAndPlaceCursorAtEnd("")
        assertThat(this.content!!.uploadedPhoneNumber).isNotNull()
        assertThat(this.content!!.uploadedEmail).isNotNull()
      }
      sendEvent(ContactInfoEvent.SubmitData)
      with(awaitItem().content!!) {
        assertThat(phoneNumberHasError).isTrue()
        assertThat(emailHasError).isTrue()
        assertThat(submittingUpdatedInfo).isFalse()
      }
    }
  }
}
