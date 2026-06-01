package com.hedvig.android.data.paying.member.di

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.core.demomode.Provider
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCase
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseDemo
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
interface DataPayingMemberMetroProviders {
  @Provides
  @SingleIn(AppScope::class)
  fun provideGetOnlyHasNonPayingContractsUseCaseProvider(
    demoManager: DemoManager,
    prodImpl: GetOnlyHasNonPayingContractsUseCase,
    demoImpl: GetOnlyHasNonPayingContractsUseCaseDemo,
  ): GetOnlyHasNonPayingContractsUseCaseProvider = GetOnlyHasNonPayingContractsUseCaseProvider(
    demoManager = demoManager,
    demoImpl = demoImpl,
    prodImpl = prodImpl,
  )

  @Provides
  fun provideGetOnlyHasNonPayingContractsUseCaseAsProvider(
    p: GetOnlyHasNonPayingContractsUseCaseProvider,
  ): Provider<GetOnlyHasNonPayingContractsUseCase> = p
}
