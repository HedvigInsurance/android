package com.hedvig.app.mocks

import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel
import com.hedvig.app.feature.marketing.ui.MarketingStoriesViewModel
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.viewmodel.DirectDebitViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
    viewModel<OfferViewModel> { MockOfferViewModel(get()) }
    viewModel<ProfileViewModel> { MockProfileViewModel() }
    viewModel<DirectDebitViewModel> { MockDirectDebitViewModel() }
    viewModel<KeyGearViewModel> { MockKeyGearViewModel() }
    viewModel<KeyGearItemDetailViewModel> { MockKeyGearItemDetailViewModel() }
    viewModel<CreateKeyGearItemViewModel> { MockCreateKeyGearItemViewModel() }
    viewModel<KeyGearValuationViewModel> { MockKeyGearValuationViewModel() }
    viewModel<MarketingStoriesViewModel> { MockMarketingStoriesViewModel() }
}
