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
      typeMap = mapOf(typeOf<ArrayList<String>>() to StringArrayListType),
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

private val StringArrayListType: NavType<ArrayList<String>> = object : CollectionNavType<ArrayList<String>>(false) {
  override fun put(bundle: Bundle, key: String, value: ArrayList<String>) {
    bundle.putStringArrayList(key, value)
  }

  @Suppress("UNCHECKED_CAST", "DEPRECATION")
  override fun get(bundle: Bundle, key: String): ArrayList<String>? {
    return bundle.getStringArrayList(key)
  }

  override fun parseValue(value: String): ArrayList<String> {
    return arrayListOf(value)
  }

  override fun parseValue(value: String, previousValue: ArrayList<String>): ArrayList<String> {
    return ArrayList(previousValue.toList() + value)
  }

  override fun valueEquals(value: ArrayList<String>, other: ArrayList<String>): Boolean {
    return value == other
  }

  override fun serializeAsValues(value: ArrayList<String>): List<String> {
    return value.toList()
  }
}
