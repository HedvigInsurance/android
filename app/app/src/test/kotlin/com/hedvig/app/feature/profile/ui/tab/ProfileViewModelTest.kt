package com.hedvig.app.feature.profile.ui.tab

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.market.Market
import com.hedvig.android.market.test.FakeMarketManager
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ChargeEstimation
import com.hedvig.app.feature.profile.data.Member
import com.hedvig.app.feature.profile.data.ProfileData
import com.hedvig.app.feature.profile.data.ProfileRepository
import giraffe.ProfileQuery
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import java.math.BigDecimal
import kotlin.random.Random

class ProfileViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val successfulProfileQueryData = ProfileQuery.Data(GiraffeFakeResolver)
  private val failedProfileQueryData = OperationResult.Error.GeneralError("")
  private val noopLogoutUseCase = object : LogoutUseCase {
    override fun invoke() {
      // no-op
    }
  }

  private val mockedProfile: Either<OperationResult.Error, ProfileData> = either {
    ProfileData(
      member = Member(id = "", firstName = "", lastName = "", email = "", phoneNumber = ""),
      chargeEstimation = ChargeEstimation(
        subscription = Money.of(BigDecimal(100), "SEK"),
        discount = Money.of(BigDecimal(100), "SEK"),
        charge = Money.of(BigDecimal(100), "SEK"),
      ),
      directDebitStatus = null,
      activePaymentMethods = null,
    )
  }

  private val mockedError: Either<OperationResult.Error, ProfileData> = either {
    raise(OperationResult.Error.NetworkError(message = "test"))
  }

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() {
    runTest {
      val fakeProfileRepository = FakeProfileRepository(mockedProfile)

      val viewModel = ProfileViewModel(
        fakeProfileRepository,
        FakeGetEurobonusStatusUseCase(),
        FakeFeatureManager(
          featureMap = {
            mapOf(
              Feature.PAYMENT_SCREEN to false,
              Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
            )
          },
        ),
        FakeMarketManager(Market.SE),
        noopLogoutUseCase,
      )

      viewModel.data.test {
        assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
        runCurrent()
        val profileUiState: ProfileUiState = viewModel.data.value
        assertThat(profileUiState.paymentInfo).isNull()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val fakeProfileRepository = FakeProfileRepository(mockedProfile)

    val viewModel = ProfileViewModel(
      fakeProfileRepository,
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
          )
        },
      ),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.paymentInfo).isNotNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val fakeProfileRepository = FakeProfileRepository(mockedError)

    val viewModel = ProfileViewModel(
      fakeProfileRepository,
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
          )
        },
      ),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.paymentInfo).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when business-model-feature is deactivated, should not show business-model-data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeProfileRepository(mockedProfile),
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to Random.nextBoolean(),
            Feature.SHOW_BUSINESS_MODEL to false,
          )
        },
      ),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showBusinessModel).isEqualTo(false)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when business-model-feature is activated, should show business-model-data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeProfileRepository(mockedProfile),
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to Random.nextBoolean(),
            Feature.SHOW_BUSINESS_MODEL to true,
          )
        },
      ),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showBusinessModel).isEqualTo(true)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus does not exist, should not show the EuroBonus status`() = runTest {
    val viewModel = ProfileViewModel(
      FakeProfileRepository(mockedProfile),
      FakeGetEurobonusStatusUseCase {
        add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      FakeFeatureManager(noopFeatureManager = true),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.euroBonus).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus exists, should show the EuroBonus status`() = runTest {
    val viewModel = ProfileViewModel(
      FakeProfileRepository(mockedProfile),
      FakeGetEurobonusStatusUseCase {
        add(EuroBonus("code1234").right())
      },
      FakeFeatureManager(noopFeatureManager = true),
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.euroBonus).isEqualTo(EuroBonus("code1234"))
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `Initially all optional items are off, and as they come in, they show one by one`() = runTest {
    val featureManager = FakeFeatureManager2(
      fixedMap = mapOf(Feature.PAYMENT_SCREEN to true, Feature.SHOW_BUSINESS_MODEL to true),
    )
    val profileRepository = FakeProfileRepository(mockedProfile)
    val euroBonusStatusUseCase = FakeGetEurobonusStatusUseCase {}

    val viewModel = ProfileViewModel(
      profileRepository,
      euroBonusStatusUseCase,
      featureManager,
      FakeMarketManager(Market.SE),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      assertThat(viewModel.data.value.euroBonus).isNull()
      euroBonusStatusUseCase.responseTurbine.add(EuroBonus("1234").right())
      runCurrent()
      assertThat(viewModel.data.value.euroBonus).isEqualTo(EuroBonus("1234"))
      assertThat(viewModel.data.value.paymentInfo).isNotNull()

      cancelAndIgnoreRemainingEvents()
    }
  }
}

private class FakeGetEurobonusStatusUseCase(
  block: Turbine<Either<GetEurobonusError, EuroBonus>>.() -> Unit = {
    add(GetEurobonusError.EurobonusNotApplicable.left())
  },
) : GetEurobonusStatusUseCase {
  val responseTurbine = Turbine<Either<GetEurobonusError, EuroBonus>>(name = "EurobonusResponse").apply(block)

  override suspend fun invoke(): Either<GetEurobonusError, EuroBonus> {
    return responseTurbine.awaitItem()
  }
}

private class FakeProfileRepository(val mockedProfile: Either<OperationResult.Error, ProfileData>) : ProfileRepository {

  override suspend fun profile(): Either<OperationResult.Error, ProfileData> {
    return mockedProfile
  }

  override suspend fun updateEmail(input: String): Either<OperationResult.Error, Member> {
    TODO("Not implemented")
  }

  override suspend fun updatePhoneNumber(input: String): Either<OperationResult.Error, Member> {
    TODO("Not implemented")
  }
}
