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
import assertk.assertions.prop
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorageImpl
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.test.test
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class HomePresenterTest {
  val testCrossSell = RecommendedCrossSell(
    crossSell = CrossSell(
      "id",
      "title",
      "subtitle",
      "url",
      ImageAsset("", "", ""),
    ),
    bannerText = "50% discount the first year",
    buttonText = "Explore offer",
    discountText = "-50%",
    buttonDescription = "Limited time offer",
  )

  @Test
  fun `asking to refresh successfully asks for a fetch from the network`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()

      getHomeDataUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("").left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      sendEvent(HomeEvent.RefreshData)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isTrue()
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Loading>()

      getHomeDataUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("").left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()
    }
  }

  @Test
  fun `getting a failed response and retrying, should result in a successful state`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("").left())
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
      { FakeCrossSellHomeNotificationService() },
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
                submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
              ),
            ),
          ),
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = true,
          hasUnseenChatMessages = false,
          showHelpCenter = false,
          crossSells = CrossSellSheetData(testCrossSell, listOf()),
          firstVetSections = listOf(),
          travelBannerInfo = null,
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
                submittedDate = Instant.parse("2024-05-01T00:00:00Z"),
              ),
            ),
          ),
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          firstVetAction = null,
          crossSellsAction = HomeTopBarAction.CrossSellsAction(
            CrossSellSheetData(testCrossSell, listOf()),
            crossSellRecommendationNotification = CrossSellRecommendationNotification
              (true, 1L),
          ),
          chatAction = HomeTopBarAction.ChatAction,
          hasUnseenChatMessages = false,
          travelAddonBannerInfo = null,
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
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(
            enableNotifications = MemberReminder.EnableNotifications(),
          ),
          showChatIcon = false,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(null, listOf()),
          firstVetSections = listOf(),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(
            connectPayment = null,
          ),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          travelAddonBannerInfo = null,
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
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(ApolloOperationError.OperationError.Other("").left())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Error>()

      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
    }
  }

  @Test
  fun `with a successfull response, the unread chat state is set according to the unread message count`(
    @TestParameter hasNotification: Boolean,
  ) = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(
            enableNotifications = MemberReminder.EnableNotifications(),
          ),
          showChatIcon = false,
          hasUnseenChatMessages = hasNotification,
          showHelpCenter = false,
          firstVetSections = listOf(),
          crossSells = CrossSellSheetData(null, listOf()),
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem())
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::hasUnseenChatMessages)
        .isEqualTo(hasNotification)
    }
  }

  @Test
  fun `if firstVet sections and crossSells lists are empty do not show first vet icon and crossSells icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = false,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(null, listOf()),
          firstVetSections = listOf(),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          travelAddonBannerInfo = null,
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
      { FakeCrossSellHomeNotificationService() },
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
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = false,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(null, listOf()),
          firstVetSections = listOf(
            firstVet,
          ),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = HomeTopBarAction.FirstVetAction(listOf(firstVet)),
          crossSellsAction = null,
          travelAddonBannerInfo = null,
        ),
      )
    }
  }

  @Test
  fun `if crossSell has recommendation or otherCrossSells list show crossSells icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )
    val crossSell = CrossSell(
      id = "id",
      title = "title",
      storeUrl = "url",
      subtitle = "subt",
      pillowImage = ImageAsset("", "", ""),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = false,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(testCrossSell, listOf(crossSell)),
          firstVetSections = listOf(),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = HomeTopBarAction.CrossSellsAction(
            CrossSellSheetData(testCrossSell, listOf(crossSell)),
            crossSellRecommendationNotification = CrossSellRecommendationNotification
              (true, 1L),
          ),
          travelAddonBannerInfo = null,
        ),
      )
    }
  }

  @Test
  fun `if should show chat show chat icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = true,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(null, emptyList()),
          firstVetSections = listOf(),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = HomeTopBarAction.ChatAction,
          firstVetAction = null,
          crossSellsAction = null,
          travelAddonBannerInfo = null,
        ),
      )
    }
  }

  @Test
  fun `if shouldn't show chat do not show chat icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      SeenImportantMessagesStorageImpl(),
      { FakeCrossSellHomeNotificationService() },
      backgroundScope,
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          showChatIcon = false,
          hasUnseenChatMessages = false,
          crossSells = CrossSellSheetData(null, emptyList()),
          firstVetSections = listOf(),
          showHelpCenter = false,
          travelBannerInfo = null,
        ).right(),
      )
      assertThat(awaitItem()).isEqualTo(
        HomeUiState.Success(
          isReloading = false,
          homeText = HomeText.Active,
          claimStatusCardsData = null,
          veryImportantMessages = listOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          travelAddonBannerInfo = null,
        ),
      )
    }
  }

  private class TestGetHomeDataUseCase : GetHomeDataUseCase {
    val forceNetworkFetchTurbine = Turbine<Boolean>()
    val responseTurbine = Turbine<Either<ApolloOperationError, HomeData>>()

    override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> {
      forceNetworkFetchTurbine.add(forceNetworkFetch)
      return responseTurbine.asChannel().receiveAsFlow()
    }
  }

  private val someIrrelevantHomeDataInstance: HomeData = HomeData(
    contractStatus = HomeData.ContractStatus.Active,
    claimStatusCardsData = null,
    veryImportantMessages = listOf(),
    memberReminders = MemberReminders(),
    showChatIcon = false,
    hasUnseenChatMessages = false,
    showHelpCenter = false,
    firstVetSections = listOf(),
    crossSells = CrossSellSheetData(null, emptyList()),
    travelBannerInfo = null,
  )
}

private class FakeCrossSellHomeNotificationService : CrossSellHomeNotificationService {
  override fun showRedDotNotification(): Flow<Boolean> {
    return flowOf(true)
  }

  override fun getLastEpochDayNewRecommendationNotificationWasShown(): Flow<Long?> {
    return flowOf(1L)
  }

  override suspend fun markAsSeen() {
  }

  override suspend fun setLastEpochDayNewRecommendationNotificationWasShown(epochDay: Long) {
  }
}
