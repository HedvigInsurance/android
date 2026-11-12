package com.hedvig.android.desktop.walkthrough

import java.io.File

internal enum class Outcome(val label: String) {
  Rendered("rendered"),
  Threw("threw"),
  TimedOut("timed out"),
  NeedsData("needs data"),
  NotAvailable("not available on desktop"),
  Fallback("fallback entry"),
  Excluded("excluded"),
}

internal data class Visit(
  val name: String,
  val kind: String,
  val outcome: Outcome,
  val settle: SettleResult?,
  /** The arguments faked to build the key, why it could not be built, or where the visit landed. */
  val notes: List<String>,
  val failure: String?,
  val problems: List<LoggedProblem>,
  val png: String?,
)

/** Writes `index.md`: a summary of counts, a table of every visit, then the log lines and failures per visit. */
internal fun writeIndex(file: File, header: List<String>, visits: List<Visit>) {
  val text = buildString {
    appendLine("# Desktop walkthrough")
    appendLine()
    header.forEach { appendLine("- $it") }
    appendLine()
    appendLine("## Counts")
    appendLine()
    Outcome.entries.forEach { outcome ->
      val count = visits.count { it.outcome == outcome }
      if (count > 0) appendLine("- ${outcome.label}: $count")
    }
    appendLine()
    appendLine("## Visits")
    appendLine()
    appendLine("| # | Screen | Kind | Outcome | Settle | ms | WARN | ERROR | Notes | PNG |")
    appendLine("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |")
    visits.forEachIndexed { index, visit ->
      val warnings = visit.problems.count { it.priority.name == "WARN" }
      val errors = visit.problems.count { it.priority.name != "WARN" }
      val png = visit.png?.let { "[$it]($it)" } ?: ""
      appendLine(
        "| ${index + 1} | `${visit.name}` | ${visit.kind} | ${visit.outcome.label} | " +
          "${visit.settle?.settle?.name?.lowercase() ?: ""} | ${visit.settle?.millis ?: ""} | $warnings | $errors | " +
          "${visit.notes.joinToString("; ").cell()} | $png |",
      )
    }
    appendLine()
    appendLine("## Failures and log lines per visit")
    visits.filter { it.failure != null || it.problems.isNotEmpty() }.forEach { visit ->
      appendLine()
      appendLine("### ${visit.name} (${visit.outcome.label})")
      appendLine()
      appendLine("```")
      visit.failure?.let { appendLine("FAILURE: $it") }
      visit.problems.forEach { problem ->
        appendLine("${problem.priority.name.first()}/${problem.tag ?: "Hedvig"}: ${problem.message.take(500)}")
        problem.trace?.let { appendLine(it.prependIndent("    ")) }
      }
      appendLine("```")
    }
  }
  file.writeText(text)
}

private fun String.cell(): String = replace("|", "\\|").replace("\n", " ")
