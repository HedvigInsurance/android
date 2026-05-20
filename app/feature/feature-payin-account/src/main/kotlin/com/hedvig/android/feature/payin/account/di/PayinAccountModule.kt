package com.hedvig.android.feature.payin.account.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.feature.payin.account.data.GetPayinAccountUseCase
import com.hedvig.android.feature.payin.account.data.SetupInvoicePayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCase
import com.hedvig.android.feature.payin.account.data.SetupSwishPayinUseCaseImpl
import com.hedvig.android.feature.payin.account.ui.overview.PayinAccountOverviewViewModel
import com.hedvig.android.feature.payin.account.ui.setupinvoice.SetupInvoicePayinViewModel
import com.hedvig.android.feature.payin.account.ui.setupswish.SetupSwishPayinViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val payinAccountModule = module {
  single<SetupSwishPayinUseCase> {
    SetupSwishPayinUseCaseImpl(get<ApolloClient>(), get<NetworkCacheManager>())
  }
  single<GetPayinAccountUseCase> {GetPayinAccountUseCase(get<ApolloClient>())}
  single<SetupInvoicePayinUseCase> { SetupInvoicePayinUseCase(get<ApolloClient>(), get<NetworkCacheManager>()) }
  viewModel<SetupInvoicePayinViewModel> { SetupInvoicePayinViewModel(get<SetupInvoicePayinUseCase>()) }
  viewModel<SetupSwishPayinViewModel> { SetupSwishPayinViewModel(get<SetupSwishPayinUseCase>()) }
  viewModel<PayinAccountOverviewViewModel> { PayinAccountOverviewViewModel(get<GetPayinAccountUseCase>()) }
}
