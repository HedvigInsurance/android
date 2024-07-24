package com.hedvig.android.feature.changeaddress.destination.offer

import arrow.core.right
import assertk.assertions.isEqualTo
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.destination.FakeChangeAddressRepository
import com.hedvig.android.feature.changeaddress.destination.fakeMoveQuote
import com.hedvig.android.feature.changeaddress.destination.fakeMovingParametersForOfferFromVilla
import com.hedvig.android.logger.TestLogcatLoggingRule
import com.hedvig.android.molecule.test.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ChangeAddressOfferPresenterTest {
  @get:Rule
  val testLogcatLogger = TestLogcatLoggingRule()

  @Test
  fun `when one of the quotes is clicked it is expanded`() = runTest {
    val repository = FakeChangeAddressRepository()
    val presenter = ChangeAddressOfferPresenter(
      fakeMovingParametersForOfferFromVilla,
      repository,
    )
    presenter.test(
      ChangeAddressOfferUiState(
        movingDate = fakeMovingParametersForOfferFromVilla.newAddressParameters.movingDate,
        moveIntentId = MoveIntentId(fakeMovingParametersForOfferFromVilla.selectHousingTypeParameters.moveIntentId),
      ),
    ) {
      skipItems(1)
      repository.createQuotesResponseTurbine.add(listOf(fakeMoveQuote).right())
      val isThereQuotesExpanded = awaitItem().quotes.map { it.isExpanded }.contains(true)
      assertk.assertThat(isThereQuotesExpanded).isEqualTo(false)
      sendEvent(ChangeAddressOfferEvent.ExpandQuote(fakeMoveQuote))
      skipItems(1)
      val isThereQuotesExpanded2 = awaitItem().quotes.map { it.isExpanded }.contains(true)
      assertk.assertThat(isThereQuotesExpanded2).isEqualTo(true)
    }
  }
}
