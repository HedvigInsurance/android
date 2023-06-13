package com.hedvig.app.feature.profile.ui.eurobonus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import octopus.EurobonusDataQuery
import octopus.UpdateEurobonusNumberMutation
import kotlin.time.Duration.Companion.seconds

internal class EurobonusViewModel(
  private val apolloClient: ApolloClient,
) : ViewModel() {

  var eurobonusText: String by mutableStateOf("")
    private set

  init {
    viewModelScope.launch {
      apolloClient.query(EurobonusDataQuery())
        .safeExecute()
        .toEither()
        .onRight { data ->
          data.currentMember.partnerData?.sas?.eurobonusNumber?.let { existingEurobonusNumber ->
            Snapshot.withMutableSnapshot {
              eurobonusText = existingEurobonusNumber
            }
          }
        }
      isLoadingInitialEurobonusValue.update { false }
      snapshotFlow { eurobonusText }.drop(1).collectLatest {
        hasError.update { false }
        isDirty.update { true }
      }
    }
  }

  private val isLoadingInitialEurobonusValue = MutableStateFlow(true)
  private val isDirty = MutableStateFlow(false)
  private val isSubmitting = MutableStateFlow(false)
  private val hasError = MutableStateFlow(false)

  val uiState: StateFlow<EurobonusUiState> =
    combine(
      snapshotFlow { eurobonusText },
      isDirty,
      isSubmitting,
      hasError,
      isLoadingInitialEurobonusValue,
    ) { eurobonusText, isDirty, isSubmitting, hasError, isLoadingInitialEurobonusValue ->
      val canSubmit =
        eurobonusText.isNotBlank() && isDirty && !isSubmitting && !hasError && !isLoadingInitialEurobonusValue
      val isLoading = isSubmitting || isLoadingInitialEurobonusValue
      val canEditText = !isLoading
      EurobonusUiState(
        canSubmit = canSubmit,
        isLoading = isLoading,
        canEditText = canEditText,
        hasError = hasError,
      )
    }
      .stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5.seconds),
        EurobonusUiState(
          canSubmit = false,
          isLoading = false,
          canEditText = false,
          hasError = false,
        ),
      )

  fun updateEurobonusValue(newEurobonusValue: String) {
    if (isSubmitting.value) {
      return
    }
    Snapshot.withMutableSnapshot {
      eurobonusText = newEurobonusValue
    }
  }

  fun submitEurobonus(newEurobonusValue: String) {
    if (isSubmitting.value || hasError.value || newEurobonusValue.isBlank()) {
      return
    }
    isSubmitting.update { true }
    viewModelScope.launch {
      apolloClient.mutation(UpdateEurobonusNumberMutation(newEurobonusValue))
        .safeExecute()
        .toEither()
        .fold(
          ifLeft = {
            hasError.update { true }
          },
          ifRight = {
            isDirty.update { false }
          },
        )
      isSubmitting.update { false }
    }
  }
}

data class EurobonusUiState(
  val canSubmit: Boolean,
  val isLoading: Boolean,
  val canEditText: Boolean,
  val hasError: Boolean?,
)
