package com.hedvig.android.feature.payoutaccount.ui.setupswish

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.core.common.di.ActivityRetainedScope
import com.hedvig.android.core.common.di.HedvigViewModel
import com.hedvig.android.core.common.validation.PhoneNumberRules
import com.hedvig.android.data.paying.member.GetMemberPhoneNumberUseCase
import com.hedvig.android.feature.payoutaccount.data.SetupSwishPayoutUseCase
import com.hedvig.android.feature.payoutaccount.navigation.SelectPayoutMethodKey
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import com.hedvig.android.navigation.compose.Backstack
import com.hedvig.android.navigation.compose.popUpTo
import dev.zacsweers.metro.Inject

@Inject
@HedvigViewModel(ActivityRetainedScope::class)
internal class SetupSwishPayoutViewModel(
  setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
  getMemberPhoneNumberUseCase: GetMemberPhoneNumberUseCase,
  backstack: Backstack,
) : MoleculeViewModel<SetupSwishPayoutEvent, SetupSwishPayoutUiState>(
    SetupSwishPayoutUiState(TextFieldState(), false, null, false),
    SetupSwishPayoutPresenter(setupSwishPayoutUseCase, getMemberPhoneNumberUseCase, backstack),
  )

internal sealed interface SetupSwishPayoutEvent {
  data object Save : SetupSwishPayoutEvent

  data object FinishSetup : SetupSwishPayoutEvent
}

internal data class SetupSwishPayoutUiState(
  val phoneNumberState: TextFieldState,
  val isLoading: Boolean,
  val errorMessage: ErrorMessage?,
  val isConnected: Boolean,
)

internal class SetupSwishPayoutPresenter(
  private val setupSwishPayoutUseCase: SetupSwishPayoutUseCase,
  private val getMemberPhoneNumberUseCase: GetMemberPhoneNumberUseCase,
  private val backstack: Backstack,
) : MoleculePresenter<SetupSwishPayoutEvent, SetupSwishPayoutUiState> {
  @Composable
  override fun MoleculePresenterScope<SetupSwishPayoutEvent>.present(
    lastState: SetupSwishPayoutUiState,
  ): SetupSwishPayoutUiState {
    val phoneNumberState = remember { lastState.phoneNumberState }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<ErrorMessage?>(null) }
    var isConnected by remember { mutableStateOf(false) }
    var saveIteration by remember { mutableStateOf<String?>(null) }

    // Seeds the field with the number the backend already holds, so the usual case is a confirm
    // rather than a re-type. Anything the member has typed themselves wins.
    LaunchedEffect(Unit) {
      if (phoneNumberState.text.isNotEmpty()) return@LaunchedEffect
      val storedNumber = getMemberPhoneNumberUseCase.invoke().getOrNull() ?: return@LaunchedEffect
      // Null for a number this field cannot hold. Notably one stored in international form: Swish
      // takes a Swedish mobile number without a country code, and turning "+46…" into "0…" is a
      // guess that produces a number nobody can call, so it is left for the member to type.
      val usableNumber = PhoneNumberRules.SwishPhoneNumber.cleanedForSubmission(storedNumber)
      if (usableNumber == null || !PhoneNumberRules.SwishPhoneNumber.isWellFormed(usableNumber)) {
        return@LaunchedEffect
      }
      if (phoneNumberState.text.isEmpty()) {
        phoneNumberState.setTextAndPlaceCursorAtEnd(usableNumber.toString())
      }
    }

    val currentSave = saveIteration
    if (currentSave != null) {
      LaunchedEffect(currentSave) {
        isLoading = true
        errorMessage = null
        setupSwishPayoutUseCase.invoke(currentSave).fold(
          ifLeft = {
            isLoading = false
            errorMessage = it
            saveIteration = null
          },
          ifRight = {
            isLoading = false
            isConnected = true
            saveIteration = null
          },
        )
      }
    }

    CollectEvents { event ->
      when (event) {
        SetupSwishPayoutEvent.Save -> {
          if (!isLoading) {
            saveIteration = phoneNumberState.text.toString()
          }
        }

        SetupSwishPayoutEvent.FinishSetup -> {
          backstack.popUpTo<SelectPayoutMethodKey>(inclusive = true)
        }
      }
    }

    return SetupSwishPayoutUiState(
      phoneNumberState = phoneNumberState,
      isLoading = isLoading,
      errorMessage = errorMessage,
      isConnected = isConnected,
    )
  }
}
