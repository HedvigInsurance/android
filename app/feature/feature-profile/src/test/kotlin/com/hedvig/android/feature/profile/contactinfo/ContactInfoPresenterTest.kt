package com.hedvig.android.feature.profile.contactinfo

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
  fun `Changing the info to a new valid input should be reflected in the state after it `() = runTest {
    val repository = ContactInfoRepositoryImpl(apolloClient, NoopNetworkCacheManager)
    val presenter = ContactInfoPresenter(Provider { repository })
    apolloClient.registerSuspendingTestResponse(
      ContactInformationQuery(),
      ContactInformationQuery.Data {
        this.currentMember = this.buildMember {
          this.phoneNumber = "+123"
          this.email = "test@hedvig.com"
        }
      },
    )
    presenter.test(ContactInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(ContactInfoUiState.Loading)
      with(awaitItem()) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo("+123")
        assertThat(this.content!!.emailState.text).isEqualTo("test@hedvig.com")
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber("+123"))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email("test@hedvig.com"))
        assertThat(this.content!!.submittingUpdatedInfo).isEqualTo(false)
        assertThat(this.content!!.canSubmit).isEqualTo(false)
        content!!.phoneNumberState.edit { append("456") }
        content!!.emailState.edit { delete(length - 1, length) }
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber("+123"))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email("test@hedvig.com"))
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
        MemberUpdateEmailMutation("test@hedvig.co"),
        MemberUpdateEmailMutation.Data {
          this.memberUpdateEmail = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = "+123"
              this.email = "test@hedvig.co"
            }
          }
        },
      )
      apolloClient.registerSuspendingTestResponse(
        MemberUpdatePhoneNumberMutation("+123456"),
        MemberUpdatePhoneNumberMutation.Data {
          this.memberUpdatePhoneNumber = this.buildMemberMutationOutput {
            this.userError = null
            this.member = this.buildMember {
              this.phoneNumber = "+123456"
              this.email = "test@hedvig.com"
            }
          }
        },
      )
      with(awaitItem().content!!) {
        assertThat(this.content!!.phoneNumberState.text).isEqualTo("+123456")
        assertThat(this.content!!.emailState.text).isEqualTo("test@hedvig.co")
        assertThat(this.content!!.uploadedPhoneNumber).isEqualTo(PhoneNumber("+123456"))
        assertThat(this.content!!.uploadedEmail).isEqualTo(Email("test@hedvig.co"))
        assertThat(this.content!!.submittingUpdatedInfo).isFalse()
      }
    }
  }
}
