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
import androidx.navigation.toRoute
import coil.ImageLoader
import com.hedvig.android.core.common.android.sharePDF
import com.hedvig.android.feature.claim.details.ui.AddFilesDestination
import com.hedvig.android.feature.claim.details.ui.AddFilesViewModel
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsDestination
import com.hedvig.android.feature.claim.details.ui.ClaimDetailsViewModel
import com.hedvig.android.navigation.compose.typed.composable
import com.hedvig.android.navigation.core.AppDestination
import kotlin.reflect.typeOf
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
      typeMap = mapOf(typeOf<ArrayList<String>>() to StringListType),
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

private val StringListType: NavType<List<String>> = object : CollectionNavType<List<String>>(false) {
  override fun put(bundle: Bundle, key: String, value: List<String>) {
    error("")
//    bundle.putStringArrayList(key, ArrayList(value))
    bundle.putStringArray(key, value.toTypedArray())
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  override fun get(bundle: Bundle, key: String): List<String>? {
    error("")
//    return bundle[key] as ArrayList<String>
    return bundle.getStringArray(key)?.toList()
  }

  override fun parseValue(value: String): List<String> {
    error("")
    return listOf(value)
  }

  override fun parseValue(value: String, previousValue: List<String>): List<String> {
    error("")
    return previousValue + value
  }

  override fun valueEquals(value: List<String>, other: List<String>): Boolean {
    error("")
    return value == other
  }

  override fun serializeAsValues(value: List<String>): List<String> {
    error("")
    return value
  }
}
