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
import com.hedvig.android.data.contract.android.CrossSell
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorageImpl
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.test.test
import com.hedvig.android.notification.badge.data.crosssell.card.FakeCrossSellCardNotificationBadgeService
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class HomePresenterTest {
  @Test
  fun `asking to refresh successfully asks for a fetch from the network`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

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
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

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
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = HomeData.ClaimStatusCardsData(
            nonEmptyListOf(
              ClaimStatusCardUiState(
                id = "id",
                pillTypes = emptyList(),
                claimProgressItemsUiState = emptyList(),
                claimType = "Broken item",
                insuranceDisplayName = "Home Insurance",
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          showChatIcon = false,
          showHelpCenter = false,
          crossSells = persistentListOf(),
          firstVetSections = listOf(),
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
                pillTypes = emptyList(),
                claimProgressItemsUiState = emptyList(),
                claimType = "Broken item",
                insuranceDisplayName = "Home Insurance",
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
        ),
      )
    }
  }

  @Test
  fun `the notification member reminder must not show for the home presenter`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(
            enableNotifications = MemberReminder.EnableNotifications(),
          ),
          crossSells = persistentListOf(),
          firstVetSections = listOf(),
          showChatIcon = false,
          showHelpCenter = false,
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
          isHelpCenterEnabled = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
        ),
      )
    }
  }

  @Test
  fun `receiving a failed state and then a successful one propagates the success without having to retry`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(ErrorMessage().left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
    }
  }

  @Test
  fun `if firstVet sections and crossSells lists are empty do not show first vet icon and crossSells icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          crossSells = persistentListOf(),
          showChatIcon = false,
          firstVetSections = listOf(),
          showHelpCenter = false,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
        ),
      )
    }
  }

  @Test
  fun `if not empty firstVet sections list state should have first vet action with same firstVetSections`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )
    val firstVet = FirstVetSection(
      buttonTitle = "ButtonTitle",
      description = "description",
      title = "title",
      url = null,
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          crossSells = persistentListOf(),
          showChatIcon = false,
          firstVetSections = listOf(firstVet),
          showHelpCenter = false,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          chatAction = null,
          firstVetAction = HomeTopBarAction.FirstVetAction(listOf(firstVet)),
          crossSellsAction = null,
        ),
      )
    }
  }

  @Test
  fun `if not empty crossSells list show crossSells icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellCardNotificationBadgeService() },
      backgroundScope,
    )
    val crossSell = CrossSell(
      id = "id",
      title = "title",
      storeUrl = "url",
      subtitle = "subt",
      type = CrossSell.CrossSellType.HOME,
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          crossSells = persistentListOf(crossSell),
          showChatIcon = false,
          firstVetSections = listOf(),
          showHelpCenter = false,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = HomeTopBarAction.CrossSellsAction(persistentListOf(crossSell)),
        ),
      )
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
    showChatIcon = false,
    showHelpCenter = false,
    firstVetSections = listOf(),
    crossSells = persistentListOf(),
  )
}
