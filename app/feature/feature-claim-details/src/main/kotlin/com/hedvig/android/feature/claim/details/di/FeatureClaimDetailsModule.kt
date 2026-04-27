package com.hedvig.android.feature.claim.details.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.fileupload.ClaimsServiceUploadFileUseCase
import com.hedvig.android.core.fileupload.DownloadPdfUseCase
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.feature.claim.details.data.GetClaimDetailUiStateUseCase
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val claimDetailsModule = module {
  single<GetClaimDetailUiStateUseCase> {
    GetClaimDetailUiStateUseCase(get<ApolloClient>(), get<CrossSellAfterClaimClosedRepository>())
  }
  viewModel<AddFilesViewModel> { (targetUploadUrl: String, initialFilesUri: List<String>) ->
    AddFilesViewModel(
      claimsServiceUploadFileUseCase = get(),
      fileService = get(),
      targetUploadUrl = targetUploadUrl,
      cacheManager = get(),
      initialFilesUri = initialFilesUri,
    )
  }
  viewModel<ClaimDetailsViewModel> { (claimId: String, isPartnerClaim: Boolean) ->
    ClaimDetailsViewModel(
      claimId = claimId,
      isPartnerClaim = isPartnerClaim,
      getClaimDetailUiStateUseCase = get<GetClaimDetailUiStateUseCase>(),
      claimsServiceUploadFileUseCase = get<ClaimsServiceUploadFileUseCase>(),
      downloadPdfUseCase = get<DownloadPdfUseCase>(),
    )
  }
}
