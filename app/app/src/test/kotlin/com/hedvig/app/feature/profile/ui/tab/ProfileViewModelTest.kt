package com.hedvig.app.feature.profile.ui.tab

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.apollographql.apollo3.api.ApolloResponse
import com.hedvig.android.apollo.OperationResult
import com.hedvig.android.apollo.giraffe.test.GiraffeFakeResolver
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.market.Market
import com.hedvig.android.market.test.FakeMarketManager
import com.hedvig.app.authenticate.LogoutUseCase
import com.hedvig.app.feature.profile.data.ProfileRepository
import giraffe.ProfileQuery
import giraffe.UpdateEmailMutation
import giraffe.UpdatePhoneNumberMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
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

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() {
    runTest {
      val fakeProfileRepository = FakeProfileRepository(addDefaultFailedResponse = false)

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
        FakeMarketManager(Market.FR),
        noopLogoutUseCase,
      )

      viewModel.data.test {
        assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
        fakeProfileRepository.profileTurbine.add(successfulProfileQueryData.right())
        runCurrent()
        val profileUiState: ProfileUiState = viewModel.data.value
        assertThat(profileUiState.paymentInfo).isNull()
        cancelAndIgnoreRemainingEvents()
      }
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val fakeProfileRepository = FakeProfileRepository(addDefaultFailedResponse = false)

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
      fakeProfileRepository.profileTurbine.add(successfulProfileQueryData.right())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.paymentInfo).isNotNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val fakeProfileRepository = FakeProfileRepository(addDefaultFailedResponse = false)

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
      fakeProfileRepository.profileTurbine.add(failedProfileQueryData.left())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.paymentInfo).isNull()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when business-model-feature is deactivated, should not show business-model-data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeProfileRepository(),
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
      FakeProfileRepository(),
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
      FakeProfileRepository(),
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
      FakeProfileRepository(),
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
      fixedMap = mapOf(Feature.PAYMENT_SCREEN to true),
    )
    val profileRepository = FakeProfileRepository(addDefaultFailedResponse = false)
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

      assertThat(viewModel.data.value.showBusinessModel).isFalse()
      featureManager.featureTurbine.add(Feature.SHOW_BUSINESS_MODEL to true)
      runCurrent()
      assertThat(viewModel.data.value.showBusinessModel).isTrue()

      assertThat(viewModel.data.value.euroBonus).isNull()
      euroBonusStatusUseCase.responseTurbine.add(EuroBonus("1234").right())
      runCurrent()
      assertThat(viewModel.data.value.euroBonus).isEqualTo(EuroBonus("1234"))

      assertThat(viewModel.data.value.paymentInfo).isNull()
      profileRepository.profileTurbine.add(successfulProfileQueryData.right())
      runCurrent()
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

private class FakeProfileRepository(addDefaultFailedResponse: Boolean = true) : ProfileRepository {
  val profileTurbine = Turbine<Either<OperationResult.Error, ProfileQuery.Data>>(name = "profileTurbine").apply {
    if (addDefaultFailedResponse) {
      add(OperationResult.Error.GeneralError("").left())
    }
  }

  override fun profile(): Flow<Either<OperationResult.Error, ProfileQuery.Data>> {
    return profileTurbine.asChannel().consumeAsFlow()
  }

  override suspend fun updateEmail(input: String): ApolloResponse<UpdateEmailMutation.Data> {
    TODO("Not implemented")
  }

  override suspend fun updatePhoneNumber(input: String): ApolloResponse<UpdatePhoneNumberMutation.Data> {
    TODO("Not implemented")
  }

  override suspend fun writeEmailAndPhoneNumberInCache(email: String?, phoneNumber: String?) {
    TODO("Not implemented")
  }
}
