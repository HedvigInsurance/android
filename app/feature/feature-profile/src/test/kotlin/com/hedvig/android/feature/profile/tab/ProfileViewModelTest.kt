package com.hedvig.android.feature.profile.tab

import app.cash.turbine.Turbine
import app.cash.turbine.test
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.android.auth.LogoutUseCase
import com.hedvig.android.core.common.test.MainCoroutineRule
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class ProfileViewModelTest {

  @get:Rule
  val mainCoroutineRule = MainCoroutineRule()

  private val noopLogoutUseCase = object : LogoutUseCase {
    override fun invoke() {
      // no-op
    }
  }

  @Test
  fun `when payment-feature is not activated, should not show payment-data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to false,
            Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
          )
        },
      ),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(false)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, should show payment data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
          )
        },
      ),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(true)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when payment-feature is activated, but response fails, should not show payment data`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase(),
      FakeFeatureManager(
        featureMap = {
          mapOf(
            Feature.PAYMENT_SCREEN to true,
            Feature.SHOW_BUSINESS_MODEL to Random.nextBoolean(),
          )
        },
      ),
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()
      val profileUiState: ProfileUiState = viewModel.data.value
      assertThat(profileUiState.showPaymentScreen).isEqualTo(true)
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `when euro bonus does not exist, should not show the EuroBonus status`() = runTest {
    val viewModel = ProfileViewModel(
      FakeGetEurobonusStatusUseCase {
        add(GetEurobonusError.EurobonusNotApplicable.left())
      },
      FakeFeatureManager(noopFeatureManager = true),
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
      FakeGetEurobonusStatusUseCase {
        add(EuroBonus("code1234").right())
      },
      FakeFeatureManager(noopFeatureManager = true),
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
    val euroBonusStatusUseCase = FakeGetEurobonusStatusUseCase {}

    val viewModel = ProfileViewModel(
      euroBonusStatusUseCase,
      featureManager,
      noopLogoutUseCase,
    )

    viewModel.data.test {
      assertThat(viewModel.data.value).isEqualTo(ProfileUiState())
      runCurrent()

      assertThat(viewModel.data.value.euroBonus).isNull()
      euroBonusStatusUseCase.responseTurbine.add(EuroBonus("1234").right())
      runCurrent()
      assertThat(viewModel.data.value.euroBonus).isEqualTo(EuroBonus("1234"))
      assertThat(viewModel.data.value.showPaymentScreen).isEqualTo(true)

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
