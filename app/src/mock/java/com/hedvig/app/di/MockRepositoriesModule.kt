package com.hedvig.app.di

import com.hedvig.app.feature.loggedin.ui.LoggedInRepository
import com.hedvig.app.feature.whatsnew.WhatsNewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MockRepositoriesModule {
    @Binds
    abstract fun bindWhatsNewRepo(repo: WhatsNewRepositoryMock): WhatsNewRepository

    @Binds
    abstract fun bindLoggedInRepository(repo: LoggedInRepositoryMock): LoggedInRepository
}
