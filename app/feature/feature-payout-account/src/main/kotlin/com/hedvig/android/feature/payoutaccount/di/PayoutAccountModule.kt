package com.hedvig.android.feature.payoutaccount.di

import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.GetPayoutAccountUseCaseImpl
import com.hedvig.android.feature.payoutaccount.data.UpdateBankAccountUseCase
import com.hedvig.android.feature.payoutaccount.data.UpdateBankAccountUseCaseImpl
import com.hedvig.android.feature.payoutaccount.ui.editbankaccount.EditBankAccountViewModel
import com.hedvig.android.feature.payoutaccount.ui.overview.PayoutAccountOverviewViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val payoutAccountModule = module {
  single<GetPayoutAccountUseCaseImpl> { GetPayoutAccountUseCaseImpl() }
  single<GetPayoutAccountUseCase> { get<GetPayoutAccountUseCaseImpl>() }
  single<UpdateBankAccountUseCase> { UpdateBankAccountUseCaseImpl(get<GetPayoutAccountUseCaseImpl>()) }
  viewModel<PayoutAccountOverviewViewModel> { PayoutAccountOverviewViewModel(get<GetPayoutAccountUseCase>()) }
  viewModel<EditBankAccountViewModel> { EditBankAccountViewModel(get<UpdateBankAccountUseCase>()) }
}
