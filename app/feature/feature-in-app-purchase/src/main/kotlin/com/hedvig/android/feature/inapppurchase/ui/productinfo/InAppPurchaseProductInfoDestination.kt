package com.hedvig.android.feature.inapppurchase.ui.productinfo

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import com.hedvig.android.compose.ui.animateContentHeight
import com.hedvig.android.compose.ui.plus
import com.hedvig.android.data.contract.gradientResource
import com.hedvig.android.design.system.hedvig.HedvigMultiScreenPreview
import com.hedvig.android.design.system.hedvig.HedvigTabRow
import com.hedvig.android.design.system.hedvig.HedvigTheme
import com.hedvig.android.design.system.hedvig.InsuranceCard
import com.hedvig.android.design.system.hedvig.Surface
import com.hedvig.android.design.system.hedvig.TabDefaults.TabSize.Small
import com.hedvig.android.design.system.hedvig.TabDefaults.TabStyle.Filled
import com.hedvig.android.design.system.hedvig.TopAppBarWithBack
import com.hedvig.android.design.system.hedvig.hedvigDropShadow
import com.hedvig.android.design.system.hedvig.rememberHedvigTabRowState
import com.hedvig.android.design.system.hedvig.rememberPreviewImageLoader
import com.hedvig.android.design.system.hedvig.scaledBy
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseMocks
import com.hedvig.android.feature.inapppurchase.data.InAppPurchaseProduct
import hedvig.resources.Res
import hedvig.resources.insurance_details_view_tab_1_title
import hedvig.resources.insurance_details_view_tab_2_title
import hedvig.resources.insurance_details_view_tab_3_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InAppPurchaseProductInfoDestination(
  product: InAppPurchaseProduct,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  InAppPurchaseProductInfoScreen(
    product = product,
    imageLoader = imageLoader,
    navigateUp = navigateUp,
    openUrl = openUrl,
  )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InAppPurchaseProductInfoScreen(
  product: InAppPurchaseProduct,
  imageLoader: ImageLoader,
  navigateUp: () -> Unit,
  openUrl: (String) -> Unit,
) {
  val variant = product.variants.firstOrNull()
  Surface(color = HedvigTheme.colorScheme.backgroundPrimary, modifier = Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
      TopAppBarWithBack(title = product.displayNameShort, onClick = navigateUp)
      val pagerState = rememberPagerState(pageCount = { 3 })
      LazyColumn(
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
          .asPaddingValues()
          .plus(PaddingValues(top = 16.dp)),
        modifier = Modifier.fillMaxSize(),
      ) {
        item(key = 1, contentType = "InsuranceCard") {
          InsuranceCard(
            chips = emptyList(),
            topText = product.displayNameFull,
            bottomText = product.tagline,
            imageLoader = imageLoader,
            isLoading = false,
            fallbackPainter = variant?.contractGroup?.let { painterResource(it.gradientResource()) }
              ?: ColorPainter(Color.Black.copy(alpha = 0.7f)),
            imageContentScale = ContentScale.Crop.scaledBy(1.32f),
            modifier = Modifier
              .padding(horizontal = 16.dp)
              .hedvigDropShadow(),
          )
        }
        item(key = 2, contentType = "space") { Spacer(Modifier.height(16.dp)) }
        stickyHeader(key = 3, contentType = "PagerSelector") {
          Surface(color = HedvigTheme.colorScheme.backgroundPrimary) {
            PagerSelector(
              pagerState = pagerState,
              modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp),
            )
          }
        }
        item(key = 4, contentType = "Pager") {
          HorizontalPager(
            state = pagerState,
            key = { it },
            flingBehavior = PagerDefaults.flingBehavior(
              state = pagerState,
              snapAnimationSpec = horizontalPagerSpringSpec(),
            ),
            verticalAlignment = Alignment.Top,
            modifier = Modifier
              .padding(top = 8.dp)
              .animateContentHeight(spring(stiffness = Spring.StiffnessLow)),
          ) { pageIndex ->
            when (pageIndex) {
              0 -> OverviewTab(faq = product.faq)

              1 -> CoverageTab(
                insurableLimits = variant?.insurableLimits.orEmpty(),
                perils = variant?.perils.orEmpty(),
              )

              2 -> DocumentsTab(documents = variant?.documents.orEmpty(), onDocumentClick = openUrl)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PagerSelector(pagerState: PagerState, modifier: Modifier = Modifier) {
  HedvigTabRow(
    tabTitles = listOf(
      stringResource(Res.string.insurance_details_view_tab_1_title),
      stringResource(Res.string.insurance_details_view_tab_2_title),
      stringResource(Res.string.insurance_details_view_tab_3_title),
    ),
    tabRowState = rememberHedvigTabRowState(pagerState),
    selectIndicatorAnimationSpec = horizontalPagerSpringSpec(IntOffset.VisibilityThreshold),
    tabSize = Small,
    tabStyle = Filled,
    modifier = modifier.fillMaxWidth(),
  )
}

private fun <T> horizontalPagerSpringSpec(visibilityThreshold: T? = null) = spring(
  stiffness = Spring.StiffnessMediumLow,
  visibilityThreshold = visibilityThreshold,
)

@HedvigMultiScreenPreview
@Composable
private fun PreviewInAppPurchaseProductInfoScreen() {
  HedvigTheme {
    InAppPurchaseProductInfoScreen(
      product = InAppPurchaseMocks.accidentForm.product,
      imageLoader = rememberPreviewImageLoader(),
      navigateUp = {},
      openUrl = {},
    )
  }
}
