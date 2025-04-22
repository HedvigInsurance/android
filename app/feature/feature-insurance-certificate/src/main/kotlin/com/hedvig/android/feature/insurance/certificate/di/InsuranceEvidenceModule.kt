package com.hedvig.android.feature.insurance.certificate.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceUseCaseImpl
import org.koin.dsl.module

val insuranceEvidenceModule = module {
  single<GetInsuranceEvidenceUseCase> { GetInsuranceEvidenceUseCaseImpl(get<ApolloClient>()) }

//  viewModel<InsuranceEvidenceViewModel> {
//    InsuranceEvidenceViewModel(
//      get<GetInsuranceEvidenceUseCase>(),
//    )
//  }
}
