package com.hedvig.android.feature.profile.eurobonus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import octopus.EurobonusDataQuery
import octopus.UpdateEurobonusNumberMutation

internal class EurobonusViewModel(apolloClient: ApolloClient) : MoleculeViewModel<EurobonusEvent, EurobonusUiState>(
  initialState = EurobonusUiState(
    canSubmit = false,
    isLoading = false,
    canEditText = false,
    hasError = false,
  ),
  presenter = EurobonusPresenter(apolloClient),
)

internal class EurobonusPresenter(private val apolloClient: ApolloClient) :
  MoleculePresenter<EurobonusEvent, EurobonusUiState> {
  @Composable
  override fun MoleculePresenterScope<EurobonusEvent>.present(lastState: EurobonusUiState): EurobonusUiState {
    var eurobonusNumberFromBackend by remember {
      mutableStateOf<String?>(null)
    }

    var eurobonusNumberToShow by remember {
      mutableStateOf("")
    }

    var isLoadingInitialEurobonusValue by remember {
      mutableStateOf(true)
    }

    var isSubmitting by remember {
      mutableStateOf(false)
    }

    var hasError by remember {
      mutableStateOf(false)
    }

    var isEligibleForEurobonus by remember {
      mutableStateOf(true)
    }

    var submittingIteration by remember {
      mutableIntStateOf(0)
    }

    CollectEvents { event ->
      when (event) {
        is EurobonusEvent.SubmitEditedEurobonus -> {
          if (!isSubmitting && eurobonusNumberToShow.isNotBlank()) {
            submittingIteration++
          }
        }

        is EurobonusEvent.UpdateEurobonusValue -> {
          if (!isSubmitting) {
            hasError = false
            eurobonusNumberToShow = event.newEurobonusValue
          }
        }
      }
    }

    LaunchedEffect(Unit) {
      apolloClient.query(EurobonusDataQuery())
        .fetchPolicy(FetchPolicy.NetworkOnly)
        .safeExecute()
        .toEither()
        .onRight { data ->
          data.currentMember.partnerData?.sas?.eligible?.let { eligible ->
            if (!eligible) {
              isEligibleForEurobonus = false
            }
          }
          data.currentMember.partnerData?.sas?.eurobonusNumber?.let { existingEurobonusNumber ->
            eurobonusNumberFromBackend = existingEurobonusNumber
            eurobonusNumberToShow = existingEurobonusNumber
          }
          hasError = false
        }
        .onLeft {
          hasError = true
        }
      isLoadingInitialEurobonusValue = false
    }

    LaunchedEffect(submittingIteration) {
      if (submittingIteration > 0) {
        isSubmitting = true
        val valueToSubmit = eurobonusNumberToShow
        apolloClient.mutation(UpdateEurobonusNumberMutation(valueToSubmit))
          .safeExecute()
          .toEither()
          .fold(
            ifLeft = {
              hasError = true
            },
            ifRight = {
              it.memberUpdateEurobonusNumber.member?.partnerData?.sas?.eurobonusNumber?.let { existingEurobonusNumber ->
                eurobonusNumberFromBackend = existingEurobonusNumber
              }
            },
          )
        isSubmitting = false
      }
    }

    val differentFromOriginal = eurobonusNumberToShow != eurobonusNumberFromBackend
    val isLoadingInProcess = isSubmitting || isLoadingInitialEurobonusValue
    val canSubmit =
      eurobonusNumberToShow.isNotBlank() && differentFromOriginal && !isLoadingInProcess

    return EurobonusUiState(
      canSubmit = canSubmit,
      isLoading = isLoadingInitialEurobonusValue,
      isSubmitting = isSubmitting,
      canEditText = !isLoadingInProcess,
      hasError = hasError,
      eurobonusNumber = eurobonusNumberToShow,
      isEligibleForEurobonus = isEligibleForEurobonus,
    )
  }
}

internal data class EurobonusUiState(
  val canSubmit: Boolean,
  val isLoading: Boolean,
  val canEditText: Boolean,
  val hasError: Boolean?,
  val eurobonusNumber: String = "",
  val isEligibleForEurobonus: Boolean? = null,
  val isSubmitting: Boolean = false,
)

internal sealed interface EurobonusEvent {
  data object SubmitEditedEurobonus : EurobonusEvent

  data class UpdateEurobonusValue(val newEurobonusValue: String) : EurobonusEvent
}
