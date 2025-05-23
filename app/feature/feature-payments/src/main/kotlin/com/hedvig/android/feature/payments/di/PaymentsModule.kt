package com.hedvig.android.feature.payments.di

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.demomode.DemoManager
import com.hedvig.android.data.paying.member.GetOnlyHasNonPayingContractsUseCaseProvider
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCase
import com.hedvig.android.feature.payments.data.GetChargeDetailsUseCaseImpl
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCase
import com.hedvig.android.feature.payments.data.GetDiscountsOverviewUseCaseImpl
import com.hedvig.android.feature.payments.data.GetDiscountsUseCase
import com.hedvig.android.feature.payments.data.GetDiscountsUseCaseImpl
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCase
import com.hedvig.android.feature.payments.data.GetPaymentsHistoryUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCase
import com.hedvig.android.feature.payments.overview.data.GetForeverInformationUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCase
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseDemo
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseImpl
import com.hedvig.android.feature.payments.overview.data.GetUpcomingPaymentUseCaseProvider
import com.hedvig.android.feature.payments.ui.details.PaymentDetailsViewModel
import com.hedvig.android.feature.payments.ui.discounts.DiscountsViewModel
import com.hedvig.android.feature.payments.ui.history.PaymentHistoryViewModel
import com.hedvig.android.feature.payments.ui.payments.PaymentsViewModel
import kotlinx.datetime.Clock
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val paymentsModule = module {
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
  single<GetDiscountsOverviewUseCase> {
    GetDiscountsOverviewUseCaseImpl(
      get<GetDiscountsUseCase>(),
      get<GetForeverInformationUseCase>(),
      get<GetOnlyHasNonPayingContractsUseCaseProvider>().prodImpl,
    )
  }

  single<GetDiscountsUseCase> {
    GetDiscountsUseCaseImpl(
      get<ApolloClient>(),
      get<Clock>(),
    )
  }

  viewModel<PaymentsViewModel> {
    PaymentsViewModel(
      get<GetUpcomingPaymentUseCaseProvider>(),
    )
  }

  viewModel<DiscountsViewModel> {
    DiscountsViewModel(
      get<GetDiscountsOverviewUseCase>(),
    )
  }

  viewModel<PaymentDetailsViewModel> { (chargeId: String?) ->
    PaymentDetailsViewModel(
      chargeId = chargeId,
      getChargeDetailsUseCase = get<GetChargeDetailsUseCase>(),
    )
  }

  viewModel<PaymentHistoryViewModel> {
    PaymentHistoryViewModel(
      get<GetPaymentsHistoryUseCase>(),
    )
  }
  single<GetUpcomingPaymentUseCaseProvider> {
    GetUpcomingPaymentUseCaseProvider(
      demoManager = get<DemoManager>(),
      demoImpl = get<GetUpcomingPaymentUseCaseDemo>(),
      prodImpl = get<GetUpcomingPaymentUseCaseImpl>(),
    )
  }
  single<GetUpcomingPaymentUseCaseImpl> {
    GetUpcomingPaymentUseCaseImpl(
      get<ApolloClient>(),
      clock = get<Clock>(),
    )
  }
  single<GetUpcomingPaymentUseCaseDemo> {
    GetUpcomingPaymentUseCaseDemo(
      clock = get<Clock>(),
    )
  }
}
