package com.hedvig.android.feature.chat.inbox

import com.halilibo.richtext.commonmark.CommonMarkdownParseOptions
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.markdown.node.AstBlockNodeType
import com.halilibo.richtext.markdown.node.AstCode
import com.halilibo.richtext.markdown.node.AstHardLineBreak
import com.halilibo.richtext.markdown.node.AstImage
import com.halilibo.richtext.markdown.node.AstNode
import com.halilibo.richtext.markdown.node.AstSoftLineBreak
import com.halilibo.richtext.markdown.node.AstText

// The inbox conversation row shows a single ellipsized preview line with no clickable regions,
// so we strip markdown formatting rather than render it. Walking the commonmark AST means any
// syntax the parser understands collapses to its text content, so future markdown features need
// no changes here.
internal fun String.markdownToPlainText(): String = buildString {
  appendPlainText(inboxMarkdownParser.parse(this@markdownToPlainText))
}

private val inboxMarkdownParser = CommonmarkAstNodeParser(CommonMarkdownParseOptions(autolink = false))

private fun StringBuilder.appendPlainText(node: AstNode) {
  val type = node.type
  if (type is AstText) {
    append(type.literal)
    return
  }
  if (type is AstCode) {
    append(type.literal)
    return
  }
  if (type === AstSoftLineBreak || type === AstHardLineBreak) {
    append(' ')
    return
  }
  // Alt text isn't useful in a one-line preview, so we drop the image entirely.
  if (type is AstImage) return
  // Container node — recurse into children, separating block-level siblings with a space so
  // paragraphs don't run together.
  var child = node.links.firstChild
  while (child != null) {
    appendPlainText(child)
    if (child.links.next != null && child.type is AstBlockNodeType) append(' ')
    child = child.links.next
  }
}
