package com.hedvig.onboarding.embark.ui

import com.hedvig.app.util.loadKoinModulesIfNotDefined
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModel
import com.hedvig.onboarding.chooseplan.MoreOptionsViewModelImpl
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

object MoreOptionsModule {
    fun init() = loadKoinModulesIfNotDefined(listOf(moreOptionsModule))

    fun unload() = unloadKoinModules(moreOptionsModule)

    val moreOptionsModule = module {
        viewModel<MoreOptionsViewModel> { MoreOptionsViewModelImpl(get()) }
    }
}
