package com.hedvig.android.feature.home.home.data

import arrow.core.Either
import arrow.core.right
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedCrossSell
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.memberreminders.MemberReminders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class GetHomeDataUseCaseDemo : GetHomeDataUseCase {
  override fun invoke(forceNetworkFetch: Boolean): Flow<Either<ApolloOperationError, HomeData>> = flowOf(
    HomeData(
      contractStatus = HomeData.ContractStatus.Active,
      claimStatusCardsData = null,
      veryImportantMessages = listOf(),
      memberReminders = MemberReminders(
        connectPayment = null,
        upcomingRenewals = null,
        enableNotifications = null,
      ),
      showChatIcon = false,
      hasUnseenChatMessages = false,
      showHelpCenter = true,
      firstVetSections = listOf(),
      crossSells = CrossSellSheetData(
        recommendedCrossSell =
          RecommendedCrossSell(
            crossSell = CrossSell(
              "rh",
              "Car Insurance",
              "For you and your car",
              "",
              ImageAsset("", "", ""),
            ),
            bannerText = "50% discount the first year",
            discountText = "-50%",
            buttonText = "Explore offer",
            buttonDescription = "Limited time offer",
          ),
        otherCrossSells = listOf(
          CrossSell(
            "rf",
            "Pet insurance",
            "For your dog or cat",
            "",
            ImageAsset("", "", ""),
          ),
        ),
      ),
      travelBannerInfo = null,
    ).right(),
  )
}
