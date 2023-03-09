package com.hedvig.app.feature.insurance.ui.detail.coverage.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.app.feature.insurance.ui.detail.coverage.CoverageViewModel
import com.hedvig.app.feature.insurance.ui.detail.coverage.GetContractCoverageUseCase
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val insuranceCoverageModule = module {
  viewModel<CoverageViewModel> { (contactId: String) ->
    CoverageViewModel(contactId, get())
  }
  single<GetContractCoverageUseCase> {
    GetContractCoverageUseCase(get<ApolloClient>(giraffeClient), get())
  }
}
