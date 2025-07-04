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

fun generatePackageSwiftFileContents(versionCode: String, checksum: String): String = """
// swift-tools-version:5.3

import PackageDescription

let package = Package(
    name: "umbrella",
    platforms: [
        .iOS(.v14),
    ],
    products: [
        .library(
            name: "umbrella",
            targets: ["umbrella"]
        )
    ],
    targets: [
        .binaryTarget(
            name: "umbrella",
            url: "https://github.com/HedvigInsurance/umbrella/releases/download/v$versionCode/umbrella.xcframework.zip",
            checksum: "$checksum"
        )
    ]
)
"""

fun generateChecksum(): String {
  return runCommand(
    "swift",
    "package",
    "compute-checksum",
    "../app/umbrella/build/XCFrameworks/release/umbrella.xcframework.zip",
  )
}

fun main() {
  val version: String = System.getenv("VERSION_CODE")
  println("version: $version")
//  val checksum = generateChecksum()
//  println(checksum)
  val fileContents = generatePackageSwiftFileContents(version, "checksum")
//  println(fileContents)
  runCommand("cd", "..")
  println(runCommand("ls"))
  println(runCommand("pwd"))
  runCommand("git", "clone", "https://github.com/HedvigInsurance/umbrella.git")
  // touch a file with fileContents in the user's root directory
  runCommand("touch", "./Package.swift")
  runCommand("bash", "-e", "echo $'$fileContents' > $./Package.swift")
}

main()
