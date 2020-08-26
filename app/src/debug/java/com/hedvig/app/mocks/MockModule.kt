package com.hedvig.app.mocks

import com.hedvig.app.MockAdyenViewModel
import com.hedvig.app.MockContractDetailViewModel
import com.hedvig.app.MockDashboardViewModel
import com.hedvig.app.feature.adyen.AdyenViewModel
import com.hedvig.app.feature.dashboard.ui.DashboardViewModel
import com.hedvig.app.feature.dashboard.ui.contractcoverage.ContractCoverageViewModel
import com.hedvig.app.feature.dashboard.ui.contractdetail.ContractDetailViewModel
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.offer.MockOfferViewModel
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.profile.MockProfileViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
    viewModel<OfferViewModel> { MockOfferViewModel() }
    viewModel<ProfileViewModel> { MockProfileViewModel() }
    viewModel<PaymentViewModel> { MockPaymentViewModel() }
    viewModel<KeyGearViewModel> { MockKeyGearViewModel() }
    viewModel<KeyGearItemDetailViewModel> { MockKeyGearItemDetailViewModel() }
    viewModel<CreateKeyGearItemViewModel> { MockCreateKeyGearItemViewModel() }
    viewModel<KeyGearValuationViewModel> { MockKeyGearValuationViewModel() }
    viewModel<DashboardViewModel> { MockDashboardViewModel(get()) }
    viewModel<ContractDetailViewModel> { MockContractDetailViewModel(get()) }
    viewModel<ContractCoverageViewModel> { MockContractCoverageViewModel(get()) }
    viewModel<MarketingViewModel> { MockMarketingViewModel() }
    viewModel<AdyenViewModel> { MockAdyenViewModel() }
}
