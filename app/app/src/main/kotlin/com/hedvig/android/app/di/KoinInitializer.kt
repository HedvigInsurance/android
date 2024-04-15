package com.hedvig.android.app.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.startup.TimberInitializer
import com.hedvig.app.applicationModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class KoinInitializer : Initializer<KoinApplication> {
  override fun create(context: Context): KoinApplication = startKoin {
    androidLogger(Level.ERROR)
    androidContext(context.applicationContext)
    workManagerFactory()
    modules(applicationModule)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(TimberInitializer::class.java)
  }
}
