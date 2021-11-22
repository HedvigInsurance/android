package com.hedvig.app.feature.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.app.adyenModule
import com.hedvig.app.applicationModule
import com.hedvig.app.cacheManagerModule
import com.hedvig.app.changeAddressModule
import com.hedvig.app.changeDateBottomSheetModule
import com.hedvig.app.chatEventModule
import com.hedvig.app.checkoutModule
import com.hedvig.app.choosePlanModule
import com.hedvig.app.clockModule
import com.hedvig.app.coilModule
import com.hedvig.app.connectPaymentModule
import com.hedvig.app.dataStoreModule
import com.hedvig.app.embarkModule
import com.hedvig.app.embarkTrackerModule
import com.hedvig.app.featureManagerModule
import com.hedvig.app.homeModule
import com.hedvig.app.insuranceModule
import com.hedvig.app.keyGearModule
import com.hedvig.app.localeBroadcastManagerModule
import com.hedvig.app.localeManagerModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.marketManagerModule
import com.hedvig.app.marketPickerModule
import com.hedvig.app.marketPickerTrackerModule
import com.hedvig.app.marketingModule
import com.hedvig.app.notificationModule
import com.hedvig.app.numberActionSetModule
import com.hedvig.app.offerModule
import com.hedvig.app.onboardingModule
import com.hedvig.app.paymentModule
import com.hedvig.app.previousInsViewModel
import com.hedvig.app.profileModule
import com.hedvig.app.pushTokenManagerModule
import com.hedvig.app.referralsModule
import com.hedvig.app.repositoriesModule
import com.hedvig.app.retrievePriceModule
import com.hedvig.app.serviceModule
import com.hedvig.app.sharedPreferencesModule
import com.hedvig.app.textActionSetModule
import com.hedvig.app.trackerModule
import com.hedvig.app.trustlyModule
import com.hedvig.app.useCaseModule
import com.hedvig.app.valueStoreModule
import com.hedvig.app.viewModelModule
import com.hedvig.app.whatsNewModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

class KoinInitializer : Initializer<KoinApplication> {
    override fun create(context: Context) = startKoin {
        androidLogger()
        androidContext(context.applicationContext)
        modules(
            listOf(
                applicationModule,
                viewModelModule,
                loggedInModule,
                insuranceModule,
                marketingModule,
                offerModule,
                profileModule,
                paymentModule,
                keyGearModule,
                adyenModule,
                referralsModule,
                homeModule,
                serviceModule,
                repositoriesModule,
                localeBroadcastManagerModule,
                trackerModule,
                embarkModule,
                previousInsViewModel,
                marketPickerTrackerModule,
                whatsNewModule,
                marketManagerModule,
                connectPaymentModule,
                trustlyModule,
                notificationModule,
                marketPickerModule,
                textActionSetModule,
                numberActionSetModule,
                choosePlanModule,
                clockModule,
                localeManagerModule,
                changeAddressModule,
                changeDateBottomSheetModule,
                useCaseModule,
                valueStoreModule,
                onboardingModule,
                pushTokenManagerModule,
                checkoutModule,
                cacheManagerModule,
                sharedPreferencesModule,
                coilModule,
                embarkTrackerModule,
                chatEventModule,
                dataStoreModule,
                featureManagerModule,
                retrievePriceModule,
            )
        )
    }

    override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
