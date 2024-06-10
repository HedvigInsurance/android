package com.hedvig.android.feature.changeaddress.destination.offer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.SuccessfulMove
import com.hedvig.android.feature.changeaddress.destination.createQuoteInput
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferEvent.ConfirmMove
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferEvent.DismissErrorDialog
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferEvent.ExpandQuote
import com.hedvig.android.feature.changeaddress.destination.offer.ChangeAddressOfferEvent.Retry
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.datetime.LocalDate

internal class ChangeAddressOfferViewModel(
  previousParameters: MovingParameters,
  changeAddressRepository: ChangeAddressRepository,
) : MoleculeViewModel<ChangeAddressOfferEvent, ChangeAddressOfferUiState>(
    initialState = ChangeAddressOfferUiState(
      movingDate = previousParameters.newAddressParameters.movingDate,
      moveIntentId = MoveIntentId(previousParameters.selectHousingTypeParameters.moveIntentId),
    ),
    presenter = ChangeAddressOfferPresenter(
      previousParameters = previousParameters,
      changeAddressRepository = changeAddressRepository,
    ),
  )

internal class ChangeAddressOfferPresenter(
  private val previousParameters: MovingParameters,
  private val changeAddressRepository: ChangeAddressRepository,
) : MoleculePresenter<ChangeAddressOfferEvent, ChangeAddressOfferUiState> {
  @Composable
  override fun MoleculePresenterScope<ChangeAddressOfferEvent>.present(
    lastState: ChangeAddressOfferUiState,
  ): ChangeAddressOfferUiState {
    var currentState by remember { mutableStateOf(lastState) }

    var dataLoadIteration by remember { mutableIntStateOf(0) }

    var quotesLoadIteration by remember { mutableIntStateOf(0) }

    CollectEvents { event ->
      when (event) {
        is ConfirmMove -> {
          dataLoadIteration++
        }
        DismissErrorDialog -> currentState = currentState.copy(errorMessage = null)
        is ExpandQuote -> {
          currentState = currentState.copy(
            currentState.quotes.map { quote: MoveQuote ->
              if (quote == event.moveQuote) {
                event.moveQuote.copy(isExpanded = !event.moveQuote.isExpanded)
              } else {
                quote
              }
            },
          )
        }

        Retry -> quotesLoadIteration++
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (dataLoadIteration > 0) {
        currentState = currentState.copy(isLoading = true)
        changeAddressRepository.commitMove(
          MoveIntentId(previousParameters.selectHousingTypeParameters.moveIntentId),
        ).fold(
          ifLeft = { error ->
            currentState = currentState.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          },
          ifRight = { result ->
            currentState = currentState.copy(
              isLoading = false,
              successfulMoveResult = result,
            )
          },
        )
      }
    }

    LaunchedEffect(quotesLoadIteration) {
      val input = createQuoteInput(
        housingType = previousParameters.selectHousingTypeParameters.housingType,
        isStudent = previousParameters.newAddressParameters.isStudent,
        moveIntentId = previousParameters.selectHousingTypeParameters.moveIntentId,
        street = previousParameters.newAddressParameters.street,
        postalCode = previousParameters.newAddressParameters.postalCode,
        moveFromAddressId = previousParameters.selectHousingTypeParameters.moveFromAddressId,
        movingDate = previousParameters.newAddressParameters.movingDate,
        numberInsured = previousParameters.newAddressParameters.numberInsured,
        squareMeters = previousParameters.newAddressParameters.squareMeters,
        yearOfConstruction = previousParameters.villaOnlyParameters?.yearOfConstruction,
        ancillaryArea = previousParameters.villaOnlyParameters?.ancillaryArea,
        numberOfBathrooms = previousParameters.villaOnlyParameters?.numberOfBathrooms,
        extraBuildings = previousParameters.villaOnlyParameters?.extraBuildings ?: listOf(),
        isSublet = previousParameters.villaOnlyParameters?.isSublet ?: false,
      )
      currentState = currentState.copy(error = false, isLoading = true)
      changeAddressRepository.createQuotes(input).fold(
        ifLeft = { error ->
          currentState = currentState.copy(
            isLoading = false,
            error = true,
          )
        },
        ifRight = { quotes ->
          currentState = currentState.copy(
            isLoading = false,
            quotes = quotes,
          )
        },
      )
    }
    return currentState
  }
}

internal data class ChangeAddressOfferUiState(
  val quotes: List<MoveQuote> = emptyList(),
  val successfulMoveResult: SuccessfulMove? = null,
  val errorMessage: String? = null,
  val error: Boolean = false,
  val movingDate: LocalDate,
  val moveIntentId: MoveIntentId,
  val isLoading: Boolean = false,
)

internal sealed interface ChangeAddressOfferEvent {
  data class ConfirmMove(val id: MoveIntentId) : ChangeAddressOfferEvent

  data class ExpandQuote(val moveQuote: MoveQuote) : ChangeAddressOfferEvent

  data object DismissErrorDialog : ChangeAddressOfferEvent

  data object Retry : ChangeAddressOfferEvent
}
