package com.hedvig.onboarding.embark

import com.hedvig.app.feature.onboarding.MoreOptionsViewModel
import com.hedvig.app.feature.onboarding.MoreOptionsViewModelImpl
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionParams
import com.hedvig.onboarding.embark.passages.numberaction.NumberActionViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModel
import com.hedvig.onboarding.embark.passages.previousinsurer.PreviousInsurerViewModelImpl
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetParameter
import com.hedvig.onboarding.embark.passages.textactionset.TextActionSetViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.dsl.module

object OnboardingModule {

    fun init() = loadKoinModules(
        listOf(
            embarkModule,
            previousInsViewModel,
            moreOptionsModule,
            textActionSetModule,
            numberActionModule,
            repositoryModule,
            embarkTrackerModule
        )
    )

    val embarkModule = module {
        viewModel<EmbarkViewModel> { EmbarkViewModelImpl(get(), get()) }
    }

    val previousInsViewModel = module {
        viewModel<PreviousInsurerViewModel> { PreviousInsurerViewModelImpl() }
    }

    val moreOptionsModule = module {
        viewModel<MoreOptionsViewModel> { MoreOptionsViewModelImpl(get()) }
    }

    val textActionSetModule = module {
        viewModel { (data: TextActionSetParameter) -> TextActionSetViewModel(data) }
    }

    val numberActionModule = module {
        viewModel { (data: NumberActionParams) -> NumberActionViewModel(data) }
    }

    val repositoryModule = module {
        single { EmbarkRepository(get(), get(), get()) }
    }

    val embarkTrackerModule = module { single<EmbarkTracker> { EmbarkTrackerImpl(get()) } }
}
