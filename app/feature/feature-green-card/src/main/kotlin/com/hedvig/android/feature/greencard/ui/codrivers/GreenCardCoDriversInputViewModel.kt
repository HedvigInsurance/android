package com.hedvig.android.feature.greencard.ui.codrivers

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.feature.greencard.data.CreateGreenCardUseCase
import com.hedvig.android.feature.greencard.data.GreenCardCoDriver
import com.hedvig.android.feature.greencard.navigation.GreenCardCoDriversInputKey
import com.hedvig.android.feature.greencard.navigation.GreenCardCreatedKey
import com.hedvig.android.feature.greencard.navigation.GreenCardKey
import com.hedvig.android.feature.greencard.ui.common.AddressFieldsState
import com.hedvig.android.feature.greencard.ui.common.trimmedText
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.navigateAndPopUpTo
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject

@AssistedInject
@HedvigViewModel(ActivityRetainedScope::class)
internal class GreenCardCoDriversInputViewModel(
  @Assisted input: GreenCardCoDriversInputKey,
  createGreenCardUseCase: CreateGreenCardUseCase,
  backstack: Backstack,
) : MoleculeViewModel<GreenCardCoDriversInputEvent, GreenCardCoDriversInputUiState>(
    initialState = GreenCardCoDriversInputUiState(
      coDrivers = emptyList(),
      showMissingFieldErrors = false,
      isCreating = false,
      showCreateError = false,
    ),
    presenter = GreenCardCoDriversInputPresenter(input, createGreenCardUseCase, backstack),
  )

internal class GreenCardCoDriversInputPresenter(
  private val input: GreenCardCoDriversInputKey,
  private val createGreenCardUseCase: CreateGreenCardUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<GreenCardCoDriversInputEvent, GreenCardCoDriversInputUiState> {
  @Composable
  override fun MoleculePresenterScope<GreenCardCoDriversInputEvent>.present(
    lastState: GreenCardCoDriversInputUiState,
  ): GreenCardCoDriversInputUiState {
    var coDrivers by remember { mutableStateOf(lastState.coDrivers) }
    var showMissingFieldErrors by remember { mutableStateOf(lastState.showMissingFieldErrors) }
    var createRequest by remember { mutableStateOf<List<GreenCardCoDriver>?>(null) }
    var showCreateError by remember { mutableStateOf(false) }

    CollectEvents { event ->
      when (event) {
        GreenCardCoDriversInputEvent.AddCoDriver -> {
          coDrivers = coDrivers + CoDriverFieldsState()
        }

        is GreenCardCoDriversInputEvent.RemoveCoDriver -> {
          coDrivers = coDrivers - event.coDriver
        }

        GreenCardCoDriversInputEvent.DismissCreateError -> {
          showCreateError = false
        }

        GreenCardCoDriversInputEvent.Submit -> {
          if (createRequest != null) return@CollectEvents
          showMissingFieldErrors = coDrivers.any { !it.isComplete() }
          if (!showMissingFieldErrors) {
            createRequest = coDrivers.map(CoDriverFieldsState::toCoDriver)
          }
        }
      }
    }

    LaunchedEffect(createRequest) {
      val request = createRequest ?: return@LaunchedEffect
      createGreenCardUseCase.invoke(
        contractId = input.tripInput.contractId,
        travelStartDate = input.tripInput.travelStartDate,
        travelEndDate = input.tripInput.travelEndDate,
        mailingAddress = input.mailingAddress,
        coDrivers = request,
      ).fold(
        ifLeft = {
          logcat(LogPriority.ERROR) { "Creating green card failed: $it" }
          showCreateError = true
        },
        ifRight = { url ->
          backstack.navigateAndPopUpTo<GreenCardKey>(GreenCardCreatedKey(url), inclusive = false)
        },
      )
      createRequest = null
    }

    return GreenCardCoDriversInputUiState(
      coDrivers = coDrivers,
      showMissingFieldErrors = showMissingFieldErrors,
      isCreating = createRequest != null,
      showCreateError = showCreateError,
    )
  }
}

/**
 * The fields edit these states in place, which keeps the cursor where the member put it.
 */
internal class CoDriverFieldsState(
  val name: TextFieldState = TextFieldState(),
  val address: AddressFieldsState = AddressFieldsState(),
) {
  fun isComplete(): Boolean = name.text.isNotBlank() && address.isComplete()

  fun toCoDriver(): GreenCardCoDriver = GreenCardCoDriver(name.trimmedText(), address.toAddress())
}

internal sealed interface GreenCardCoDriversInputEvent {
  data object AddCoDriver : GreenCardCoDriversInputEvent

  data class RemoveCoDriver(val coDriver: CoDriverFieldsState) : GreenCardCoDriversInputEvent

  data object DismissCreateError : GreenCardCoDriversInputEvent

  data object Submit : GreenCardCoDriversInputEvent
}

internal data class GreenCardCoDriversInputUiState(
  val coDrivers: List<CoDriverFieldsState>,
  val showMissingFieldErrors: Boolean,
  val isCreating: Boolean,
  val showCreateError: Boolean,
)
