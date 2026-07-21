package com.hedvig.android.feature.cross.sell.sheet

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.crosssells.CrossSellFlowSource
import com.hedvig.android.crosssells.CrossSellImpressionTracker
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.CrossSellType
import com.hedvig.android.crosssells.CrossSellUserFlow
import com.hedvig.android.crosssells.RecommendedAddon
import com.hedvig.android.data.contract.CrossSell
import com.hedvig.android.data.contract.ImageAsset
import com.hedvig.android.data.cross.sell.after.flow.CrossSellAfterFlowRepository
import com.hedvig.android.data.cross.sell.after.flow.CrossSellInfoType
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import octopus.type.CrossSellInput
import org.junit.Test

internal class CrossSellSheetPresenterTest {
  private val recommendedAddon = RecommendedAddon(
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
  fun `a response with nothing to show does not show the sheet`() = runTest {
    val emptyData = CrossSellSheetData(null, emptyList(), null)
    val presenter = CrossSellSheetPresenter(
      FakeGetCrossSellSheetDataUseCase(emptyData),
      FakeCrossSellAfterFlowRepository(CrossSellInfoType.EditCoInsured),
      RecordingCrossSellImpressionTracker(),
    )

    presenter.test(CrossSellSheetState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.Loading)
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.DontShow)
    }
  }

  @Test
  fun `a response with a recommendation shows the sheet`() = runTest {
    val addonData = CrossSellSheetData(null, emptyList(), recommendedAddon)
    val presenter = CrossSellSheetPresenter(
      FakeGetCrossSellSheetDataUseCase(addonData),
      FakeCrossSellAfterFlowRepository(CrossSellInfoType.EditCoInsured),
      RecordingCrossSellImpressionTracker(),
    )

    presenter.test(CrossSellSheetState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.Loading)
      assertThat(awaitItem()).isEqualTo(
        CrossSellSheetState.Content(addonData, CrossSellInfoType.EditCoInsured),
      )
    }
  }

  @Test
  fun `showing the sheet tracks an impression per shown offer`() = runTest {
    val newPromise = CrossSell(
      id = "cs1",
      title = "Car",
      subtitle = "Car insurance",
      storeUrl = "url",
      pillowImage = ImageAsset(id = "img", src = "src", description = "alt"),
    )
    val data = CrossSellSheetData(
      recommendedCrossSell = null,
      otherCrossSells = listOf(newPromise),
      recommendedAddon = recommendedAddon,
    )
    val tracker = RecordingCrossSellImpressionTracker()
    val presenter = CrossSellSheetPresenter(
      FakeGetCrossSellSheetDataUseCase(data),
      FakeCrossSellAfterFlowRepository(CrossSellInfoType.EditCoInsured),
      tracker,
    )

    presenter.test(CrossSellSheetState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.Loading)
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.Content(data, CrossSellInfoType.EditCoInsured))
      sendEvent(CrossSellSheetEvent.CrossSellSheetShown)
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.DontShow)
    }

    assertThat(tracker.shown).isEqualTo(
      listOf(
        RecordingCrossSellImpressionTracker.Impression(
          CrossSellUserFlow.SmartXSell,
          CrossSellType.Addon,
          "addonId",
          CrossSellFlowSource.EditCoInsured,
        ),
        RecordingCrossSellImpressionTracker.Impression(
          CrossSellUserFlow.SmartXSell,
          CrossSellType.NewPromise,
          "cs1",
          CrossSellFlowSource.EditCoInsured,
        ),
      ),
    )
  }
}

private class RecordingCrossSellImpressionTracker : CrossSellImpressionTracker {
  data class Impression(
    val userFlow: CrossSellUserFlow,
    val crossSellType: CrossSellType,
    val offerId: String,
    val flowSource: CrossSellFlowSource?,
  )

  val shown = mutableListOf<Impression>()

  override fun crossSellShown(
    userFlow: CrossSellUserFlow,
    crossSellType: CrossSellType,
    offerId: String,
    flowSource: CrossSellFlowSource?,
  ) {
    shown += Impression(userFlow, crossSellType, offerId, flowSource)
  }
}

private class FakeGetCrossSellSheetDataUseCase(
  private val data: CrossSellSheetData,
) : GetCrossSellSheetDataUseCase {
  override suspend fun invoke(source: CrossSellInput): Flow<Either<ErrorMessage, CrossSellSheetData>> {
    return flowOf(data.right())
  }
}

private class FakeCrossSellAfterFlowRepository(
  initialInfoType: CrossSellInfoType?,
) : CrossSellAfterFlowRepository {
  private val infoType = MutableStateFlow(initialInfoType)

  override fun shouldShowCrossSellSheetWithInfo(): Flow<CrossSellInfoType?> = infoType

  override fun completedCrossSellTriggeringSelfServiceSuccessfully(type: CrossSellInfoType) {
    infoType.value = type
  }

  override fun showedCrossSellSheet(type: CrossSellInfoType?) {
    infoType.value = null
  }
}
