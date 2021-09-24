package com.hedvig.app.di

import android.content.Context
import com.hedvig.app.BuildConfig
import com.hedvig.app.authenticate.AuthenticationTokenService
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.isDebug
import com.hedvig.app.makeLocaleString
import com.hedvig.app.makeUserAgent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

    @Provides
    fun provideOkHttpClient(
        marketManager: MarketManager,
        authenticationTokenService: AuthenticationTokenService,
        @ApplicationContext context: Context
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val builder = original
                    .newBuilder()
                    .method(original.method, original.body)
                authenticationTokenService.authenticationToken?.let { token ->
                    builder.header("Authorization", token)
                }
                chain.proceed(builder.build())
            }
            .addInterceptor { chain ->
                chain.proceed(
                    chain
                        .request()
                        .newBuilder()
                        .header("User-Agent", makeUserAgent(context, marketManager.market))
                        .header("Accept-Language", makeLocaleString(context, marketManager.market))
                        .header("apollographql-client-name", BuildConfig.APPLICATION_ID)
                        .header("apollographql-client-version", BuildConfig.VERSION_NAME)
                        .build()
                )
            }
        if (isDebug()) {
            val logger = HttpLoggingInterceptor { message -> Timber.tag("OkHttp").i(message) }
            logger.level = HttpLoggingInterceptor.Level.BODY
            builder.addInterceptor(logger)
        }
        return builder.build()
    }
}
