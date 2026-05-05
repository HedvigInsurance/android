package com.hedvig.android.app.di

import com.hedvig.android.app.AppInitializers
import com.hedvig.android.app.crosssell.GetMemberAuthorizationCodeUseCase
import com.hedvig.android.initializable.Initializable
import io.ktor.client.HttpClient
import org.koin.dsl.module

val appModule = module {
  single<AppInitializers> {
    AppInitializers(getAll<Initializable>().toSet())
  }
  single<GetMemberAuthorizationCodeUseCase> {
    GetMemberAuthorizationCodeUseCase(get<HttpClient>(), get())
  }
}
