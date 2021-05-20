package com.hedvig.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class RepoModule {

    @Provides
    fun repo(): RepoInterface {
        return object : RepoInterface {
            override fun getString(): String {
                return "Test"
            }
        }
    }
}
