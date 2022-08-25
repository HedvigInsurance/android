package com.hedvig.app.mocks

import com.hedvig.app.feature.adyen.MockAdyenConnectPayinViewModel
import com.hedvig.app.feature.adyen.payin.AdyenConnectPayinViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
  viewModel<AdyenConnectPayinViewModel> { MockAdyenConnectPayinViewModel() }
}
