package com.hedvig.android.feature.change.tier.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.changetier.data.ChangeTierDeductibleIntent
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCase
import com.hedvig.android.feature.change.tier.data.GetCurrentContractDataUseCaseImpl
import com.hedvig.android.feature.change.tier.ui.stepcustomize.SelectCoverageViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val chooseTierModule = module {
  viewModel<SelectCoverageViewModel> { params ->
    SelectCoverageViewModel(
      getCurrentContractDataUseCase = get<GetCurrentContractDataUseCase>(),
      insuranceId = params.get<String>(),
      intent = params.get<ChangeTierDeductibleIntent>(),
    )
  }

  single<GetCurrentContractDataUseCase> {
    GetCurrentContractDataUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
}
