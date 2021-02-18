package com.hedvig.onboarding.chooseplan

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

object ChoosePlanModule {

    fun init() = loadKoinModulesIfNotDefined(modules)

    fun unload() = unloadKoinModules(modules)

    val choosePlanModule = module {
        viewModel<ChoosePlanViewModel> { ChoosePlanViewModelImpl(get()) }
    }

    private val choosePlanRepository = module {
        single { ChoosePlanRepository(get(), get(), get()) }
    }

    private val memberIdRepository = module {
        single { MemberIdRepository(get()) }
    }

    private val modules = memberIdRepository + choosePlanModule + choosePlanRepository
}
