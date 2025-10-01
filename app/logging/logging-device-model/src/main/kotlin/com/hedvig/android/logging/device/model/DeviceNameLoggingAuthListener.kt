package com.hedvig.android.logging.device.model

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.auth.event.AuthEventListener
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import octopus.MemberLogDeviceMutation

internal class DeviceNameLoggingAuthListener(
  private val apolloClient: ApolloClient,
) : AuthEventListener {
  override suspend fun loggedIn(accessToken: String) {
    val androidInfoProvider = AndroidInfoProvider()
    apolloClient.mutation(
      MemberLogDeviceMutation(
        brand = androidInfoProvider.deviceBrand,
        model = androidInfoProvider.deviceName,
      ),
    ).safeExecute().fold(
      ifLeft = {
        logcat(LogPriority.INFO) { "MemberLogDeviceMutation failed with error: $it" }
      },
      ifRight = {
        if (it.memberLogDevice?.message != null) {
          logcat(LogPriority.INFO) { "MemberLogDeviceMutation failed with userError: ${it.memberLogDevice.message}" }
        } else {
          logcat(LogPriority.INFO) { "MemberLogDeviceMutation succeeded" }
        }
      },
    )
  }
}
