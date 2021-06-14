package com.hedvig.app.di

import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.loggedin.ui.LoggedInRepositoryImpl
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import com.hedvig.app.feature.whatsnew.WhatsNewRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class LiveRepositoriesModule {
    @Binds
    abstract fun bindWhatsNewRepository(repo: WhatsNewRepositoryImpl): WhatsNewRepository

    @Binds
    abstract fun bindLoggedInRepository(repo: LoggedInRepositoryImpl): LoggedInRepository
}
