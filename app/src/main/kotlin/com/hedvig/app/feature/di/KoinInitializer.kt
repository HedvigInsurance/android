package com.hedvig.app.feature.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.auth.di.authModule
import com.hedvig.android.core.datastore.di.dataStoreModule
import com.hedvig.android.feature.businessmodel.di.businessModelModule
import com.hedvig.android.hanalytics.android.di.hAnalyticsAndroidModule
import com.hedvig.android.hanalytics.di.hAnalyticsModule
import com.hedvig.android.hanalytics.engineering.di.HAnalyticsEngineeringModuleImpl
import com.hedvig.android.hanalytics.featureflags.di.featureManagerModule
import com.hedvig.android.language.di.languageModule
import com.hedvig.android.market.di.marketManagerModule
import com.hedvig.android.notification.badge.data.di.notificationBadgeModule
import com.hedvig.app.adyenModule
import com.hedvig.app.apolloClientModule
import com.hedvig.app.applicationModule
import com.hedvig.app.authRepositoryModule
import com.hedvig.app.cacheManagerModule
import com.hedvig.app.changeAddressModule
import com.hedvig.app.changeDateBottomSheetModule
import com.hedvig.app.chatEventModule
import com.hedvig.app.checkoutModule
import com.hedvig.app.clockModule
import com.hedvig.app.coilModule
import com.hedvig.app.connectPaymentModule
import com.hedvig.app.datastoreAndroidModule
import com.hedvig.app.embarkModule
import com.hedvig.app.externalInsuranceModule
import com.hedvig.app.graphQLQueryModule
import com.hedvig.app.homeModule
import com.hedvig.app.insuranceModule
import com.hedvig.app.logModule
import com.hedvig.app.loggedInModule
import com.hedvig.app.navigatorModule
import com.hedvig.app.notificationModule
import com.hedvig.app.numberActionSetModule
import com.hedvig.app.offerModule
import com.hedvig.app.onboardingModule
import com.hedvig.app.paymentModule
import com.hedvig.app.profileModule
import com.hedvig.app.pushTokenManagerModule
import com.hedvig.app.referralsModule
import com.hedvig.app.repositoriesModule
import com.hedvig.app.serviceModule
import com.hedvig.app.sharedPreferencesModule
import com.hedvig.app.stringConstantsModule
import com.hedvig.app.textActionSetModule
import com.hedvig.app.trustlyModule
import com.hedvig.app.useCaseModule
import com.hedvig.app.valueStoreModule
import com.hedvig.app.viewModelModule
import com.hedvig.app.whatsNewModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoinInitializer : Initializer<KoinApplication> {
  override fun create(context: Context) = startKoin {
    androidLogger(Level.ERROR)
    androidContext(context.applicationContext)
    modules(
      listOf(
        HAnalyticsEngineeringModuleImpl().getModule(),
        adyenModule,
        apolloClientModule,
        applicationModule,
        authModule,
        businessModelModule,
        cacheManagerModule,
        changeAddressModule,
        changeDateBottomSheetModule,
        chatEventModule,
        checkoutModule,
        clockModule,
        coilModule,
        connectPaymentModule,
        datastoreAndroidModule,
        dataStoreModule,
        embarkModule,
        externalInsuranceModule,
        featureManagerModule,
        graphQLQueryModule,
        hAnalyticsAndroidModule,
        hAnalyticsModule,
        homeModule,
        insuranceModule,
        languageModule,
        logModule,
        loggedInModule,
        marketManagerModule,
        notificationBadgeModule,
        notificationModule,
        navigatorModule,
        numberActionSetModule,
        offerModule,
        onboardingModule,
        paymentModule,
        profileModule,
        pushTokenManagerModule,
        referralsModule,
        repositoriesModule,
        serviceModule,
        sharedPreferencesModule,
        stringConstantsModule,
        textActionSetModule,
        trustlyModule,
        useCaseModule,
        valueStoreModule,
        viewModelModule,
        whatsNewModule,
        authRepositoryModule,
      ),
    )
  }

  override fun dependencies() = emptyList<Class<out Initializer<*>>>()
}
