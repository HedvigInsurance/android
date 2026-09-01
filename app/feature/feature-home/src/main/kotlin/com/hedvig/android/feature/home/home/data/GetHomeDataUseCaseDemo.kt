package com.hedvig.android.feature.home.home.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.core.uidata.UiCurrencyCode
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.memberreminders.MemberReminders
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
internal class GetHomeDataUseCaseDemo(
  private val dismissedShopSessionsStorage: DismissedShopSessionsStorage,
) : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> {
    return dismissedShopSessionsStorage.observeDismissedSessionIds().map { dismissedSessionIds ->
      demoHomeData.copy(
        ongoingShopSessions = demoHomeData.ongoingShopSessions.filterNot { it.id in dismissedSessionIds },
      ).right()
    }
  }
}

private val demoCarCrossSell = CrossSell(
  "rh",
  "Car Insurance",
  "For you and your car",
  "",
  ImageAsset("", "", ""),
  ImageAsset("", "", ""),
)

private val demoPetCrossSell = CrossSell(
  "rf",
  "Pet insurance",
  "For your dog or cat",
  "",
  ImageAsset("", "", ""),
  ImageAsset("", "", ""),
)

private val demoHomeData = HomeData(
  contractStatus = HomeData.ContractStatus.Active,
  claimStatusCardsData = null,
  veryImportantMessages = listOf(),
  memberReminders = MemberReminders(
    connectPayment = null,
    upcomingRenewals = null,
    enableNotifications = null,
  ),
  hasUnseenChatMessages = false,
  showHelpCenter = true,
  firstVetSections = listOf(),
  crossSells = CrossSellSheetData(
    recommendedCrossSell =
      RecommendedCrossSell(
        crossSell = demoCarCrossSell,
        bannerText = "50% discount the first year",
        discountText = "-50%",
        buttonText = "Explore offer",
        buttonDescription = "Limited time offer",
        backgroundPillowImages = null,
        bundleProgress = null,
      ),
    otherCrossSells = listOf(demoPetCrossSell),
    recommendedAddon = null,
  ),
  discoverCrossSells = listOf(demoCarCrossSell, demoPetCrossSell),
  ongoingShopSessions = listOf(
    OngoingShopSession(
      id = "demo-session-1",
      title = "Home + Accident",
      subtitle = "Studio apartment, Stockholm",
      monthlyNet = UiMoney(199.0, UiCurrencyCode.SEK),
      resumeUrl = "https://www.hedvig.com",
      pillowImageUrl = null,
    ),
  ),
  addonBannerInfos = emptyList(),
  showChatIcon = false,
  firstName = "Demo",
  draftClaim = null,
)
