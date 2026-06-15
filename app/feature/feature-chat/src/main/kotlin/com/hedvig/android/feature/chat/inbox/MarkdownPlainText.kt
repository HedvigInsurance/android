package com.hedvig.android.feature.chat.inbox

import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser

// The inbox conversation row shows a single ellipsized preview line with no clickable regions,
// so we strip markdown formatting rather than render it. We let the parser render to HTML and then
// strip the tags, so any syntax the parser understands collapses to its text content and future
// markdown features need no changes here.
internal fun String.markdownToPlainText(): String {
  val flavour = CommonMarkFlavourDescriptor()
  val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(this)
  val html = HtmlGenerator(this, tree, flavour).generateHtml()
  return html
    .replace(Regex("<[^>]*>"), " ") // strip tags; block boundaries become whitespace
    .replace("&lt;", "<")
    .replace("&gt;", ">")
    .replace("&quot;", "\"")
    .replace("&#39;", "'")
    .replace("&amp;", "&") // decode last so we don't re-interpret a decoded entity
    .replace(Regex("\\s+"), " ")
    .trim()
}