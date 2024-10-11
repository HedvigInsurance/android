package com.hedvig.android.feature.movingflow

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Optional
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.design.system.hedvig.HedvigTextButton
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TopAppBar
import com.hedvig.android.design.system.hedvig.TopAppBarActionType
import com.hedvig.android.logger.logcat
import com.hedvig.android.navigation.compose.Destination
import com.hedvig.android.navigation.compose.navdestination
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import octopus.feature.movingflow.MoveIntentV2CommitMutation
import octopus.feature.movingflow.MoveIntentV2CreateMutation
import octopus.feature.movingflow.MoveIntentV2RequestMutation
import octopus.feature.movingflow.MoveIntentV2RequestMutation.Data.MoveIntentRequest
import octopus.feature.movingflow.fragment.MoveIntentFragment
import octopus.type.MoveApartmentSubType
import octopus.type.MoveApiVersion
import octopus.type.MoveIntentRequestInput
import octopus.type.MoveToAddressInput
import octopus.type.MoveToApartmentInput

@Serializable
data object MovingFlowDestination : Destination

fun NavGraphBuilder.movingFlowGraph(apolloClient: ApolloClient) {
  navdestination<MovingFlowDestination> {
    val coroutineScope = rememberCoroutineScope()
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = HedvigTheme.colorScheme.backgroundPrimary,
    ) {
      var moveIntent: MoveIntentFragment? by remember { mutableStateOf(null) }
      var request: MoveIntentRequest? by remember { mutableStateOf(null) }
      Column {
        TopAppBar("Moving flow", TopAppBarActionType.BACK, {})
        HedvigTextButton("Create") {
          coroutineScope.launch {
            apolloClient
              .mutation(MoveIntentV2CreateMutation())
              .safeExecute()
              .getOrNull()!!
              .moveIntentCreate
              .moveIntent!!
              .also {
                logcat { it.toString() }
                moveIntent = it
              }
          }
        }
        HedvigTextButton("Request") {
          coroutineScope.launch {
            @Suppress("NAME_SHADOWING")
            val moveIntent = moveIntent!!
            apolloClient
              .mutation(
                MoveIntentV2RequestMutation(
                  moveIntent.id,
                  MoveIntentRequestInput(
                    apiVersion = Optional.present(MoveApiVersion.V2_TIERS_AND_DEDUCTIBLES),
                    moveToAddress = MoveToAddressInput(
                      street = "St:street",
                      postalCode = "14755",
                      city = Optional.absent(),
                    ),
                    moveFromAddressId = moveIntent.currentHomeAddresses[0].id,
                    movingDate = Clock.System.now()
                      .toLocalDateTime(TimeZone.currentSystemDefault()).date
                      .plus(DatePeriod(days = 1)),
                    numberCoInsured = 0,
                    squareMeters = 52,
                    apartment = Optional.present(
                      MoveToApartmentInput(
                        subType = MoveApartmentSubType.RENT,
                        isStudent = false,
                      ),
                    ),
                    house = Optional.absent(),
                  ),
                ),
              )
              .safeExecute()
              .getOrNull()!!
              .moveIntentRequest
              .also { it: MoveIntentRequest ->
                logcat { it.toString() }
                request = it
              }
          }
        }
        HedvigTextButton("Commit") {
          val request = request!!
          coroutineScope.launch {
            apolloClient
              .mutation(MoveIntentV2CommitMutation(request.moveIntent!!.id, request.moveIntent.homeQuotes!![0].id))
              .safeExecute()
              .getOrNull()!!
              .moveIntentCommit
              .moveIntent
          }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
      }
    }
  }
}
