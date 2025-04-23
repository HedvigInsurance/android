package com.hedvig.android.feature.insurance.certificate.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.feature.insurance.certificate.data.GenerateInsuranceEvidenceUseCase
import com.hedvig.android.feature.insurance.certificate.data.GenerateInsuranceEvidenceUseCaseImpl
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceInitialDataUseCase
import com.hedvig.android.feature.insurance.certificate.data.GetInsuranceEvidenceInitialDataUseCaseImpl
import com.hedvig.android.feature.insurance.certificate.ui.email.InsuranceEvidenceEmailInputViewModel
import com.hedvig.android.feature.insurance.certificate.ui.overview.InsuranceEvidenceOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val insuranceEvidenceModule = module {
  single<GenerateInsuranceEvidenceUseCase> { GenerateInsuranceEvidenceUseCaseImpl(get<ApolloClient>()) }
  single<GetInsuranceEvidenceInitialDataUseCase> {
    GetInsuranceEvidenceInitialDataUseCaseImpl(get<ApolloClient>())
  }
  viewModel<InsuranceEvidenceEmailInputViewModel> {
    InsuranceEvidenceEmailInputViewModel(
      generateInsuranceEvidenceUseCase = get<GenerateInsuranceEvidenceUseCase>(),
      getEmailUseCase = get<GetInsuranceEvidenceInitialDataUseCase>(),
    )
  }
  viewModel<InsuranceEvidenceOverviewViewModel> {
    InsuranceEvidenceOverviewViewModel(
      get<DownloadPdfUseCase>(),
    )
  }
}
