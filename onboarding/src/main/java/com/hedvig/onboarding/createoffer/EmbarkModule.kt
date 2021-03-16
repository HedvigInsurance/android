package com.hedvig.onboarding.createoffer

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import com.hedvig.onboarding.createoffer.passages.datepicker.DatePickerViewModel
import com.hedvig.onboarding.createoffer.passages.numberaction.NumberActionParams
import com.hedvig.onboarding.createoffer.passages.numberaction.NumberActionViewModel
import com.hedvig.onboarding.createoffer.passages.previousinsurer.PreviousInsurerViewModel
import com.hedvig.onboarding.createoffer.passages.previousinsurer.PreviousInsurerViewModelImpl
import com.hedvig.onboarding.createoffer.passages.textactionset.TextActionSetParameter
import com.hedvig.onboarding.createoffer.passages.textactionset.TextActionSetViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

object EmbarkModule {

    fun init() = loadKoinModulesIfNotDefined(modules)

    fun unload() = unloadKoinModules(modules)

    val embarkModule = module {
        viewModel<EmbarkViewModel> { EmbarkViewModelImpl(get(), get()) }
    }

    private val embarkRepository = module {
        single { EmbarkRepository(get(), get(), get(), get()) }
    }

    private val trackerModule = module {
        single<EmbarkTracker> { EmbarkTrackerImpl(get()) }
    }

    private val viewModelsModule = module {
        viewModel<PreviousInsurerViewModel> { PreviousInsurerViewModelImpl() }
        viewModel { (data: TextActionSetParameter) -> TextActionSetViewModel(data) }
        viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
        viewModel { DatePickerViewModel() }
    }

    private val modules = embarkRepository + viewModelsModule + trackerModule + embarkModule
}
