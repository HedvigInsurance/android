package com.hedvig.app.mocks

import com.hedvig.app.MockInsuranceViewModel
import com.hedvig.app.feature.adyen.MockAdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.insurance.ui.InsuranceViewModel
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel
import com.hedvig.app.feature.marketing.ui.MarketingViewModel
import com.hedvig.app.feature.offer.MockOfferViewModel
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.profile.MockProfileViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
    viewModel<OfferViewModel> { MockOfferViewModel() }
    viewModel<ProfileViewModel> { MockProfileViewModel() }
    viewModel<KeyGearViewModel> { MockKeyGearViewModel() }
    viewModel<KeyGearItemDetailViewModel> { MockKeyGearItemDetailViewModel() }
    viewModel<CreateKeyGearItemViewModel> { MockCreateKeyGearItemViewModel() }
    viewModel<KeyGearValuationViewModel> { MockKeyGearValuationViewModel() }
    viewModel<InsuranceViewModel> { MockInsuranceViewModel(get()) }
    viewModel<MarketingViewModel> { MockMarketingViewModel() }
    viewModel<AdyenConnectPayinViewModel> { MockAdyenConnectPayinViewModel() }
}
