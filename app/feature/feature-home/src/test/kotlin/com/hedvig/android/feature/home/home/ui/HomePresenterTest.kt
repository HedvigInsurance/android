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
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.chat.read.timestamp.FakeChatLastMessageReadRepository
import com.hedvig.android.feature.home.home.data.ChatMessage
import com.hedvig.android.feature.home.home.data.GetHomeDataUseCase
import com.hedvig.android.feature.home.home.data.HomeData
import com.hedvig.android.featureflags.flags.Feature
import com.hedvig.android.featureflags.test.FakeFeatureManager2
import com.hedvig.android.memberreminders.MemberReminder
import com.hedvig.android.memberreminders.MemberReminders
import com.hedvig.android.molecule.test.test
import com.hedvig.android.ui.claimstatus.model.ClaimStatusCardUiState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class HomePresenterTest {
  @Test
  fun `asking to refresh successfully asks for a fetch from the network`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      FakeChatLastMessageReadRepository(),
      FakeFeatureManager2(),
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
      FakeChatLastMessageReadRepository(),
      FakeFeatureManager2(),
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
      FakeChatLastMessageReadRepository(),
      FakeFeatureManager2(),
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
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          hasClaims = true,
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
              ),
            ),
          ),
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(),
          isHelpCenterEnabled = false,
          showChatIcon = false,
          hasUnseenChatMessages = false,
          hasClaims = true,
        ),
      )
    }
  }

  @Test
  fun `the notification member reminder must not show for the home presenter`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      FakeChatLastMessageReadRepository(),
      FakeFeatureManager2(),
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
          hasClaims = true,
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
          showChatIcon = false,
          hasUnseenChatMessages = false,
          hasClaims = true,
        ),
      )
    }
  }

  @Test
  fun `receiving a failed state and then a successful one propagates the success without having to retry`() = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      FakeChatLastMessageReadRepository(),
      FakeFeatureManager2(),
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
  fun `when the disable chat feature flag is true, the chat icon should remain hidden`() = runTest {
    val featureManager = FakeFeatureManager2()
    val homePresenter = HomePresenter(
      { TestGetHomeDataUseCase() },
      FakeChatLastMessageReadRepository(),
      featureManager,
    )

    homePresenter.test(
      initialState = HomeUiState.Success(
        isReloading = true,
        homeText = HomeText.Active,
        claimStatusCardsData = null,
        veryImportantMessages = persistentListOf(),
        memberReminders = MemberReminders(),
        isHelpCenterEnabled = false,
        showChatIcon = false,
        hasUnseenChatMessages = false,
        hasClaims = true,
      ),
    ) {
      assertThat(awaitItem().showChatIcon).isFalse()
      featureManager.featureTurbine.add(Feature.DISABLE_CHAT to true)
    }
  }

  @Test
  fun `when the disable chat feature flag is false, the chat icon should now show`() = runTest {
    val featureManager = FakeFeatureManager2(mapOf(Feature.HELP_CENTER to false))
    val homePresenter = HomePresenter(
      { TestGetHomeDataUseCase() },
      FakeChatLastMessageReadRepository(),
      featureManager,
    )

    homePresenter.test(
      HomeUiState.Success(
        isReloading = true,
        homeText = HomeText.Active,
        claimStatusCardsData = null,
        veryImportantMessages = persistentListOf(),
        memberReminders = MemberReminders(),
        isHelpCenterEnabled = false,
        showChatIcon = false,
        hasUnseenChatMessages = false,
        hasClaims = true,
      ),
    ) {
      assertThat(awaitItem().showChatIcon).isFalse()
      featureManager.featureTurbine.add(Feature.DISABLE_CHAT to false)
      assertThat(awaitItem().showChatIcon).isTrue()
    }
  }

  @Test
  fun `when the disable help center feature flag is true it should show`() = runTest {
    val featureManager = FakeFeatureManager2(mapOf(Feature.DISABLE_CHAT to true))
    val homePresenter = HomePresenter(
      { TestGetHomeDataUseCase() },
      FakeChatLastMessageReadRepository(),
      featureManager,
    )

    homePresenter.test(
      HomeUiState.Success(
        isReloading = true,
        homeText = HomeText.Active,
        claimStatusCardsData = null,
        veryImportantMessages = persistentListOf(),
        memberReminders = MemberReminders(),
        isHelpCenterEnabled = false,
        showChatIcon = false,
        hasUnseenChatMessages = false,
        hasClaims = true,
      ),
    ) {
      assertThat(awaitItem().isHelpCenterEnabled).isFalse()
      featureManager.featureTurbine.add(Feature.HELP_CENTER to true)
      assertThat(awaitItem().isHelpCenterEnabled).isTrue()
    }
  }

  @Test
  fun `with a successfull response, the unread chat state is set according to the ChatLastMessageReadRepository`(
    @TestParameter hasNotification: Boolean,
  ) = runTest {
    val getHomeDataUseCase = TestGetHomeDataUseCase()
    val chatLastMessageReadRepository = FakeChatLastMessageReadRepository()
    val homePresenter = HomePresenter(
      { getHomeDataUseCase },
      chatLastMessageReadRepository,
      FakeFeatureManager2(),
    )

    homePresenter.test(HomeUiState.Loading) {
      assertThat(awaitItem()).isEqualTo(HomeUiState.Loading)

      chatLastMessageReadRepository.isNewestMessageNewerThanLastReadTimestamp.add(hasNotification)
      getHomeDataUseCase.responseTurbine.add(
        HomeData(
          contractStatus = HomeData.ContractStatus.Active,
          claimStatusCardsData = null,
          veryImportantMessages = persistentListOf(),
          memberReminders = MemberReminders(
            enableNotifications = MemberReminder.EnableNotifications(),
          ),
          hasClaims = true,
        ).right(),
      )
      assertThat(awaitItem())
        .isInstanceOf<HomeUiState.Success>().prop(HomeUiState.Success::hasUnseenChatMessages)
        .isEqualTo(hasNotification)
    }
  }

  private class TestGetHomeDataUseCase() : GetHomeDataUseCase {
    val forceNetworkFetchTurbine = Turbine<Boolean>()
    val responseTurbine = Turbine<Either<ErrorMessage, HomeData>>()

    override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ErrorMessage, HomeData>> {
      forceNetworkFetchTurbine.add(forceNetworkFetch)
      return responseTurbine.asChannel().receiveAsFlow()
    }

    override fun observeChatMessages(): Flow<Either<ErrorMessage, List<ChatMessage>>> {
      return flowOf(ErrorMessage("Not implemented").left())
    }
  }

  private val someIrrelevantHomeDataInstance: HomeData = HomeData(
    contractStatus = HomeData.ContractStatus.Active,
    claimStatusCardsData = null,
    veryImportantMessages = persistentListOf(),
    memberReminders = MemberReminders(),
    hasClaims = true,
  )
}
