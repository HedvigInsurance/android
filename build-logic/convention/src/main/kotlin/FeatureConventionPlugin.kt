import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Common configuration for feature modules.
 */
class FeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      ensureNotDependingOnOtherFeatureModule()
    }
  }
}

private fun Project.ensureNotDependingOnOtherFeatureModule() {
  val thisModuleName = this.name
  configurations.configureEach {
    resolutionStrategy {
      eachDependency {
        if (requested.group != "hedvigandroid") return@eachDependency // Only check for our own modules
        if (requested.name == thisModuleName) return@eachDependency // Only check deps to other modules
        val requestedModuleIsAFeatureModule =
          requested.name.startsWith("feature-") && !requested.name.startsWith("feature-flags")
        require(!requestedModuleIsAFeatureModule) {
          "Hedvig build error on FeatureConventionPlugin." +
            "\nYou are trying to depend on another feature module from a feature module." +
            "\nThis is not allowed as it breaks our ability to properly share code between modules." +
            "\nIn particular, ${thisModuleName} is trying to depend on ${requested.name}." +
            "\nIf you need to share code between feature modules, consider moving the shared code to a library module."
        }
      }
    }
  }
}
