package com.hedvig.android.navigation.core.di

import com.hedvig.android.core.common.di.isProductionQualifier
import com.hedvig.android.navigation.core.HedvigDeepLinkContainer
import com.hedvig.android.navigation.core.HedvigDeepLinkContainerImpl
import org.koin.dsl.module

val deepLinkModule = module {
  single<HedvigDeepLinkContainer> { HedvigDeepLinkContainerImpl(get<Boolean>(isProductionQualifier)) }
}
