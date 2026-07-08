package com.hedvig.android.feature.cross.sell.sheet

import arrow.core.Either
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.crosssells.CrossSellSheetData
import com.hedvig.android.crosssells.RecommendedAddon
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
    buttonTitle = "See offer",
    description = "For a safer trip abroad",
    deepLink = "https://hedvig.com/addon",
    banner = "Add extra safety when traveling",
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
    )

    presenter.test(CrossSellSheetState.Loading) {
      assertThat(awaitItem()).isEqualTo(CrossSellSheetState.Loading)
      assertThat(awaitItem()).isEqualTo(
        CrossSellSheetState.Content(addonData, CrossSellInfoType.EditCoInsured),
      )
    }
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
