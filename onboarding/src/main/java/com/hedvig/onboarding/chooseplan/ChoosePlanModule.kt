package com.hedvig.onboarding.chooseplan

import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.settings.MarketManagerImpl
import com.hedvig.onboarding.mocks.MockChoosePlanViewModel
import com.hedvig.onboarding.mocks.MockMoreOptionsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

object ChoosePlanModule {

    fun init() = loadKoinModules(memberIdRepository + modules)
    fun initMocks() = loadKoinModules(memberIdRepository + modules + mocks)

    private val mocks = listOf(
        module(override = true) {
            viewModel<MoreOptionsViewModel> { MockMoreOptionsViewModel() }
            viewModel<ChoosePlanViewModel> { MockChoosePlanViewModel() }
            single<MarketManager>(override = true) { MarketManagerImpl(get(), get()) }
        }
    )

    private val modules = listOf(
        module {
            viewModel<MoreOptionsViewModel> { MoreOptionsViewModelImpl(get()) }
            viewModel<ChoosePlanViewModel> { ChoosePlanViewModelImpl(get()) }
            single { ChoosePlanRepository(get(), get()) }
        }
    )

    private val memberIdRepository = module {
        single {
            MemberIdRepository(get())
        }
    }
}
