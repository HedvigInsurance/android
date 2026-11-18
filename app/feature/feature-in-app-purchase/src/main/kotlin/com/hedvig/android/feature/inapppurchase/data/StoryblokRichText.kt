package com.hedvig.android.feature.inapppurchase.data

/**
 * Renders a Storyblok rich text document (`{"type": "doc", "content": [...]}`) as markdown. Node types without a
 * markdown equivalent keep only their text.
 */
internal fun storyblokRichTextToMarkdown(document: Any?): String {
  return blocksOf(document).joinToString(separator = "\n\n").trim()
}

private fun blocksOf(node: Any?): List<String> {
  return childrenOf(node).mapNotNull { child ->
    when (typeOf(child)) {
      "paragraph" -> inlineOf(child)

      "heading" -> "#".repeat(attrsOf(child)["level"].toIntOrDefault(2)) + " " + inlineOf(child)

      "bullet_list" -> childrenOf(child).joinToString("\n") { "- " + listItemOf(it) }

      "ordered_list" -> childrenOf(child).withIndex().joinToString("\n") { (index, item) ->
        "${index + 1}. " + listItemOf(item)
      }

      "blockquote" -> blocksOf(child).joinToString("\n") { "> $it" }

      "horizontal_rule" -> "---"

      else -> inlineOf(child)
    }.takeIf { it.isNotBlank() }
  }
}

private fun listItemOf(item: Any?): String = blocksOf(item).joinToString(" ")

private fun inlineOf(node: Any?): String {
  return childrenOf(node).joinToString(separator = "") { child ->
    when (typeOf(child)) {
      "text" -> withMarks((child as Map<*, *>)["text"] as? String ?: "", child["marks"] as? List<*>)
      "hard_break" -> "  \n"
      else -> inlineOf(child)
    }
  }
}

private fun withMarks(text: String, marks: List<*>?): String {
  return marks.orEmpty().fold(text) { marked, mark ->
    when (typeOf(mark)) {
      "bold" -> "**$marked**"
      "italic" -> "*$marked*"
      "link" -> (attrsOf(mark)["href"] as? String)?.let { "[$marked]($it)" } ?: marked
      else -> marked
    }
  }
}

private fun typeOf(node: Any?): String? = (node as? Map<*, *>)?.get("type") as? String

private fun attrsOf(node: Any?): Map<*, *> = (node as? Map<*, *>)?.get("attrs") as? Map<*, *> ?: emptyMap<Any, Any>()

private fun childrenOf(node: Any?): List<*> = (node as? Map<*, *>)?.get("content") as? List<*> ?: emptyList<Any>()

private fun Any?.toIntOrDefault(default: Int): Int = (this as? Number)?.toInt() ?: default
