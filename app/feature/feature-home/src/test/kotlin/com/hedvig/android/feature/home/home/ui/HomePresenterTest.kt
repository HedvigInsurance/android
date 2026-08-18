package com.hedvig.android.feature.home.home.ui

import app.cash.turbine.Turbine
import arrow.core.Either
import arrow.core.left
import arrow.core.nonEmptyListOf
import arrow.core.right
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import assertk.assertions.prop
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.core.common.ApplicationScope
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedAddon
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.claimintent.DeleteClaimIntentDraftUseCase
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.feature.home.home.data.OngoingShopSession
import com.hedvig.android.feature.home.home.data.SeenImportantMessagesStorageImpl
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.memberquickactions.GetMemberQuickActionsUseCase
import com.hedvig.android.memberquickactions.InnerHelpCenterDestination
import com.hedvig.android.memberquickactions.QuickAction
import com.hedvig.android.memberquickactions.QuickLinkDestination
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.test.test
import com.hedvig.android.notification.badge.data.crosssell.home.CrossSellHomeNotificationService
import com.hedvig.android.shared.partners.deflect.DeflectData
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import com.hedvig.android.ui.emergency.FirstVetSection
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_PAYMENTS_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE
import hedvig.resources.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE
import hedvig.resources.Res
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class HomePresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()
  val testCrossSell = RecommendedCrossSell(
    crossSell = CrossSell(
      "id",
      "title",
      "subtitle",
      "url",
      ImageAsset("", "", ""),
      ImageAsset("", "", ""),
    ),
    bannerText = "50% discount the first year",
    buttonText = "Explore offer",
    discountText = "-50%",
    buttonDescription = "Limited time offer",
    backgroundPillowImages = null,
    bundleProgress = null,
  )

  val testAddon = RecommendedAddon(
    id = "addonId",
    title = "Travel Insurance Plus",
    buttonText = "See offer",
    description = "For a safer trip abroad",
    deepLink = "https://hedvig.com/addon",
    bannerText = "Add extra safety when traveling",
    benefits = listOf("Travel up to 60 days in a row"),
    pillowImageSmall = "smallSrc",
    pillowImageLarge = "largeSrc",
  )

  @Test
  fun `asking to refresh successfully asks for a fetch from the network`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(testCrossSell, listOf(), null),
          firstVetSections = listOf(),
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          firstVetAction = null,
          crossSellsAction = HomeTopBarAction.CrossSellsAction(
            CrossSellSheetData(testCrossSell, listOf(), null),
            crossSellRecommendationNotification = CrossSellRecommendationNotification
              (true, 1L),
          ),
          chatAction = HomeTopBarAction.ChatAction,
          hasUnseenChatMessages = false,
          addonBannerInfos = emptyList(),
          isProduction = false,
          crossSellsPartition = CrossSellsPartition(
            discoverCrossSells = listOf(testCrossSell.crossSell),
          ),
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `a recommended addon without any cross sells still shows the cross sells top bar action`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    val addonOnlyCrossSells = CrossSellSheetData(null, listOf(), testAddon)

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
          crossSells = addonOnlyCrossSells,
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = listOf(),
          draftClaim = null,
        ).right(),
      )
      assertThat(awaitItem())
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::crossSellsAction)
        .isEqualTo(
          HomeTopBarAction.CrossSellsAction(
            addonOnlyCrossSells,
            CrossSellRecommendationNotification(true, 1L),
          ),
        )
    }
  }

  @Test
  fun `the notification member reminder must not show for the home presenter`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, listOf(), null),
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          addonBannerInfos = emptyList(),
          isProduction = false,
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `receiving a failed state and then a successful one propagates the success without having to retry`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
  fun `with a successful response, the unread chat state is set according to the unread message count`(
    @TestParameter hasNotification: Boolean,
  ) = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, listOf(), null),
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, listOf(), null),
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          addonBannerInfos = emptyList(),
          isProduction = false,
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `if not empty firstVet sections list state should have first vet action with same firstVetSections`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, listOf(), null),
          firstVetSections = listOf(
            firstVet,
          ),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = HomeTopBarAction.FirstVetAction(listOf(firstVet)),
          crossSellsAction = null,
          addonBannerInfos = emptyList(),
          isProduction = false,
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `if crossSell has recommendation or otherCrossSells list show crossSells icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    val crossSell = CrossSell(
      id = "id",
      title = "title",
      storeUrl = "url",
      subtitle = "subt",
      pillowImageSmall = ImageAsset("", "", ""),
      pillowImageLarge = ImageAsset("", "", ""),
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
          crossSells = CrossSellSheetData(testCrossSell, listOf(crossSell), null),
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = HomeTopBarAction.CrossSellsAction(
            CrossSellSheetData(testCrossSell, listOf(crossSell), null),
            crossSellRecommendationNotification = CrossSellRecommendationNotification
              (true, 1L),
          ),
          addonBannerInfos = emptyList(),
          isProduction = false,
          crossSellsPartition = CrossSellsPartition(
            discoverCrossSells = listOf(testCrossSell.crossSell, crossSell),
          ),
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `if should show chat show chat icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, emptyList(), null),
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = HomeTopBarAction.ChatAction,
          firstVetAction = null,
          crossSellsAction = null,
          addonBannerInfos = emptyList(),
          isProduction = false,
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `if shouldn't show chat do not show chat icon`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
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
          crossSells = CrossSellSheetData(null, emptyList(), null),
          firstVetSections = listOf(),
          showHelpCenter = false,
          addonBannerInfos = emptyList(),
          draftClaim = null,
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
          quickActions = emptyList(),
          hasUnseenChatMessages = false,
          chatAction = null,
          firstVetAction = null,
          crossSellsAction = null,
          addonBannerInfos = emptyList(),
          isProduction = false,
          draftClaim = null,
        ),
      )
    }
  }

  @Test
  fun `the recommended crossSell leads the discover list, ahead of the other crossSells`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    val otherCrossSell = CrossSell(
      id = "other",
      title = "title",
      subtitle = "subt",
      storeUrl = "url",
      pillowImageSmall = ImageAsset("", "", ""),
      pillowImageLarge = ImageAsset("", "", ""),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        someIrrelevantHomeDataInstance.copy(
          crossSells = CrossSellSheetData(testCrossSell, listOf(otherCrossSell), recommendedAddon = null),
        ).right(),
      )
      assertThat(awaitItem())
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::crossSellsPartition)
        .isEqualTo(
          CrossSellsPartition(
            discoverCrossSells = listOf(testCrossSell.crossSell, otherCrossSell),
          ),
        )
    }
  }

  @Test
  fun `deleting the draft claim calls the use case and reloads home on success`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val deleteClaimIntentDraftUseCase = TestDeleteClaimIntentDraftUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      deleteClaimIntentDraftUseCase,
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()
      getHomeDataUseCase.responseTurbine.add(
        someIrrelevantHomeDataInstance.copy(
          draftClaim = HomeData.DraftClaim("draft-id", "My things", Instant.parse("2026-07-01T00:00:00Z")),
        ).right(),
      )
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()

      sendEvent(HomeEvent.DeleteDraftClaim("draft-id"))
      assertThat(deleteClaimIntentDraftUseCase.deletedIdsTurbine.awaitItem()).isEqualTo("draft-id")
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isTrue()
      cancelAndIgnoreRemainingEvents()
    }
  }

  @Test
  fun `firstName is propagated to the success state`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      getHomeDataUseCase.responseTurbine.add(
        someIrrelevantHomeDataInstance.copy(firstName = "Richard").right(),
      )
      assertThat(awaitItem())
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::firstName)
        .isEqualTo("Richard")
    }
  }

  @Test
  fun `a failed draft deletion does not reload home`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val deleteClaimIntentDraftUseCase = TestDeleteClaimIntentDraftUseCase().apply {
      result = ErrorMessage().left()
    }
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      deleteClaimIntentDraftUseCase,
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      assertThat(getHomeDataUseCase.forceNetworkFetchTurbine.awaitItem()).isFalse()
      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()

      sendEvent(HomeEvent.DeleteDraftClaim("draft-id"))
      assertThat(deleteClaimIntentDraftUseCase.deletedIdsTurbine.awaitItem()).isEqualTo("draft-id")
      getHomeDataUseCase.forceNetworkFetchTurbine.expectNoEvents()
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

  @Test
  fun `home shows the first three member quick actions with sick-abroad filtered out`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val quickActions = listOf(
      editInsuranceMultiSelect,
      changeAddressLink,
      sickAbroadLink,
      paymentsLink,
    )
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(quickActions.right()),
    )
    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isInstanceOf<HomeUiState.Loading>()
      getHomeDataUseCase.responseTurbine.add(someIrrelevantHomeDataInstance.right())
      val success = assertThat(awaitItem()).isInstanceOf<HomeUiState.Success>()
      success.prop(HomeUiState.Success::quickActions)
        .isEqualTo(listOf(editInsuranceMultiSelect, changeAddressLink, paymentsLink))
    }
  }

  private val editInsuranceMultiSelect = QuickAction.MultiSelectExpandedLink(
    titleRes = Res.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_TITLE,
    hintTextRes = Res.string.HC_QUICK_ACTIONS_EDIT_INSURANCE_SUBTITLE,
    links = listOf(
      QuickAction.StandaloneQuickLink(
        titleRes = Res.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_TITLE,
        hintTextRes = Res.string.HC_QUICK_ACTIONS_UPGRADE_COVERAGE_SUBTITLE,
        quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeTier,
      ),
    ),
  )
  private val changeAddressLink = QuickAction.StandaloneQuickLink(
    titleRes = Res.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_TITLE,
    hintTextRes = Res.string.HC_QUICK_ACTIONS_CHANGE_ADDRESS_SUBTITLE,
    quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkChangeAddress,
  )
  private val paymentsLink = QuickAction.StandaloneQuickLink(
    titleRes = Res.string.HC_QUICK_ACTIONS_PAYMENTS_TITLE,
    hintTextRes = Res.string.HC_QUICK_ACTIONS_PAYMENTS_SUBTITLE,
    quickLinkDestination = QuickLinkDestination.OuterDestination.QuickLinkConnectPayment,
  )
  private val sickAbroadLink = QuickAction.StandaloneQuickLink(
    titleRes = Res.string.HC_QUICK_ACTIONS_SICK_ABROAD_TITLE,
    hintTextRes = Res.string.HC_QUICK_ACTIONS_SICK_ABROAD_SUBTITLE,
    quickLinkDestination = InnerHelpCenterDestination.QuickLinkSickAbroad(
      DeflectData(
        title = null,
        infoText = null,
        warningText = null,
        partnersContainer = null,
        partnersInfo = null,
        content = DeflectData.InfoBlock("", ""),
        faq = emptyList(),
        buttonText = "",
      ),
    ),
  )

  @Test
  fun `ongoing shop sessions propagate to the success ui state`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      getHomeDataUseCase,
      SeenImportantMessagesStorageImpl(),
      FakeCrossSellHomeNotificationService(),
      ApplicationScope(backgroundScope),
      false,
      TestDeleteClaimIntentDraftUseCase(),
      FakeGetMemberQuickActionsUseCase(emptyList<QuickAction>().right()),
    )

    val session = OngoingShopSession(
      id = "session-1",
      title = "Home + Accident",
      subtitle = null,
      monthlyNet = UiMoney(199.0, UiCurrencyCode.SEK),
      resumeUrl = "https://hedvig.com/resume/session-1",
      pillowImageUrl = null,
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)
      getHomeDataUseCase.responseTurbine.add(
        someIrrelevantHomeDataInstance.copy(ongoingShopSessions = listOf(session)).right(),
      )
      val success = awaitItem()
      assertThat(success)
        .isInstanceOf<HomeUiState.Success>()
        .prop(HomeUiState.Success::ongoingShopSessions)
        .containsExactly(session)
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
    crossSells = CrossSellSheetData(null, emptyList(), null),
    addonBannerInfos = emptyList(),
    draftClaim = null,
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

private class FakeGetMemberQuickActionsUseCase(
  private val result: Either<ErrorMessage, List<QuickAction>>,
) : GetMemberQuickActionsUseCase {
  override suspend fun invoke(): Either<ErrorMessage, List<QuickAction>> = result
}

private class TestDeleteClaimIntentDraftUseCase : DeleteClaimIntentDraftUseCase {
  val deletedIdsTurbine = Turbine<String>()
  var result: Either<ErrorMessage, Unit> = Unit.right()

  override suspend fun invoke(id: String): Either<ErrorMessage, Unit> {
    deletedIdsTurbine.add(id)
    return result
  }
}
