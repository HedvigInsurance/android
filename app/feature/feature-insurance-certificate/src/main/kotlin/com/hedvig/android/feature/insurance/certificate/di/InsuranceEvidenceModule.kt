package com.hedvig.android.feature.insurance.certificate.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceUseCaseImpl
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val insuranceEvidenceModule = module {
  single<GetInsuranceEvidenceUseCase> { GetInsuranceEvidenceUseCaseImpl(get<ApolloClient>()) }

  viewModel<InsuranceEvidenceEmailInputViewModel> {
    InsuranceEvidenceEmailInputViewModel(
      getInsuranceEvidenceUseCase = get<GetInsuranceEvidenceUseCase>(),
    )
  }

  viewModel<InsuranceEvidenceOverviewViewModel> {
    InsuranceEvidenceOverviewViewModel()
  }
}
