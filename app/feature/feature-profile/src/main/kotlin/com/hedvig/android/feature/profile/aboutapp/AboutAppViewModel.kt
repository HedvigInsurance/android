package com.hedvig.android.feature.profile.aboutapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.ErrorMessage
import com.hedvig.android.apollo.safeFlow
import com.hedvig.android.core.datastore.DeviceIdDataStore
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import octopus.MemberIdQuery

internal class AboutAppViewModel(
  apolloClient: ApolloClient,
  deviceIdDataStore: DeviceIdDataStore,
) : ViewModel() {
  val uiState: StateFlow<AboutAppUiState> = combine(
    apolloClient
      .query(MemberIdQuery())
      .safeFlow(::ErrorMessage)
      .map { it.getOrNull()?.currentMember?.id },
    deviceIdDataStore.observeDeviceId(),
  ) { memberId, deviceId ->
    AboutAppUiState(memberId, deviceId)
  }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      AboutAppUiState(null, null),
    )
}

internal data class AboutAppUiState(
  val memberId: String?,
  val deviceId: String?,
)
