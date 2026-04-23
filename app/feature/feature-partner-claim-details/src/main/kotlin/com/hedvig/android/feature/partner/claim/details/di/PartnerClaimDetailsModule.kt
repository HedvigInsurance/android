package com.hedvig.android.feature.partner.claim.details.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.feature.partner.claim.details.data.GetPartnerClaimDetailUseCase
import com.hedvig.android.feature.partner.claim.details.ui.PartnerClaimDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val partnerClaimDetailsModule = module {
  single<GetPartnerClaimDetailUseCase> {
    GetPartnerClaimDetailUseCase(get<ApolloClient>())
  }
  viewModel<PartnerClaimDetailsViewModel> { (claimId: String) ->
    PartnerClaimDetailsViewModel(
      claimId = claimId,
      getPartnerClaimDetailUseCase = get<GetPartnerClaimDetailUseCase>(),
    )
  }
}
