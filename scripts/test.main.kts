#!/usr/bin/env kotlin

import kotlin.Exception

fun runCommand(vararg args: String): String {
  val builder = ProcessBuilder(*args)
    .redirectError(ProcessBuilder.Redirect.INHERIT)
  val process = builder.start()
  val output = process.inputStream.bufferedReader().readText().trim()
  val ret = process.waitFor()
  if (ret != 0) {
    throw Exception("command ${args.joinToString(" ")} failed:\n$output")
  }
  return output
}

fun main() {
  println(runCommand("ls"))
  println(runCommand("pwd"))
}

main()
