package com.hedvig.android.apollo.giraffe.di

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import org.koin.dsl.module

@Suppress("RemoveExplicitTypeArguments")
val giraffeModule = module {
  publicAndroidModuleFunction("").also {
    it + 1
  }
  single<ApolloClient>(giraffeClient) {
    val hedvigBuildConstants = get<HedvigBuildConstants>()
    get<ApolloClient.Builder>().copy()
      .httpServerUrl(hedvigBuildConstants.urlGiraffeGraphql)
      .webSocketServerUrl(hedvigBuildConstants.urlGiraffeGraphqlSubscription)
      .build()
  }
}

fun publicAndroidModuleFunction(input: String): Int {
  return 3
}
