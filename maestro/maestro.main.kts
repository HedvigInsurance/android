#! /usr/bin/env kotlinc -script --

@file:DependsOn("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
@file:DependsOn("com.github.pgreze:kotlin-process:1.5")

import com.github.pgreze.process.process
import kotlinx.coroutines.runBlocking
import java.io.IOException

runBlocking {
  try {
    process("maestro", "--version")
  } catch (e: IOException) {
    println("""Maestro is not installed. Try running `curl -Ls "https://get.maestro.mobile.dev" | bash`""")
    throw e
  }
  try {
    process("maestro", "test", "demo-mode.yaml")
  } catch (e: Exception) {
    if (e.message?.contains("Package com.hedvig.dev.app is not installed") == true) {
      println("The hedvig dev app needs to be installed for the test to run")
    }
    throw e
  }
}
