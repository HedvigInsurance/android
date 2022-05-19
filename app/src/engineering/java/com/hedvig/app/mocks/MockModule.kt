package com.hedvig.app.mocks

import com.hedvig.app.feature.adyen.MockAdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import com.hedvig.app.feature.keygear.KeyGearValuationViewModel
import com.hedvig.app.feature.keygear.ui.createitem.CreateKeyGearItemViewModel
import com.hedvig.app.feature.keygear.ui.itemdetail.KeyGearItemDetailViewModel
import com.hedvig.app.feature.keygear.ui.tab.KeyGearViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
    viewModel<KeyGearViewModel> { MockKeyGearViewModel() }
    viewModel<KeyGearItemDetailViewModel> { MockKeyGearItemDetailViewModel() }
    viewModel<CreateKeyGearItemViewModel> { MockCreateKeyGearItemViewModel() }
    viewModel<KeyGearValuationViewModel> { MockKeyGearValuationViewModel() }
    viewModel<AdyenConnectPayinViewModel> { MockAdyenConnectPayinViewModel() }
}
