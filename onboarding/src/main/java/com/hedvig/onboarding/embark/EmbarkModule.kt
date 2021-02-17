package com.hedvig.onboarding.embark

import com.hedvig.onboarding.chooseplan.MoreOptionsViewModel
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModelImpl
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionParams
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModelImpl
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetParameter
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetViewModel
import com.hedvig.onboarding.mocks.MockEmbarkViewModel
import com.hedvig.onboarding.mocks.MockMoreOptionsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

object EmbarkModule {

    fun init() = loadKoinModules(
        listOf(
            viewModelsModule,
            repositoryModule,
            embarkTrackerModule
        )
    )

    fun initMocks() = loadKoinModules(
        listOf(
            viewModelsModule,
            repositoryModule,
            embarkTrackerModule
        ) + mockViewModelsModule
    )

    private val viewModelsModule = module {
        viewModel<EmbarkViewModel> { EmbarkViewModelImpl(get(), get()) }
        viewModel<PreviousInsurerViewModel> { PreviousInsurerViewModelImpl() }
        viewModel<MoreOptionsViewModel>(override = true) { MoreOptionsViewModelImpl(get()) }
        viewModel { (data: TextActionSetParameter) -> TextActionSetViewModel(data) }
        viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
    }

    private val mockViewModelsModule = module(override = true) {
        viewModel<EmbarkViewModel> { MockEmbarkViewModel(get()) }
        viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
    }

    private val repositoryModule = module {
        single { EmbarkRepository(get(), get(), get(), get()) }
    }

    private val embarkTrackerModule = module { single<EmbarkTracker> { EmbarkTrackerImpl(get()) } }
}
