package com.hedvig.android.shared.tier.comparison.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.featureflags.FeatureManager
import com.hedvig.android.shared.tier.comparison.data.GetCoverageComparisonUseCase
import com.hedvig.android.shared.tier.comparison.data.GetCoverageComparisonUseCaseImpl
import com.hedvig.android.shared.tier.comparison.navigation.ComparisonParameters
import com.hedvig.android.shared.tier.comparison.ui.ComparisonViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val comparisonModule = module {
  viewModel<ComparisonViewModel> { params ->
    ComparisonViewModel(
      comparisonParameters = params.get<ComparisonParameters>(),
      getCoverageComparisonUseCase = get<GetCoverageComparisonUseCase>(),
    )
  }
  single<GetCoverageComparisonUseCase> {
    GetCoverageComparisonUseCaseImpl(
      apolloClient = get<ApolloClient>(),
      featureManager = get<FeatureManager>(),
    )
  }
}
