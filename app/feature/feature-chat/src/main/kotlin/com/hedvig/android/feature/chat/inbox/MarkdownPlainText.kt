package com.hedvig.android.feature.chat.inbox

import org.intellij.markdown.IElementType
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

// The inbox conversation row shows a single ellipsized preview line with no clickable regions,
// so we strip markdown formatting rather than render it. Walking the markdown AST means any
// syntax the parser understands collapses to its text content, so future markdown features need
// no changes here.
internal fun String.markdownToPlainText(): String {
  val flavour = CommonMarkFlavourDescriptor()
  val tree = MarkdownParser(flavour).buildMarkdownTreeFromString(this)
  return buildString {
    appendPlainText(tree, this@markdownToPlainText)
  }
}

private fun StringBuilder.appendPlainText(node: ASTNode, src: String) {
  val nodeType = node.type

  // Check if this is a text node by comparing with known IElementType instances
  if (nodeType.toString().contains("TEXT") || nodeType.toString().contains("Code")) {
    append(node.getTextInNode(src))
    return
  }

  // EOL creates a space
  if (nodeType.toString().contains("EOL")) {
    append(' ')
    return
  }

  // Drop images
  if (nodeType == MarkdownElementTypes.IMAGE) return

  // Recurse into children
  node.children.forEach { child ->
    appendPlainText(child, src)
    // Add space between block elements
    if (child.type.toString().contains("Block")) append(' ')
  }
}
