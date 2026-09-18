plugins {
  id("hedvig.jvm.library")
  id("hedvig.gradle.plugin")
}

dependencies {
  compileOnly(libs.ktlint.ruleEngineCore)
  compileOnly(libs.ktlint.cliRulesetCore)
}
