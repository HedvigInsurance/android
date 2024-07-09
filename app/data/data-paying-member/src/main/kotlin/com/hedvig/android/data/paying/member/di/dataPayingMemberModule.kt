package com.hedvig.android.data.paying.member.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseDemo
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseImpl
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import org.koin.dsl.module

val dataPayingMemberModule = module {
  single<GetOnlyHasNonPayingContractsUseCaseProvider> {
    GetOnlyHasNonPayingContractsUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = GetOnlyHasNonPayingContractsUseCaseDemo(),
      prodImpl = GetOnlyHasNonPayingContractsUseCaseImpl(get<ApolloClient>()),
    )
  }
}
