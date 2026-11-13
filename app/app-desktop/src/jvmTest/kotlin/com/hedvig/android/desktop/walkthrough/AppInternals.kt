package com.hedvig.android.desktop.walkthrough

import java.io.File
import java.lang.reflect.Modifier
import java.util.IdentityHashMap
import org.jetbrains.skia.Color
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface

/**
 * Finds an object of the class named [className] reachable from [root] through fields, map values, collection
 * elements and arrays, breadth first. Fields of `java.*` and `kotlin.*` classes are not read; their collections are
 * walked through their public API.
 */
internal fun findReachable(root: Any, className: String, maxObjects: Int = 50_000): Any? {
  val seen = IdentityHashMap<Any, Unit>()
  val queue = ArrayDeque<Any>()
  queue += root
  while (queue.isNotEmpty() && seen.size < maxObjects) {
    val current = queue.removeFirst()
    if (seen.put(current, Unit) != null) continue
    if (current.javaClass.name == className) return current
    val children: List<Any?> = when (current) {
      is Map<*, *> -> runCatching { current.values.toList() }.getOrDefault(emptyList())
      is Iterable<*> -> runCatching { current.toList() }.getOrDefault(emptyList())
      is Array<*> -> current.toList()
      else -> fieldValues(current)
    }
    children.filterNotNull().filterNot { it.isLeaf() }.forEach(queue::addLast)
  }
  return null
}

private fun Any.isLeaf(): Boolean =
  this is String || this is Number || this is Boolean || this is Char || this is Class<*> || this is Enum<*>

private fun fieldValues(target: Any): List<Any?> {
  val values = mutableListOf<Any?>()
  var type: Class<*>? = target.javaClass
  while (type != null && !type.name.startsWith("java.") && !type.name.startsWith("kotlin.")) {
    for (field in type.declaredFields) {
      if (Modifier.isStatic(field.modifiers) || field.type.isPrimitive) continue
      val value = runCatching {
        field.isAccessible = true
        field.get(target)
      }.getOrNull()
      values += value
    }
    type = type.superclass
  }
  return values
}

/** Calls `emit(event)` on a `MoleculeViewModel`, the way its screen does. */
internal fun emitEvent(viewModel: Any, event: Any) {
  val emit = generateSequence<Class<*>>(viewModel.javaClass) { it.superclass }
    .flatMap { it.declaredMethods.asSequence() }
    .first { it.name == "emit" && it.parameterCount == 1 }
  emit.isAccessible = true
  emit.invoke(viewModel, event)
}

/** A `com.eygraber.uri.Uri` for [file], built the way the desktop file picker builds the URIs it hands over. */
internal fun fileUri(file: File): Any {
  val uriClass = Class.forName("com.eygraber.uri.Uri")
  val companion = uriClass.getField("Companion").get(null)
  val parse = companion.javaClass.methods.first { it.name == "parse" && it.parameterCount == 1 }
  return parse.invoke(companion, file.absoluteFile.toURI().toString())
}

/** Builds the chat event class [simpleName] of `CbmChatEvent` from a single argument. */
internal fun chatEvent(simpleName: String, argument: Any): Any {
  val eventClass = Class.forName("com.hedvig.android.feature.chat.CbmChatEvent\$$simpleName")
  val constructor = eventClass.declaredConstructors.first { it.parameterCount == 1 }
  constructor.isAccessible = true
  return constructor.newInstance(argument)
}

/** Writes a small PNG: a coloured square with a white bar, so it is recognisable in a chat thumbnail. */
internal fun writeTestPng(file: File) {
  val surface = Surface.makeRasterN32Premul(240, 160)
  surface.canvas.clear(Color.makeRGB(80, 120, 220))
  surface.canvas.drawRect(Rect.makeXYWH(20f, 60f, 200f, 40f), Paint().apply { color = Color.WHITE })
  val bytes = surface.makeImageSnapshot().encodeToData(EncodedImageFormat.PNG)?.bytes ?: error("PNG encoding failed")
  file.writeBytes(bytes)
}

/** Writes a minimal one-page PDF whose page shows [text]. */
internal fun writeTestPdf(file: File, text: String) {
  val content = "BT /F1 18 Tf 40 100 Td ($text) Tj ET"
  val objects = listOf(
    "<< /Type /Catalog /Pages 2 0 R >>",
    "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 400 200] /Contents 4 0 R " +
      "/Resources << /Font << /F1 5 0 R >> >> >>",
    "<< /Length ${content.length} >>\nstream\n$content\nendstream",
    "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
  )
  val body = StringBuilder("%PDF-1.4\n")
  val offsets = mutableListOf<Int>()
  objects.forEachIndexed { index, value ->
    offsets += body.length
    body.append("${index + 1} 0 obj\n$value\nendobj\n")
  }
  val xref = body.length
  body.append("xref\n0 ${objects.size + 1}\n0000000000 65535 f \n")
  offsets.forEach { body.append("%010d 00000 n \n".format(it)) }
  body.append("trailer\n<< /Size ${objects.size + 1} /Root 1 0 R >>\nstartxref\n$xref\n%%EOF\n")
  file.writeText(body.toString(), Charsets.ISO_8859_1)
}
