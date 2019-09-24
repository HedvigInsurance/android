package com.hedvig.app

import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.viewmodel.DirectDebitViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mockProfileModule = module {
    viewModel<ProfileViewModel> { MockProfileViewModel() }
    viewModel<DirectDebitViewModel> { MockDirectDebitViewModel() }
}
