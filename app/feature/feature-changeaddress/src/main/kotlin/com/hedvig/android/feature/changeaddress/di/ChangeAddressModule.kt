package com.hedvig.android.feature.changeaddress.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.appreview.SelfServiceCompletedEventManager
import com.hedvig.android.feature.changeaddress.ChangeAddressViewModel
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.NetworkChangeAddressRepository
import com.hedvig.android.language.LanguageService
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val changeAddressModule = module {
  single<ChangeAddressRepository> {
    NetworkChangeAddressRepository(
      get<ApolloClient>(),
      get<SelfServiceCompletedEventManager>(),
    )
  }
  viewModel<ChangeAddressViewModel> {
    ChangeAddressViewModel(
      changeAddressRepository = get<ChangeAddressRepository>(),
      languageService = get<LanguageService>(),
    )
  }
}
