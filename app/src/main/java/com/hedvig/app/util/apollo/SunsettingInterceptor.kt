package com.hedvig.app.util.apollo

import android.content.Context
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.exception.ApolloException
import com.apollographql.apollo.interceptor.ApolloInterceptor
import com.apollographql.apollo.interceptor.ApolloInterceptorChain
import com.hedvig.app.feature.sunsetting.ForceUpgradeActivity
import java.util.concurrent.Executor

class SunsettingInterceptor(
    private val context: Context,
) : ApolloInterceptor {
    @Volatile
    var disposed: Boolean = false

    override fun interceptAsync(
        request: ApolloInterceptor.InterceptorRequest,
        chain: ApolloInterceptorChain,
        dispatcher: Executor,
        callBack: ApolloInterceptor.CallBack,
    ) {
        chain.proceedAsync(
            request, dispatcher,
            object : ApolloInterceptor.CallBack {
                override fun onResponse(response: ApolloInterceptor.InterceptorResponse) {
                    if (disposed) {
                        return
                    }
                    val parsedResponse = response.parsedResponse.orNull()

                    if (parsedResponse == null) {
                        callBack.onResponse(response)
                        callBack.onCompleted()
                        return
                    }

                    val errors = parsedResponse.errors

                    if (errors == null) {
                        callBack.onResponse(response)
                        callBack.onCompleted()
                        return
                    }

                    val hasBeenSunset = errors.any(::isSunsetError)

                    if (hasBeenSunset) {
                        context.startActivity(ForceUpgradeActivity.newInstance(context))
                    }

                    callBack.onResponse(response)
                    callBack.onCompleted()
                }

                override fun onFetch(sourceType: ApolloInterceptor.FetchSourceType?) {
                    callBack.onFetch(sourceType)
                }

                override fun onFailure(e: ApolloException) {
                    if (disposed) {
                        return
                    }

                    callBack.onFailure(e)
                }

                override fun onCompleted() {
                }
            }
        )
    }

    override fun dispose() {
        disposed = true
    }
}

private fun isSunsetError(error: Any?): Boolean {
    if (error !is Error) {
        return false
    }
    val errorCode = error.customAttributes["errorCode"]

    return errorCode == "invalid_version"
}
