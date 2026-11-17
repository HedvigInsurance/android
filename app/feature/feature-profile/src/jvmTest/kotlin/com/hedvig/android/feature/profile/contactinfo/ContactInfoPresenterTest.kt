package com.hedvig.android.feature.profile.contactinfo

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
import com.hedvig.android.test.clock.TestClock
import kotlinx.coroutines.test.runTest
import octopus.ContactInformationQuery
import octopus.MemberUpdateContactInfoMutation
import octopus.builder.Data
import octopus.builder.buildMember
import octopus.builder.buildMemberMutationOutput
import octopus.builder.buildUserError
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
    val presenter = ContactInfoPresenter(repository, TestClock())
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
        assertThat(this.content!!.phoneNumber).isEqualTo(originalPhoneNumber)
        assertThat(this.content!!.email).isEqualTo(originalEmail)
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(originalPhoneNumber)
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email(originalEmail))
        assertThat(this.content!!.submittingUpdatedInfo).isEqualTo(false)
        assertThat(this.content!!.phoneNumberHasError).isFalse()
        assertThat(this.content!!.emailHasError).isFalse()
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber(alteredPhoneNumber))
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail(alteredEmail))
      with(awaitItem().content!!) {
        assertThat(phoneNumber).isEqualTo(alteredPhoneNumber)
        assertThat(email).isEqualTo(alteredEmail)
        assertThat(uploadedPhoneNumber).isEqualTo(originalPhoneNumber)
        assertThat(uploadedEmail).isEqualTo(Email(originalEmail))
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
        assertThat(this.content!!.phoneNumber).isEqualTo(alteredPhoneNumber)
        assertThat(this.content!!.email).isEqualTo(alteredEmail)
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
    val presenter = ContactInfoPresenter(repository, TestClock())
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
        phoneNumber = originalPhoneNumber,
        email = originalEmail,
        uploadedPhoneNumber = originalPhoneNumber,
        uploadedEmail = Email(originalEmail),
        submittingUpdatedInfo = false,
        hasAttemptedSubmission = true,
      ),
    ) {
      with(awaitItem().content!!) {
        assertThat(phoneNumberHasError).isFalse()
        assertThat(emailHasError).isFalse()
      }
      for (validPhoneNumber in validPhoneNumbers) {
        sendEvent(ContactInfoEvent.ChangePhoneNumber(validPhoneNumber))
        assertThat(awaitItem().content!!.phoneNumberHasError).isFalse()
      }
      for (invalidPhoneNumber in invalidPhoneNumbers) {
        sendEvent(ContactInfoEvent.ChangePhoneNumber(invalidPhoneNumber))
        with(awaitItem().content!!) {
          assertThat(phoneNumberHasError).isTrue()
          assertThat(emailHasError).isFalse()
        }
      }
      for (validEmail in validEmails) {
        sendEvent(ContactInfoEvent.ChangeEmail(validEmail))
        assertThat(awaitItem().content!!.emailHasError).isFalse()
      }
      for (invalidEmail in invalidEmails) {
        sendEvent(ContactInfoEvent.ChangeEmail(invalidEmail))
        assertThat(awaitItem().content!!.emailHasError).isTrue()
      }
      expectNoEvents()
    }
  }

  @Test
  fun `An invalid phone number from the backend is shown as an editable error instead of failing the screen`(
    @TestParameter("+12345", "12", "+1234a") backendPhoneNumber: String,
  ) = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
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
        assertThat(phoneNumber).isEqualTo(backendPhoneNumber)
        assertThat(phoneNumberHasError).isTrue()
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+123456"))
      assertThat(awaitItem().content!!.phoneNumberHasError).isFalse()
    }
  }

  @Test
  fun `Submitting invalid input shows the field error instead of uploading it`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    val email = "test@hedvig.com"
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = "",
        email = email,
        uploadedPhoneNumber = null,
        uploadedEmail = Email(email),
        submittingUpdatedInfo = false,
      ),
    ) {
      // An empty field is not an error until the member has tried to submit it
      assertThat(awaitItem().content!!.phoneNumberHasError).isFalse()
      sendEvent(ContactInfoEvent.SubmitData)
      val content = awaitItem().content!!
      assertThat(content.phoneNumberHasError).isTrue()
      assertThat(content.submittingUpdatedInfo).isFalse()
      // A number with too few digits is not uploaded either
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+12345"))
      assertThat(awaitItem().content!!.phoneNumberHasError).isTrue()
      sendEvent(ContactInfoEvent.SubmitData)
      expectNoEvents()
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+123456"))
      assertThat(awaitItem().content!!.phoneNumberHasError).isFalse()
      sendEvent(ContactInfoEvent.SubmitData)
      assertThat(awaitItem().content!!.submittingUpdatedInfo).isTrue()
    }
  }

  @Test
  fun `Retrying does fetch the new state after an initial failure`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
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
    val presenter = ContactInfoPresenter(repository, TestClock())
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
        assertThat(this.content!!.phoneNumber).isEqualTo("")
        assertThat(this.content!!.email).isEqualTo("")
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
    val presenter = ContactInfoPresenter(repository, TestClock())
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
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangePhoneNumber(""))
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail(""))
      with(awaitItem()) {
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

  @Test
  fun `Before a submission attempt only a non-empty invalid field shows an error`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = "",
        email = "",
        uploadedPhoneNumber = null,
        uploadedEmail = null,
        submittingUpdatedInfo = false,
      ),
    ) {
      with(awaitItem().content!!) {
        assertThat(hasAttemptedSubmission).isFalse()
        assertThat(phoneNumberHasError).isFalse()
        assertThat(emailHasError).isFalse()
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+12"))
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail("a@a"))
      with(awaitItem().content!!) {
        assertThat(phoneNumberHasError).isTrue()
        assertThat(emailHasError).isTrue()
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber(" "))
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail(" "))
      with(awaitItem().content!!) {
        assertThat(phoneNumberHasError).isFalse()
        assertThat(emailHasError).isFalse()
      }
    }
  }

  @Test
  fun `A phone number typed with separators is sent without them`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    val email = "test@hedvig.com"
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = "+46 70-123 45 67",
        email = email,
        uploadedPhoneNumber = null,
        uploadedEmail = Email(email),
        submittingUpdatedInfo = false,
      ),
    ) {
      skipItems(1)
      sendEvent(ContactInfoEvent.SubmitData)
      assertThat(awaitItem().content!!.submittingUpdatedInfo).isTrue()
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateContactInfoMutation(email, "+46701234567"),
        MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
          this.memberUpdateContactInfo = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = "+46701234567"
              this.email = email
            }
          }
        },
      )
      with(awaitItem().content!!) {
        assertThat(phoneNumber).isEqualTo("+46701234567")
        assertThat(uploadedPhoneNumber).isEqualTo("+46701234567")
        assertThat(submittingUpdatedInfo).isFalse()
        assertThat(showSuccessSnackBar).isTrue()
      }
      sendEvent(ContactInfoEvent.ShowedMessage)
      assertThat(awaitItem().content!!.showSuccessSnackBar).isFalse()
    }
  }

  @Test
  fun `A failed submission keeps the input and shows the backend message or a general error`(
    @TestParameter backendGivesMessage: Boolean,
  ) = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    val email = "test@hedvig.com"
    val phoneNumber = "+46701234567"
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = phoneNumber,
        email = email,
        uploadedPhoneNumber = "+46700000000",
        uploadedEmail = Email("old@hedvig.com"),
        submittingUpdatedInfo = false,
      ),
    ) {
      skipItems(1)
      sendEvent(ContactInfoEvent.SubmitData)
      assertThat(awaitItem().content!!.submittingUpdatedInfo).isTrue()
      if (backendGivesMessage) {
        apolloClient.registerSuspendingTestResponse(
          MemberUpdateContactInfoMutation(email, phoneNumber),
          MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
            this.memberUpdateContactInfo = this.buildMemberMutationOutput {
              this.userError = this.buildUserError {
                this.message = "Not allowed"
              }
              this.member = null
            }
          },
        )
      } else {
        apolloClient.registerSuspendingTestNetworkError(MemberUpdateContactInfoMutation(email, phoneNumber))
      }
      with(awaitItem().content!!) {
        assertThat(submittingUpdatedInfo).isFalse()
        assertThat(errorSnackBarText).isEqualTo(
          if (backendGivesMessage) ErrorSnackBarText.WithMessage("Not allowed") else ErrorSnackBarText.General,
        )
        assertThat(this.phoneNumber).isEqualTo(phoneNumber)
        assertThat(this.email).isEqualTo(email)
        assertThat(uploadedPhoneNumber).isEqualTo("+46700000000")
        assertThat(uploadedEmail).isEqualTo(Email("old@hedvig.com"))
        assertThat(showSuccessSnackBar).isFalse()
      }
      sendEvent(ContactInfoEvent.ShowedMessage)
      assertThat(awaitItem().content!!.errorSnackBarText).isNull()
    }
  }

  @Test
  fun `The revision increases only when the presenter writes the text itself`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data(OctopusFakeResolver) {
        this.currentMember = this.buildMember {
          this.phoneNumber = "+46700000000"
          this.email = "old@hedvig.com"
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem().content!!) {
        assertThat(phoneNumberRevision).isEqualTo(1)
        assertThat(emailRevision).isEqualTo(1)
      }
      // Member edits are not presenter writes
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+4670"))
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail("new@hedvig.com"))
      with(awaitItem().content!!) {
        assertThat(phoneNumber).isEqualTo("+4670")
        assertThat(email).isEqualTo("new@hedvig.com")
        assertThat(phoneNumberRevision).isEqualTo(1)
        assertThat(emailRevision).isEqualTo(1)
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+46701234567"))
      skipItems(1)
      // A failed submission leaves the text alone
      sendEvent(ContactInfoEvent.SubmitData)
      skipItems(1)
      apolloClient.registerSuspendingTestNetworkError(
        MemberUpdateContactInfoMutation("new@hedvig.com", "+46701234567"),
      )
      with(awaitItem().content!!) {
        assertThat(errorSnackBarText).isEqualTo(ErrorSnackBarText.General)
        assertThat(phoneNumberRevision).isEqualTo(1)
        assertThat(emailRevision).isEqualTo(1)
      }
      // A successful one writes back what the backend stored
      sendEvent(ContactInfoEvent.SubmitData)
      skipItems(1)
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateContactInfoMutation("new@hedvig.com", "+46701234567"),
        MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
          this.memberUpdateContactInfo = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = "+46701234567"
              this.email = "stored@hedvig.com"
            }
          }
        },
      )
      var afterSuccess = awaitItem().content!!
      while (afterSuccess.submittingUpdatedInfo) {
        afterSuccess = awaitItem().content!!
      }
      with(afterSuccess) {
        assertThat(email).isEqualTo("stored@hedvig.com")
        assertThat(phoneNumber).isEqualTo("+46701234567")
        assertThat(emailRevision).isEqualTo(2)
        assertThat(phoneNumberRevision).isEqualTo(2)
      }
    }
  }

  @Test
  fun `A change event carrying the text the presenter already holds changes nothing`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    val initialState = ContactInfoUiState.Content(
      phoneNumber = "+46701234567",
      email = "test@hedvig.com",
      uploadedPhoneNumber = "+46701234567",
      uploadedEmail = Email("test@hedvig.com"),
      submittingUpdatedInfo = false,
      phoneNumberRevision = 3,
      emailRevision = 5,
    )
    presenter.test(initialState) {
      assertThat(awaitItem()).isEqualTo(initialState)
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+46701234567"))
      sendEvent(ContactInfoEvent.ChangeEmail("test@hedvig.com"))
      // Equal once normalised, so also nothing
      sendEvent(ContactInfoEvent.ChangeEmail(" test@hedvig.com "))
      expectNoEvents()
    }
  }

  @Test
  fun `The presenter trims the email before validating and submitting it`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = "+46701234567",
        email = "",
        uploadedPhoneNumber = null,
        uploadedEmail = null,
        submittingUpdatedInfo = false,
        emailRevision = 1,
      ),
    ) {
      skipItems(1)
      sendEvent(ContactInfoEvent.ChangeEmail("  new@hedvig.com\t"))
      with(awaitItem().content!!) {
        assertThat(email).isEqualTo("new@hedvig.com")
        assertThat(emailHasError).isFalse()
        // Normalising is not a write the UI has to copy back
        assertThat(emailRevision).isEqualTo(1)
      }
      sendEvent(ContactInfoEvent.SubmitData)
      assertThat(awaitItem().content!!.submittingUpdatedInfo).isTrue()
      apolloClient.registerSuspendingTestResponse(
        MemberUpdateContactInfoMutation("new@hedvig.com", "+46701234567"),
        MemberUpdateContactInfoMutation.Data(OctopusFakeResolver) {
          this.memberUpdateContactInfo = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = "+46701234567"
              this.email = "new@hedvig.com"
            }
          }
        },
      )
      with(awaitItem().content!!) {
        assertThat(uploadedEmail).isEqualTo(Email("new@hedvig.com"))
        assertThat(showSuccessSnackBar).isTrue()
      }
    }
  }

  @Test
  fun `After a submission attempt each change is validated as it arrives`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    presenter.test(
      ContactInfoUiState.Content(
        phoneNumber = "",
        email = "a@a",
        uploadedPhoneNumber = null,
        uploadedEmail = null,
        submittingUpdatedInfo = false,
      ),
    ) {
      with(awaitItem().content!!) {
        assertThat(phoneNumberHasError).isFalse()
        assertThat(emailHasError).isTrue()
      }
      sendEvent(ContactInfoEvent.SubmitData)
      with(awaitItem().content!!) {
        assertThat(hasAttemptedSubmission).isTrue()
        assertThat(phoneNumberHasError).isTrue()
        assertThat(emailHasError).isTrue()
        assertThat(submittingUpdatedInfo).isFalse()
      }
      sendEvent(ContactInfoEvent.ChangeEmail("a@hedvig.com"))
      with(awaitItem().content!!) {
        assertThat(emailHasError).isFalse()
        assertThat(phoneNumberHasError).isTrue()
      }
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+4670"))
      assertThat(awaitItem().content!!.phoneNumberHasError).isTrue()
      sendEvent(ContactInfoEvent.ChangePhoneNumber("+46701234567"))
      assertThat(awaitItem().content!!.phoneNumberHasError).isFalse()
      // Emptying a field after the attempt is an error straight away
      sendEvent(ContactInfoEvent.ChangeEmail(""))
      with(awaitItem().content!!) {
        assertThat(emailHasError).isTrue()
        assertThat(submittingUpdatedInfo).isFalse()
      }
    }
  }

  @Test
  fun `Restarting from a previous state keeps its text and revisions without refetching`() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(repository, TestClock())
    val lastState = ContactInfoUiState.Content(
      phoneNumber = "+4670",
      email = "typed@hedvig",
      uploadedPhoneNumber = "+46700000000",
      uploadedEmail = Email("old@hedvig.com"),
      submittingUpdatedInfo = false,
      hasAttemptedSubmission = true,
      phoneNumberRevision = 2,
      emailRevision = 4,
    )
    presenter.test(lastState) {
      assertThat(awaitItem()).isEqualTo(lastState)
      expectNoEvents()
    }
  }
}
