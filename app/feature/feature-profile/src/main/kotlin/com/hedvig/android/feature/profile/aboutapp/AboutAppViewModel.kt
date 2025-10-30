package com.hedvig.android.feature.profile.aboutapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.datastore.DeviceIdDataStore
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import octopus.MemberIdQuery

internal class AboutAppViewModel(
  apolloClient: ApolloClient,
  deviceIdDataStore: DeviceIdDataStore,
) : MoleculeViewModel<Unit, AboutAppUiState>(
    initialState = AboutAppUiState.Loading,
    presenter = AboutAppPresenter(apolloClient, deviceIdDataStore),
  )

private class AboutAppPresenter(
  private val apolloClient: ApolloClient,
  private val deviceIdDataStore: DeviceIdDataStore,
) : MoleculePresenter<Unit, AboutAppUiState> {
  @Composable
  override fun MoleculePresenterScope<Unit>.present(lastState: AboutAppUiState): AboutAppUiState {
    var currentState by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }

    LaunchedEffect(loadIteration) {
      currentState = AboutAppUiState.Loading
      combine(
        apolloClient
          .query(MemberIdQuery())
          .safeFlow(::ErrorMessage)
          .map { it.getOrNull()?.currentMember?.id },
        deviceIdDataStore.observeDeviceId(),
      ) { memberId, deviceId ->
        AboutAppUiState.Content(memberId, deviceId)
      }.collectLatest { state ->
        currentState = state
      }
    }

    return currentState
  }
}

internal sealed interface AboutAppUiState {
  data object Loading : AboutAppUiState

  data class Content(
    val memberId: String?,
    val deviceId: String?,
  ) : AboutAppUiState
}
