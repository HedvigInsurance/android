package com.hedvig.android.feature.home.home.ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.hanalytics.featureflags.test.FakeFeatureManager2
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.test.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class HomePresenterTest {

  @Test
  fun `asking to refresh successfully asks for a fetch from the network`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(Provider { getHomeDataUseCase }, FakeFeatureManager2())

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()

      getHomeDataUseCase.responseTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      sendEvent(HomeEvent.RefreshData)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isTrue()
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Loading>()

      getHomeDataUseCase.responseTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()
    }
  }

  @Test
  fun `getting a failed response and retrying, should result in a successful state`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(Provider { getHomeDataUseCase }, FakeFeatureManager2())

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      sendEvent(HomeEvent.RefreshData)
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Loading>()

      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
    }
  }

  @Test
  fun `a successful response, properly propagates the info to the UI State`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(Provider { getHomeDataUseCase }, FakeFeatureManager2())

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                title = "title",
                subtitle = "subtitle",
                pillTypes = emptyList(),
                claimProgressItemsUiState = emptyList(),
              ),
            ),
          ),
          memberReminders = MemberReminders(),
          veryImportantMessages = persistentListOf(),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = false,
          emergencyData = null,
          commonClaimsData = persistentListOf(),
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                title = "title",
                subtitle = "subtitle",
                pillTypes = emptyList(),
                claimProgressItemsUiState = emptyList(),
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = false,
          emergencyData = null,
          commonClaimsData = persistentListOf(),
          showChatIcon = false,
        ),
      )
    }
  }

  @Test
  fun `the notification member reminder must not show for the home presenter`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(Provider { getHomeDataUseCase }, FakeFeatureManager2())

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          memberReminders = MemberReminders(
            enableNotifications = MemberReminder.EnableNotifications,
          ),
          veryImportantMessages = persistentListOf(),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = false,
          emergencyData = null,
          commonClaimsData = persistentListOf(),
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(
            connectPayment = null,
          ),
          allowAddressChange = true,
          allowGeneratingTravelCertificate = false,
          emergencyData = null,
          commonClaimsData = persistentListOf(),
          showChatIcon = false,
        ),
      )
    }
  }

  @Test
  fun `receiving a failed state and then a successful one propagates the success without having to retry`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(Provider { getHomeDataUseCase }, FakeFeatureManager2())

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
    }
  }

  @Test
  fun `when the disable chat feature flag is true, the chat icon should remain hidden`() = runTest {
    val featureManager = FakeFeatureManager2()
    val homePresenter = HomePresenter(
      Provider { TestGetHomeDataUseCase() },
      featureManager,
    )

    homePresenter.test(
      HomeUiState.Success(
        isReloading = true,
        HomeText.Active,
        null,
        persistentListOf(),
        MemberReminders(),
        false,
        false,
        null,
        persistentListOf(),
        showChatIcon = false,
      ),
    ) {
      assertThat(awaitItem().showChatIcon).isFalse()
      featureManager.featureTurbine.add(Feature.DISABLE_CHAT to true)
    }
  }

  @Test
  fun `when the disable chat feature flag is false, the chat icon should now show`() = runTest {
    val featureManager = FakeFeatureManager2()
    val homePresenter = HomePresenter(
      Provider { TestGetHomeDataUseCase() },
      featureManager,
    )

    homePresenter.test(
      HomeUiState.Success(
        isReloading = true,
        HomeText.Active,
        null,
        persistentListOf(),
        MemberReminders(),
        false,
        false,
        null,
        persistentListOf(),
        showChatIcon = false,
      ),
    ) {
      assertThat(awaitItem().showChatIcon).isFalse()
      featureManager.featureTurbine.add(Feature.DISABLE_CHAT to false)
      assertThat(awaitItem().showChatIcon).isTrue()
    }
  }

  private class TestGetHomeDataUseCase() : GetHomeDataUseCase {
    val forceNetworkFetchTurbine = Turbine<Boolean>()
    val responseTurbine = Turbine<Either<ErrorMessage, HomeData>>()
    override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>> {
      forceNetworkFetchTurbine.add(forceNetworkFetch)
      return responseTurbine.asChannel().receiveAsFlow()
    }
  }

  private val someIrrelevantHomeDataInstance: HomeData = HomeData(
    contractStatus = HomeData.ContractStatus.Active,
    claimStatusCardsData = null,
    veryImportantMessages = persistentListOf(),
    memberReminders = MemberReminders(),
    allowAddressChange = true,
    allowGeneratingTravelCertificate = false,
    emergencyData = null,
    commonClaimsData = persistentListOf(),
  )
}
