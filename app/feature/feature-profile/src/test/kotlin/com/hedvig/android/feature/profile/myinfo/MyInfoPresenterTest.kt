package com.hedvig.android.feature.profile.myinfo

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.feature.profile.data.ProfileData
import com.hedvig.android.feature.profile.data.ProfileRepository
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class MyInfoPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  private val member = ProfileData.Member(
    "idddd",
    "firstName",
    "lastName",
    "oldemail@gmail.com",
    "1234567",
  )

  @Test
  fun `if phone and email are updated they are received in state`() = runTest {
    val profileRepository = FakeProfileRepository()
    val presenter = MyInfoPresenter { profileRepository }

    presenter.test(MyInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(MyInfoUiState.Loading)
      profileRepository.profileResponseTurbine.add(
        ProfileData(member).right(),
      )
      assertThat((awaitItem() as MyInfoUiState.Success).member.email.input).isEqualTo(member.email)
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      awaitItem()
      sendEvent(MyInfoEvent.PhoneNumberChanged("31987231987"))
      awaitItem()
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      profileRepository.profileResponseTurbine.add(
        ProfileData(member.copy(phoneNumber = "31987231987", email = "newemail@gmail.com")).right(),
      )
      val result = awaitItem() as MyInfoUiState.Success
      assertThat(result.member.email.input).isEqualTo("newemail@gmail.com")
      assertThat(result.member.phoneNumber.input).isEqualTo("31987231987")
    }
  }

  @Test
  fun `if save button is clicked it is not showing anymore`() = runTest {
    val profileRepository = FakeProfileRepository()
    val presenter = MyInfoPresenter { profileRepository }
    presenter.test(MyInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(MyInfoUiState.Loading)
      profileRepository.profileResponseTurbine.add(
        ProfileData(member).right(),
      )
      val result0 = awaitItem() as MyInfoUiState.Success
      assertThat(result0.canSubmit).isEqualTo(false)
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      val result1 = awaitItem() as MyInfoUiState.Success
      assertThat(result1.canSubmit).isEqualTo(true)
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      assertThat((awaitItem() as MyInfoUiState.Success).isSubmitting).isEqualTo(true)
      profileRepository.emailResponseTurbine.add(member.copy(email = "newemail@gmail.com").right())
      profileRepository.phoneResponseTurbine.add(member.right())
      val result2 = awaitItem() as MyInfoUiState.Success
      assertThat(result2.canSubmit).isEqualTo(false)
    }
  }

  @Test
  fun `if error is received show error section`() = runTest {
    val profileRepository = FakeProfileRepository()
    val presenter = MyInfoPresenter { profileRepository }
    presenter.test(MyInfoUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(MyInfoUiState.Loading)
      profileRepository.profileResponseTurbine.add(
        ProfileData(member).right(),
      )
      assertThat(awaitItem()).isInstanceOf(MyInfoUiState.Success::class)
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      awaitItem()
      profileRepository.phoneResponseTurbine.add(OperationResult.Error.NetworkError(null, null).left())
      profileRepository.emailResponseTurbine.add(OperationResult.Error.NetworkError(null, null).left())
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      assertThat(awaitItem()).isEqualTo(MyInfoUiState.Error)
    }
  }
}

internal class FakeProfileRepository : ProfileRepository {
  val profileResponseTurbine = Turbine<Either<OperationResult.Error, ProfileData>>()
  val emailResponseTurbine = Turbine<Either<OperationResult.Error, ProfileData.Member>>()
  val phoneResponseTurbine = Turbine<Either<OperationResult.Error, ProfileData.Member>>()

  override suspend fun profile(): Either<OperationResult.Error, ProfileData> {
    return profileResponseTurbine.awaitItem()
  }

  override suspend fun updateEmail(input: String): Either<OperationResult.Error, ProfileData.Member> {
    return emailResponseTurbine.awaitItem()
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, ProfileData.Member> {
    return phoneResponseTurbine.awaitItem()
  }
}
