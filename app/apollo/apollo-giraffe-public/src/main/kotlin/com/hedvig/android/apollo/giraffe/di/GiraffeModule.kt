package com.hedvig.android.apollo.giraffe.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val giraffeModule = module {
  single<ApolloClient>(giraffeClient) {
    val hedvigBuildConstants = get<HedvigBuildConstants>()
    get<ApolloClient.Builder>().copy()
      .httpServerUrl(hedvigBuildConstants.urlGiraffeGraphql)
      .webSocketServerUrl(hedvigBuildConstants.urlGiraffeGraphqlSubscription)
      .build()
  }
}
