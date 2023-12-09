package com.hedvig.android.feature.profile.eurobonus

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import octopus.EurobonusDataQuery
import octopus.UpdateEurobonusNumberMutation

internal class EurobonusViewModel(
  private val apolloClient: ApolloClient,
) : ViewModel() {
  var eurobonusText: String by mutableStateOf("")
    private set

  private val isLoadingInitialEurobonusValue = MutableStateFlow(true)
  private val isDirty = MutableStateFlow(false)
  private val isSubmitting = MutableStateFlow(false)
  private val hasError = MutableStateFlow(false)

  // If member isn't eligible for eurobonus, should exit this screen immediatelly.
  val _isEligibleForEurobonus: MutableStateFlow<Boolean> = MutableStateFlow(true)
  val isEligibleForEurobonus: StateFlow<Boolean> = _isEligibleForEurobonus.asStateFlow()

  val uiState: StateFlow<EurobonusUiState> = merge(
    flow {
      apolloClient.query(EurobonusDataQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither()
        .onRight { data ->
          data.currentMember.partnerData?.sas?.eligible?.let { eligible ->
            if (!eligible) {
              _isEligibleForEurobonus.update { false }
            }
          }
          data.currentMember.partnerData?.sas?.eurobonusNumber?.let { existingEurobonusNumber ->
            eurobonusText = existingEurobonusNumber
          }
        }
      isLoadingInitialEurobonusValue.update { false }
      snapshotFlow { eurobonusText }
        .drop(1)
        .collectLatest {
          hasError.update { false }
          isDirty.update { true }
        }
    },
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
    },
  ).stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(),
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
    eurobonusText = newEurobonusValue
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
