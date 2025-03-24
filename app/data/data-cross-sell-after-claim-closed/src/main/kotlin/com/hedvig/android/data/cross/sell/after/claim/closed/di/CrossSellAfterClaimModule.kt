package com.hedvig.android.data.cross.sell.after.claim.closed.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepository
import com.hedvig.android.data.cross.sell.after.claim.closed.CrossSellAfterClaimClosedRepositoryImpl
import org.koin.dsl.module

val crossSellAfterClaimClosedModule = module {
  single<CrossSellAfterClaimClosedRepository> {
    CrossSellAfterClaimClosedRepositoryImpl(get<ApolloClient>())
  }
}
