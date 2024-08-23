package com.hedvig.android.feature.profile.myinfo

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
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
      assertThat((awaitItem() as MyInfoUiState.Success).member.email).isEqualTo(member.email)
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      awaitItem()
      sendEvent(MyInfoEvent.PhoneNumberChanged("31987231987"))
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::member)
        .apply {
          prop(MyInfoMember::email).isEqualTo("newemail@gmail.com")
          prop(MyInfoMember::phoneNumber).isEqualTo("31987231987")
        }
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::isSubmitting)
        .isTrue()
      profileRepository.phoneResponseTurbine.add(member.copy(phoneNumber = "31987231987").right())
      expectNoEvents()
      profileRepository.emailResponseTurbine.add(member.copy(email = "newemail@gmail.com").right())
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .apply {
          prop(MyInfoUiState.Success::canSubmit).isFalse()
          prop(MyInfoUiState.Success::isSubmitting).isFalse()
          prop(MyInfoUiState.Success::member).isEqualTo(
            MyInfoMember("newemail@gmail.com", null, "31987231987", null),
          )
        }
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
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::canSubmit)
        .isFalse()
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::canSubmit)
        .isTrue()
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::isSubmitting)
        .isTrue()
      profileRepository.emailResponseTurbine.add(member.copy(email = "newemail@gmail.com").right())
      assertThat(awaitItem())
        .isInstanceOf<MyInfoUiState.Success>()
        .prop(MyInfoUiState.Success::canSubmit)
        .isFalse()
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
      assertThat(awaitItem()).isInstanceOf<MyInfoUiState.Success>()
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      awaitItem()
      profileRepository.phoneResponseTurbine.add(Unit.left())
      profileRepository.emailResponseTurbine.add(Unit.left())
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      assertThat(awaitItem()).isEqualTo(MyInfoUiState.Error)
    }
  }

  @Test
  fun `can recover from a network error`() = runTest {
    val profileRepository = FakeProfileRepository()
    val presenter = MyInfoPresenter { profileRepository }
    presenter.test(MyInfoUiState.Loading) {
      skipItems(1)
      profileRepository.profileResponseTurbine.add(
        ProfileData(member).right(),
      )
      skipItems(1)
      sendEvent(MyInfoEvent.EmailChanged("newemail@gmail.com"))
      skipItems(1)
      sendEvent(MyInfoEvent.UpdateEmailAndPhoneNumber)
      skipItems(1)
      profileRepository.emailResponseTurbine.add(Unit.left())
      assertThat(awaitItem()).isInstanceOf<MyInfoUiState.Error>()
      sendEvent(MyInfoEvent.Reload)
      assertThat(awaitItem()).isInstanceOf<MyInfoUiState.Loading>()
      profileRepository.profileResponseTurbine.add(
        ProfileData(member).right(),
      )
      assertThat(awaitItem()).isInstanceOf<MyInfoUiState.Success>()
    }
  }
}

internal class FakeProfileRepository : ProfileRepository {
  val profileResponseTurbine = Turbine<Either<Unit, ProfileData>>()
  val emailResponseTurbine = Turbine<Either<Unit, ProfileData.Member>>()
  val phoneResponseTurbine = Turbine<Either<Unit, ProfileData.Member>>()

  override suspend fun profile(): Either<Unit, ProfileData> {
    return profileResponseTurbine.awaitItem()
  }

  override suspend fun updateEmail(input: String): Either<Unit, ProfileData.Member> {
    return emailResponseTurbine.awaitItem()
  }

  override suspend fun updatePhoneNumber(input: String): Either<Unit, ProfileData.Member> {
    return phoneResponseTurbine.awaitItem()
  }
}
