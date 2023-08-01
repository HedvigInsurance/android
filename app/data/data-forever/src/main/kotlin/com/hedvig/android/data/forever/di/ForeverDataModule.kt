package com.hedvig.android.data.forever.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.giraffe.di.giraffeClient
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.language.LanguageService
import org.koin.dsl.module

val foreverDataModule = module {
  single<ForeverRepository> { ForeverRepository(get<ApolloClient>(giraffeClient), get<LanguageService>()) }
}
