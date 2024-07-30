package com.hedvig.android.feature.deleteaccount.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.auth.MemberIdService
import com.hedvig.android.feature.chat.DeleteAccountViewModel
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountRequestStorage
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountStateUseCase
import com.hedvig.android.feature.deleteaccount.data.RequestAccountDeletionUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val deleteAccountModule = module {
  single<DeleteAccountRequestStorage> {
    DeleteAccountRequestStorage(
      dataStore = get<DataStore<Preferences>>(),
      memberIdService = get<MemberIdService>(),
    )
  }
  single<RequestAccountDeletionUseCase> {
    RequestAccountDeletionUseCase(
      apolloClient = get<ApolloClient>(),
      memberIdService = get<MemberIdService>(),
      deleteAccountRequestStorage = get<DeleteAccountRequestStorage>(),
    )
  }
  single<DeleteAccountStateUseCase> {
    DeleteAccountStateUseCase(
      apolloClient = get<ApolloClient>(),
      deleteAccountRequestStorage = get<DeleteAccountRequestStorage>(),
    )
  }
  viewModel<DeleteAccountViewModel> {
    DeleteAccountViewModel(
      requestAccountDeletionUseCase = get<RequestAccountDeletionUseCase>(),
      deleteAccountStateUseCase = get<DeleteAccountStateUseCase>(),
    )
  }
}
