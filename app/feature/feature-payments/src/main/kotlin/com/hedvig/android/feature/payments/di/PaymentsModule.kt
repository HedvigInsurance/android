package com.hedvig.android.feature.payments.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCase
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCaseImpl
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCaseImpl
import com.hedvig.android.feature.payments.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCase
import com.hedvig.android.feature.payments.overview.data.AddDiscountUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCase
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCaseDemo
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetPaymentOverviewDataUseCaseProvider
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseImpl
import com.hedvig.android.feature.payments.payments.PaymentsViewModel
import kotlinx.datetime.Clock
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val paymentsModule = module {
  single<AddDiscountUseCase> {
    AddDiscountUseCaseImpl(
      get<ApolloClient>(),
      get<NetworkCacheManager>(),
    )
  }
  single<GetPaymentsHistoryUseCase> {
    GetPaymentsHistoryUseCaseImpl(
      get<ApolloClient>(),
    )
  }
  single<GetChargeDetailsUseCase> {
    GetChargeDetailsUseCaseImpl(
      get<ApolloClient>(),
      get<Clock>(),
    )
  }
  single<GetForeverInformationUseCase> {
    GetForeverInformationUseCaseImpl(
      get<ApolloClient>(),
    )
  }
  single<GetUpcomingPaymentUseCase> {
    GetUpcomingPaymentUseCaseImpl(
      get<ApolloClient>(),
      get<Clock>(),
    )
  }

  provideGetPaymentOverviewDataUseCase()

  viewModel<PaymentsViewModel> {
    PaymentsViewModel(
      get<GetPaymentOverviewDataUseCaseProvider>(),
    )
  }

  viewModel<DiscountsViewModel> {
    DiscountsViewModel(
      get<GetPaymentOverviewDataUseCaseProvider>(),
      get<AddDiscountUseCase>(),
    )
  }

  viewModel<PaymentDetailsViewModel> { (chargeId: String) ->
    PaymentDetailsViewModel(
      chargeId = chargeId,
      getChargeDetailsUseCase = get<GetChargeDetailsUseCase>(),
    )
  }
}

private fun Module.provideGetPaymentOverviewDataUseCase() {
  single<GetPaymentOverviewDataUseCaseImpl> {
    GetPaymentOverviewDataUseCaseImpl(
      getUpcomingPaymentUseCase = get<GetUpcomingPaymentUseCase>(),
      getForeverInformationUseCase = get<GetForeverInformationUseCase>(),
      getOnlyHasNonPayingContractsUseCase = get<GetOnlyHasNonPayingContractsUseCaseProvider>().prodImpl,
    )
  }
  single<GetPaymentOverviewDataUseCaseDemo> {
    GetPaymentOverviewDataUseCaseDemo(
      clock = get<Clock>(),
    )
  }
  single<GetPaymentOverviewDataUseCaseProvider> {
    GetPaymentOverviewDataUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<GetPaymentOverviewDataUseCaseDemo>(),
      prodImpl = get<GetPaymentOverviewDataUseCaseImpl>(),
    )
  }
}
