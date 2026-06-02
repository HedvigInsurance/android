package com.hedvig.android.feature.login.marketing

import androidx.lifecycle.ViewModel
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import com.hedvig.android.molecule.public.MoleculeViewModel
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class, binding<ViewModel>())
internal class MarketingViewModel(
  languageService: LanguageService,
) : MoleculeViewModel<MarketingEvent, MarketingUiState>(
    MarketingUiState.Loading,
    MarketingPresenter(languageService),
  )
