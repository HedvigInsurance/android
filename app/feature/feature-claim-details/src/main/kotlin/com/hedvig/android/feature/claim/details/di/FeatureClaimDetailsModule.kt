package com.hedvig.android.feature.claim.details.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.core.fileupload.UploadFileUseCase
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.featureflags.FeatureManager
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimDetailsModule = module {
  single<GetClaimDetailUiStateUseCase> {
    GetClaimDetailUiStateUseCase(get<ApolloClient>(), get<CrossSellAfterClaimClosedRepository>(), get<FeatureManager>())
  }
  viewModel<AddFilesViewModel> { (targetUploadUrl: String, initialFilesUri: List<String>) ->
    AddFilesViewModel(
      uploadFileUseCase = get(),
      fileService = get(),
      targetUploadUrl = targetUploadUrl,
      cacheManager = get(),
      initialFilesUri = initialFilesUri,
    )
  }
  viewModel<ClaimDetailsViewModel> { (claimId: String) ->
    ClaimDetailsViewModel(
      claimId = claimId,
      getClaimDetailUiStateUseCase = get<GetClaimDetailUiStateUseCase>(),
      uploadFileUseCase = get<UploadFileUseCase>(),
      downloadPdfUseCase = get<DownloadPdfUseCase>(),
    )
  }
}
