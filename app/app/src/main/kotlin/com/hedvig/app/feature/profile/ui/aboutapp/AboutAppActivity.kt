package com.hedvig.app.feature.profile.ui.aboutapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hedvig.android.auth.android.AuthenticatedObserver
import com.hedvig.android.core.designsystem.preview.HedvigPreview
import com.hedvig.android.core.designsystem.theme.HedvigTheme
import com.hedvig.android.core.ui.appbar.TopAppBarWithBack
import com.hedvig.app.BuildConfig
import com.hedvig.app.R
import com.hedvig.app.feature.dismissiblepager.DismissiblePagerModel
import com.hedvig.app.feature.embark.ui.MemberIdViewModel
import com.hedvig.app.feature.whatsnew.WhatsNewDialog
import com.hedvig.app.feature.whatsnew.WhatsNewViewModel
import com.hedvig.app.isDebug
import com.hedvig.app.util.apollo.ThemedIconUrls
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AboutAppActivity : AppCompatActivity() {
  private val memberIdViewModel: MemberIdViewModel by viewModel()
  private val whatsNewViewModel: WhatsNewViewModel by viewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.addObserver(AuthenticatedObserver())
    WindowCompat.setDecorFitsSystemWindows(window, false)

    getViewModel<AboutAppViewModel>()

    var whatsNewOnClickListener: (() -> Unit)? by mutableStateOf<(() -> Unit)?>(null)
    whatsNewViewModel.news.observe(this@AboutAppActivity) { data ->
      whatsNewOnClickListener = {
        WhatsNewDialog
          .newInstance(
            data.news.mapIndexed { index, page ->
              DismissiblePagerModel.TitlePage(
                ThemedIconUrls.from(page.illustration.variants.fragments.iconVariantsFragment),
                page.title,
                page.paragraph,
                getString(
                  if (index == data.news.size - 1) {
                    hedvig.resources.R.string.NEWS_DISMISS
                  } else {
                    hedvig.resources.R.string.NEWS_PROCEED
                  },
                ),
              )
            },
          )
          .show(supportFragmentManager, WhatsNewDialog.TAG)
      }
    }

    whatsNewViewModel.fetchNews(NEWS_BASE_VERSION)
    setContent {
      val memberIdState by memberIdViewModel.state.collectAsStateWithLifecycle()
      val memberId: String? by remember {
        derivedStateOf {
          val latestMemberIdState = memberIdState
          when (latestMemberIdState) {
            is MemberIdViewModel.State.Success -> {
              latestMemberIdState.id
            }
            else -> null
          }
        }
      }
      HedvigTheme {
        AboutAppScreen(
          whatsNewOnClickListener = whatsNewOnClickListener,
          memberId = memberId,
          onBackPressed = onBackPressedDispatcher::onBackPressed,
          showOpenSourceLicenses = { OpenSourceLicensesDialog().showLicenses(this@AboutAppActivity) },
        )
      }
    }
  }

  companion object {
    private const val NEWS_BASE_VERSION = "3.0.0"
  }
}

@Composable
private fun AboutAppScreen(
  whatsNewOnClickListener: (() -> Unit)?,
  memberId: String?,
  onBackPressed: () -> Unit,
  showOpenSourceLicenses: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.background,
    modifier = Modifier.fillMaxSize(),
  ) {
    Column(
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    ) {
      TopAppBarWithBack(
        onClick = onBackPressed,
        contentPadding = WindowInsets.safeDrawing
          .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
          .asPaddingValues(),
      )
      Text(
        text = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_APP_TITLE),
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(horizontal = 24.dp),
      )
      Spacer(Modifier.height(16.dp))
      AboutAppRow(
        text = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_APP_LICENSE_ATTRIBUTIONS),
        onClick = showOpenSourceLicenses,
      )
      if (whatsNewOnClickListener != null) {
        AboutAppRow(
          text = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_APP_OPEN_WHATS_NEW),
          onClick = whatsNewOnClickListener,
        )
      }
      Spacer(Modifier.height(8.dp))
      CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(
          text = buildString {
            append(
              stringResource(
                hedvig.resources.R.string.PROFILE_ABOUT_APP_VERSION,
                BuildConfig.VERSION_NAME,
              ),
            )
            if (isDebug()) {
              append(" (")
              append(BuildConfig.VERSION_CODE)
              append(")")
            }
          },
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Text(
          text = if (memberId == null) {
            ""
          } else {
            stringResource(hedvig.resources.R.string.PROFILE_ABOUT_APP_MEMBER_ID, memberId)
          },
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
        Text(
          text = stringResource(hedvig.resources.R.string.PROFILE_ABOUT_APP_COPYRIGHT),
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        )
      }
    }
  }
}

@Composable
private fun AboutAppRow(
  text: String,
  onClick: () -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 24.dp, vertical = 16.dp),
  ) {
    Text(text = text, modifier = Modifier.weight(1f, true))
    Icon(
      painter = painterResource(R.drawable.ic_arrow_forward),
      contentDescription = null,
      modifier = Modifier.size(24.dp),
    )
  }
}

@HedvigPreview
@Composable
private fun PreviewAboutAppScreen() {
  HedvigTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
      AboutAppScreen({}, "123", {}, {})
    }
  }
}
