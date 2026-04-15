package com.hedvig.android.feature.payoutaccount.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCaseImpl
import com.hedvig.android.feature.payoutaccount.data.SetupNordeaPayoutUseCase
import com.hedvig.android.feature.payoutaccount.data.SetupNordeaPayoutUseCaseImpl
import com.hedvig.android.feature.payoutaccount.data.SetupSwishPayoutUseCase
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountViewModel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewViewModel
import com.hedvig.android.feature.payoutaccount.ui.setupswish.SetupSwishPayoutViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val payoutAccountModule = module {
  single<GetPayoutAccountUseCase> { GetPayoutAccountUseCaseImpl(get<ApolloClient>()) }
  single<SetupNordeaPayoutUseCase> {
    SetupNordeaPayoutUseCaseImpl(get<ApolloClient>(), get<NetworkCacheManager>())
  }
  single<SetupSwishPayoutUseCase> {
    SetupSwishPayoutUseCase(get<ApolloClient>(), get<NetworkCacheManager>())
  }
  viewModel<PayoutAccountOverviewViewModel> { PayoutAccountOverviewViewModel(get<GetPayoutAccountUseCase>()) }
  viewModel<EditBankAccountViewModel> { EditBankAccountViewModel(get<SetupNordeaPayoutUseCase>()) }
  viewModel<SetupSwishPayoutViewModel> { SetupSwishPayoutViewModel(get<SetupSwishPayoutUseCase>()) }
}
