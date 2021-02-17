package com.hedvig.onboarding.embark

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionParams
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModelImpl
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetParameter
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

object EmbarkModule {

    fun init() = loadKoinModulesIfNotDefined(modules)

    fun unload() = unloadKoinModules(modules)

    val embarkModule = module {
        single { EmbarkRepository(get(), get(), get(), get()) }
        viewModel<EmbarkViewModel> { EmbarkViewModelImpl(get(), get()) }
    }

    private val trackerModule = module {
        single<EmbarkTracker> { EmbarkTrackerImpl(get()) }
    }

    private val viewModelsModule = module {
        viewModel<PreviousInsurerViewModel> { PreviousInsurerViewModelImpl() }
        viewModel { (data: TextActionSetParameter) -> TextActionSetViewModel(data) }
        viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
    }

    private val modules = viewModelsModule + trackerModule + embarkModule
}
