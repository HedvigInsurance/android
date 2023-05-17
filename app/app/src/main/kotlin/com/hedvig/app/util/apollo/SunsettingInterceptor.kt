package com.hedvig.app.util.apollo

import android.content.Context
import com.apollographql.apollo3.api.ApolloRequest
import com.apollographql.apollo3.api.ApolloResponse
import com.apollographql.apollo3.api.Error
import com.apollographql.apollo3.api.Operation
import com.apollographql.apollo3.interceptor.ApolloInterceptor
import com.apollographql.apollo3.interceptor.ApolloInterceptorChain
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SunsettingInterceptor(
  private val context: Context,
) : ApolloInterceptor {
  override fun <D : Operation.Data> intercept(
    request: ApolloRequest<D>,
    chain: ApolloInterceptorChain,
  ): Flow<ApolloResponse<D>> {
    return chain.proceed(request)
      .map { response ->
        val hasBeenSunset = response.errors?.any(Error::isSunsetError)
        if (hasBeenSunset == true) {
          context.startActivity(ForceUpgradeActivity.newInstance(context))
          // Consider maybe returning an error instead? It now just replicates the old behavior.
          // Something like: `return response.newBuilder().removeDataAndAddErrorInstead()`
        }
        response
      }
  }

  companion object {
    const val SUNSETTING_ERROR_CODE = "invalid_version"
  }
}

private fun Error.isSunsetError(): Boolean {
  return nonStandardFields?.get("errorCode") == SunsettingInterceptor.SUNSETTING_ERROR_CODE
}
