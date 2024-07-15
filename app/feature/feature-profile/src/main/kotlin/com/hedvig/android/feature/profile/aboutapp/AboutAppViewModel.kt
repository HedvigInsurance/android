package com.hedvig.android.feature.profile.aboutapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import octopus.MemberIdQuery

internal class AboutAppViewModel(
  apolloClient: ApolloClient,
) : ViewModel() {
  val uiState: StateFlow<AboutAppUiState> = flow {
    val memberId = apolloClient
      .query(MemberIdQuery())
      .safeExecute()
      .toEither(::ErrorMessage)
      .getOrNull()
      ?.currentMember
      ?.id
    emit(AboutAppUiState(memberId))
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5.seconds),
    AboutAppUiState(null),
  )
}

internal data class AboutAppUiState(
  val memberId: String?,
)
