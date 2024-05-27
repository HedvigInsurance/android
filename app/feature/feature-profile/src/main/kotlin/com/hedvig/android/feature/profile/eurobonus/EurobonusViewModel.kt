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
  initialState = EurobonusUiState(false, false, false, false),
  presenter = EurobonusPresenter(apolloClient),
)

internal class EurobonusPresenter(private val apolloClient: ApolloClient) :
  MoleculePresenter<EurobonusEvent, EurobonusUiState> {
  @Composable
  override fun MoleculePresenterScope<EurobonusEvent>.present(lastState: EurobonusUiState): EurobonusUiState {
    var eurobonusTextFromBackend by remember {
      mutableStateOf<String?>(null)
    }

    var eurobonusTextToShow by remember {
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
        is EurobonusEvent.SubmitEurobonus -> {
          if (!isSubmitting && eurobonusTextToShow.isNotBlank()) {
            submittingIteration++
          }
        }

        is EurobonusEvent.UpdateEurobonusValue -> {
          if (!isSubmitting) {
            hasError = false
            eurobonusTextToShow = event.newEurobonusValue
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
            eurobonusTextFromBackend = existingEurobonusNumber
            eurobonusTextToShow = existingEurobonusNumber
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
        val valueToSubmit = eurobonusTextToShow
        apolloClient.mutation(UpdateEurobonusNumberMutation(valueToSubmit))
          .safeExecute()
          .toEither()
          .fold(
            ifLeft = {
              hasError = true
            },
            ifRight = {
              it.memberUpdateEurobonusNumber.member?.partnerData?.sas?.eurobonusNumber?.let { existingEurobonusNumber ->
                eurobonusTextFromBackend = existingEurobonusNumber
              }
            },
          )
        isSubmitting = false
      }
    }

    val differentFromOriginal = eurobonusTextToShow != eurobonusTextFromBackend
    val canSubmit =
      eurobonusTextToShow.isNotBlank() && differentFromOriginal && !isSubmitting && !isLoadingInitialEurobonusValue
    val isLoading = isSubmitting || isLoadingInitialEurobonusValue
    val canEditText = !isLoading

    return EurobonusUiState(
      canSubmit = canSubmit,
      isLoading = isLoading,
      canEditText = canEditText,
      hasError = hasError,
      eurobonusText = eurobonusTextToShow,
      isEligibleForEurobonus = isEligibleForEurobonus,
    )
  }
}

internal data class EurobonusUiState(
  val canSubmit: Boolean,
  val isLoading: Boolean,
  val canEditText: Boolean,
  val hasError: Boolean?,
  val eurobonusText: String = "", // todo: think here
  val isEligibleForEurobonus: Boolean? = null, // todo: think here
)

internal sealed interface EurobonusEvent {
  data object SubmitEurobonus : EurobonusEvent

  data class UpdateEurobonusValue(val newEurobonusValue: String) : EurobonusEvent
}
