package com.hedvig.onboarding.chooseplan

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

object ChoosePlanModule {

    fun init() = loadKoinModulesIfNotDefined(
        memberIdRepository
            + moreOptionsModule
            + choosePlanModule
            + choosePlanRepository
    )

    fun unload() = unloadKoinModules(
        memberIdRepository
            + moreOptionsModule
            + choosePlanModule
            + choosePlanRepository
    )

    val choosePlanModule = module {
        viewModel<ChoosePlanViewModel> { ChoosePlanViewModelImpl(get()) }
    }

    private val moreOptionsModule = module {
        viewModel<MoreOptionsViewModel> { MoreOptionsViewModelImpl(get()) }
    }

    private val choosePlanRepository = module {
        single { ChoosePlanRepository(get(), get()) }
    }

    private val memberIdRepository = module {
        single { MemberIdRepository(get()) }
    }
}
