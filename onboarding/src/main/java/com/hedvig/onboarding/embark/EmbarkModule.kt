package com.hedvig.onboarding.embark

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModel
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModelImpl
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

    fun init() = loadKoinModulesIfNotDefined(viewModelsModule + embarkModule + moreOptionsModule)

    fun unload() = unloadKoinModules(viewModelsModule + embarkModule + moreOptionsModule)

    val embarkModule = module {
        single { EmbarkRepository(get(), get(), get(), get()) }
        single<EmbarkTracker> { EmbarkTrackerImpl(get()) }
        viewModel<EmbarkViewModel> { EmbarkViewModelImpl(get(), get()) }
    }

    val moreOptionsModule = module {
        viewModel<MoreOptionsViewModel> { MoreOptionsViewModelImpl(get()) }
    }

    private val viewModelsModule = module {
        viewModel<PreviousInsurerViewModel> { PreviousInsurerViewModelImpl() }
        viewModel { (data: TextActionSetParameter) -> TextActionSetViewModel(data) }
        viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
    }
}
