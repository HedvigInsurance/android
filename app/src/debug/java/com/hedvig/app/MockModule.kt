package com.hedvig.app

import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.viewmodel.DirectDebitViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockModule = module {
    viewModel<OfferViewModel> { MockOfferViewModel(get()) }
    viewModel<ProfileViewModel> { MockProfileViewModel() }
    viewModel<DirectDebitViewModel> { MockDirectDebitViewModel() }
}
