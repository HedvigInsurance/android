package com.hedvig.android.feature.claim.details.navigation

import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.CollectionNavType
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navigation
import androidx.navigation.serialization.generateRouteWithArgs
import androidx.navigation.toRoute
import kotlin.collections.ArrayList
import coil.ImageLoader
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import kotlin.reflect.typeOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.serializer
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

fun NavGraphBuilder.claimDetailsGraph(
  imageLoader: ImageLoader,
  appPackageId: String,
  openUrl: (String) -> Unit,
  navigateUp: () -> Unit,
  openChat: (NavBackStackEntry) -> Unit,
  navController: NavHostController,
  applicationId: String,
) {
  navigation<AppDestination.ClaimDetails>(
    startDestination = ClaimDetailDestinations.ClaimOverviewDestination::class,
  ) {
    composable<ClaimDetailDestinations.ClaimOverviewDestination> { backStackEntry ->
      val viewModel: ClaimDetailsViewModel = koinViewModel {
        parametersOf(backStackEntry.toRoute<ClaimDetailDestinations.ClaimOverviewDestination>().claimId)
      }
      val context = LocalContext.current
      ClaimDetailsDestination(
        viewModel = viewModel,
        imageLoader = imageLoader,
        appPackageId = appPackageId,
        navigateUp = navigateUp,
        onChatClick = { openChat(backStackEntry) },
        onUri = { filesUri: List<Uri>, uploadUri: String ->
          navController.navigate(
            ClaimDetailDestinations.AddFilesDestination(
              targetUploadUrl = uploadUri,
              initialFilesUri = filesUri.map { it.toString() },
            ),
          )
        },
        openUrl = openUrl,
        sharePdf = {
          context.sharePDF(it, applicationId)
        },
      )
    }
    composable<ClaimDetailDestinations.AddFilesDestination>(
//      typeMap = mapOf(typeOf<ClaimDetailDestinations.AddFilesDestination>() to AddFilesDestinationNavType),
    ) { _, destination ->
      val viewModel: AddFilesViewModel = koinViewModel {
        parametersOf(destination.targetUploadUrl, destination.initialFilesUri)
      }
      AddFilesDestination(
        viewModel = viewModel,
        navigateUp = navigateUp,
        appPackageId = appPackageId,
        imageLoader = imageLoader,
      )
    }
  }
}

internal val AddFilesDestinationNavType = object : NavType<ClaimDetailDestinations.AddFilesDestination>(false) {
  val serializer = serializer<ClaimDetailDestinations.AddFilesDestination>()
  override fun put(bundle: Bundle, key: String, value: ClaimDetailDestinations.AddFilesDestination) {
//    bundle.putString(key, Json.encodeToString(serializer, value))
    bundle.putString(key, Json.encodeToString(value))
  }
  override fun get(bundle: Bundle, key: String): ClaimDetailDestinations.AddFilesDestination? {
    val encodedString = bundle.getString(key) ?: return null
    return Json.decodeFromString<ClaimDetailDestinations.AddFilesDestination>(encodedString)
  }
  override fun parseValue(value: String): ClaimDetailDestinations.AddFilesDestination {
    return Json.decodeFromString(serializer, value)
  }
  override fun serializeAsValue(value: ClaimDetailDestinations.AddFilesDestination): String {
    return Json.encodeToString(serializer, value)
  }
}
