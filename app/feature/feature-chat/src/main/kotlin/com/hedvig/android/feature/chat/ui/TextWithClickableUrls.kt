package com.hedvig.android.feature.chat.ui

import android.content.Intent
import android.net.Uri
import android.util.Patterns
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withAnnotation
import androidx.compose.ui.text.withStyle
import com.hedvig.android.core.designsystem.material3.infoElement

@OptIn(ExperimentalTextApi::class)
@Composable
internal fun TextWithClickableUrls(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = TextStyle.Default,
  softWrap: Boolean = true,
  overflow: TextOverflow = TextOverflow.Clip,
  maxLines: Int = Int.MAX_VALUE,
  onTextLayout: (TextLayoutResult) -> Unit = {},
  linkStyle: SpanStyle = SpanStyle(
    color = MaterialTheme.colorScheme.infoElement,
    textDecoration = TextDecoration.Underline,
  ),
  linkMatcher: Regex = Patterns.WEB_URL.toRegex(),
  onClick: (url: String) -> Unit = with(LocalContext.current) {
    { url -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
  },
) {
  val annotatedText = buildAnnotatedString {
    val offset = linkMatcher.findAll(text).fold(0) { offset: Int, match: MatchResult ->
      val url = match.value
      val uri = run {
        val uri = Uri.parse(url)
        if (uri.scheme != null) {
          uri
        } else {
          uri.buildUpon().scheme("https").build()
        }
      }
      append(text.substring(offset until match.range.first))
      withAnnotation(tag = "URL", annotation = uri.toString()) {
        withStyle(style = linkStyle) {
          append(url)
        }
      }
      match.range.last + 1
    }
    append(text.substring(offset))
  }
  ClickableText(
    text = annotatedText,
    modifier = modifier,
    style = style,
    softWrap = softWrap,
    overflow = overflow,
    maxLines = maxLines,
    onTextLayout = onTextLayout,
    onClick = { offset: Int ->
      annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset).firstOrNull()
        ?.let { onClick(it.item) }
    },
  )
}
