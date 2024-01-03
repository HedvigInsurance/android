package com.hedvig.android.feature.claim.details.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val claimDetailsModule = module {
  single<GetClaimDetailUiStateUseCase> { GetClaimDetailUiStateUseCase(get<ApolloClient>()) }

  viewModel<ClaimDetailsViewModel> { (claimId: String) ->
    ClaimDetailsViewModel(claimId, get<GetClaimDetailUiStateUseCase>(), get<UploadFileUseCase>())
  }
}
